import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { Button, TextField, InputAdornment, CircularProgress } from "@mui/material";
import { useFormik } from "formik";
import { useAppDispatch, useAppSelector } from "../../../State/store";
import { sellerLogin } from "../../../State/seller/sellerAuthSlice";
import { sendLoginSignupOtp } from "../../../State/authSlice";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";
import * as Yup from "yup";
import { motion, AnimatePresence } from "framer-motion";
import { CheckCircle, ArrowLeft } from "lucide-react";
import ResendOtpButton from "../../../component/common/ResendOtpButton";

// ─────────────────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────────────────

// IMPORTANT: these two prefixes are NOT the same thing and must not be
// unified. They are consumed by two different backend code paths:
//
//   1. AuthServiceImpl.sentLoginOtp() strips SIGNING_PREFIX ("signing_") to
//      confirm the account exists before issuing an OTP, then stores +
//      emails the OTP against the RAW (unprefixed) address. This prefix is
//      only used on the send-OTP call.
//
//   2. AuthServiceImpl.authenticate() (called from signing()) strips
//      SELLER_PREFIX ("seller_") to validate against the Seller table, then
//      looks up the VerificationCode by the RAW (unprefixed) address. This
//      prefix is only used on the verify-OTP call — see sellerAuthSlice.ts.
//
// Both ultimately key off the same raw email, so as long as each request
// uses the prefix its endpoint actually expects, they converge correctly.
// Mixing them up either breaks OTP lookup (401) or breaks mail delivery
// (the mangled string gets used as the literal "to" address).
const SIGNING_EMAIL_PREFIX = "signing_";
const SELLER_ROLE = "ROLE_SELLER" as const;

const OTP_LENGTH = 6;
const OTP_RESEND_COOLDOWN_SECONDS = 60;
const MAX_VERIFY_ATTEMPTS = 5;

// ─────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────

/**
 * Normalizes an email the same way on every request so the OTP the backend
 * generates and the OTP it later looks up always resolve to the same key.
 * Backends frequently treat "Foo@Bar.com " and "foo@bar.com" as different
 * cache/DB keys unless normalized on both sides.
 */
const normalizeEmail = (email: string): string => email.trim().toLowerCase();

const toSigningEmail = (email: string): string =>
    `${SIGNING_EMAIL_PREFIX}${normalizeEmail(email)}`;

const sanitizeOtpInput = (value: string): string =>
    value.replace(/\D/g, "").slice(0, OTP_LENGTH);

const getErrorMessage = (error: unknown, fallback: string): string => {
    if (typeof error === "string" && error.trim().length > 0) return error;
    if (error && typeof error === "object" && "message" in error) {
        const msg = (error as { message?: unknown }).message;
        if (typeof msg === "string" && msg.trim().length > 0) return msg;
    }
    return fallback;
};

// ─────────────────────────────────────────────────────────────────────────
// Validation schemas
// ─────────────────────────────────────────────────────────────────────────

const emailSchema = Yup.object({
    email: Yup.string()
        .trim()
        .email("Enter a valid email address")
        .max(254, "Email is too long")
        .required("Email is required"),
});

const loginSchema = Yup.object({
    email: Yup.string().trim().email("Enter a valid email address").required("Email is required"),
    otp: Yup.string()
        .trim()
        .matches(/^\d+$/, "OTP must contain digits only")
        .length(OTP_LENGTH, `OTP must be ${OTP_LENGTH} digits`)
        .required("OTP is required"),
});

type SellerLoginFormValues = {
    email: string;
    otp: string;
};

type Step = "email" | "otp";

const SellerLoginForm = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    // Redux-backed state for the actual verify request — this is the
    // source of truth for "is a login attempt in flight / did it fail",
    // rather than a separate local flag that can drift out of sync.
    const { loading: isVerifying, seller } = useAppSelector((state) => state.sellerAuth);

    const [step, setStep] = useState<Step>("email");
    const [isSendingOtp, setIsSendingOtp] = useState(false);
    const [resendTimer, setResendTimer] = useState(0);
    const [verifyAttempts, setVerifyAttempts] = useState(0);
    const [isLocked, setIsLocked] = useState(false);

    const otpInputRef = useRef<HTMLInputElement | null>(null);

    // Redirect if already authenticated — avoids showing a stale login
    // form to someone who already has a valid session.
    useEffect(() => {
        if (seller) {
            navigate("/seller/dashboard", { replace: true });
        }
    }, [seller, navigate]);

    const formik = useFormik<SellerLoginFormValues>({
        initialValues: { email: "", otp: "" },
        validationSchema: step === "otp" ? loginSchema : emailSchema,
        validateOnBlur: true,
        onSubmit: async (values, { setSubmitting }) => {
            if (isLocked) {
                toast.error("Too many failed attempts. Please request a new OTP.");
                setSubmitting(false);
                return;
            }

            try {
                await dispatch(
                    sellerLogin({
                        email: normalizeEmail(values.email),
                        otp: values.otp.trim(),
                    })
                ).unwrap();

                toast.success("Logged in successfully!");
                navigate("/seller/dashboard", { replace: true });
            } catch (error: unknown) {
                const attempts = verifyAttempts + 1;
                setVerifyAttempts(attempts);

                if (attempts >= MAX_VERIFY_ATTEMPTS) {
                    setIsLocked(true);
                    toast.error(
                        "Too many failed attempts. Please request a new OTP to continue."
                    );
                } else {
                    toast.error(
                        getErrorMessage(
                            error,
                            `Invalid or expired OTP. ${MAX_VERIFY_ATTEMPTS - attempts} attempt(s) remaining.`
                        )
                    );
                }

                // Never leave a failed OTP value sitting in the field.
                formik.setFieldValue("otp", "", false);
            } finally {
                setSubmitting(false);
            }
        },
    });

    const handleSendOtp = useCallback(async () => {
        if (isSendingOtp || resendTimer > 0) return;

        setIsSendingOtp(true);
        try {
            const email = normalizeEmail(formik.values.email);
            await emailSchema.validate({ email });

            await dispatch(
                sendLoginSignupOtp({ email: toSigningEmail(email), role: SELLER_ROLE })
            ).unwrap();

            toast.success("OTP sent to your email!");
            setStep("otp");
            setResendTimer(OTP_RESEND_COOLDOWN_SECONDS);
            setVerifyAttempts(0);
            setIsLocked(false);
            formik.setFieldValue("otp", "", false);
        } catch (error: any) {
            if (error?.name === "ValidationError") {
                formik.setFieldError("email", error.message);
            } else {
                toast.error(getErrorMessage(error, "Failed to send OTP. Please try again."));
            }
        } finally {
            setIsSendingOtp(false);
        }
    }, [dispatch, formik, isSendingOtp, resendTimer]);

    const handleChangeEmail = useCallback(() => {
        setStep("email");
        setResendTimer(0);
        setVerifyAttempts(0);
        setIsLocked(false);
        formik.setFieldValue("otp", "", false);
        formik.setFieldTouched("otp", false, false);
    }, [formik]);

    // Countdown effect for resend cooldown.
    useEffect(() => {
        if (resendTimer <= 0) return;
        const timer = setTimeout(() => setResendTimer((prev) => prev - 1), 1000);
        return () => clearTimeout(timer);
    }, [resendTimer]);

    // Autofocus the OTP field the moment it appears.
    useEffect(() => {
        if (step === "otp") {
            otpInputRef.current?.focus();
        }
    }, [step]);

    const attemptsRemaining = useMemo(
        () => Math.max(MAX_VERIFY_ATTEMPTS - verifyAttempts, 0),
        [verifyAttempts]
    );

    const isFormDisabled = isVerifying || isSendingOtp;

    return (
        <div className="max-w-sm mx-auto p-6 space-y-6 border rounded shadow">
            <h1 className="text-center font-bold text-2xl text-primary-color pb-4">
                Seller Login
            </h1>

            <form onSubmit={formik.handleSubmit} className="space-y-5" aria-busy={isFormDisabled}>
                <TextField
                    fullWidth
                    name="email"
                    label="Email"
                    type="email"
                    autoComplete="email"
                    value={formik.values.email}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    disabled={step === "otp" || isFormDisabled}
                    error={formik.touched.email && Boolean(formik.errors.email)}
                    helperText={formik.touched.email && formik.errors.email}
                    InputProps={{
                        endAdornment: (
                            <InputAdornment position="end">
                                <AnimatePresence>
                                    {step === "otp" && (
                                        <motion.div
                                            key="success-icon"
                                            initial={{ scale: 0, opacity: 0 }}
                                            animate={{ scale: 1, opacity: 1 }}
                                            exit={{ scale: 0, opacity: 0 }}
                                            transition={{ type: "spring", stiffness: 300, damping: 15 }}
                                        >
                                            <CheckCircle className="text-green-500" size={22} aria-label="Email verified" />
                                        </motion.div>
                                    )}
                                </AnimatePresence>
                            </InputAdornment>
                        ),
                    }}
                />

                <AnimatePresence>
                    {step === "otp" && (
                        <motion.div
                            key="otp-field"
                            initial={{ opacity: 0, y: -10 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -10 }}
                            transition={{ duration: 0.3 }}
                            className="space-y-2"
                        >
                            <div className="flex items-center justify-between">
                                <p className="font-medium text-sm opacity-60">
                                    Enter the {OTP_LENGTH}-digit code sent to your email
                                </p>
                                <button
                                    type="button"
                                    onClick={handleChangeEmail}
                                    disabled={isFormDisabled}
                                    className="flex items-center gap-1 text-xs opacity-60 hover:opacity-100 disabled:opacity-30"
                                >
                                    <ArrowLeft size={14} />
                                    Change email
                                </button>
                            </div>

                            <TextField
                                fullWidth
                                name="otp"
                                label="OTP"
                                inputRef={otpInputRef}
                                inputMode="numeric"
                                autoComplete="one-time-code"
                                inputProps={{ maxLength: OTP_LENGTH }}
                                value={formik.values.otp}
                                onChange={(e) =>
                                    formik.setFieldValue("otp", sanitizeOtpInput(e.target.value))
                                }
                                onBlur={formik.handleBlur}
                                disabled={isFormDisabled || isLocked}
                                error={formik.touched.otp && Boolean(formik.errors.otp)}
                                helperText={
                                    (formik.touched.otp && formik.errors.otp) ||
                                    (verifyAttempts > 0 && !isLocked
                                        ? `${attemptsRemaining} attempt(s) remaining`
                                        : " ")
                                }
                            />

                            <div
                                role="status"
                                aria-live="polite"
                                className="flex items-center justify-between text-sm pt-1"
                            >
                                <span className="opacity-60">Didn't get it?</span>
                                <ResendOtpButton
                                    onResend={handleSendOtp}
                                    cooldown={OTP_RESEND_COOLDOWN_SECONDS}
                                    resendText="Send Again"
                                    resendPrefixText="Retry in"
                                    resendSuffixText=" sec"
                                    buttonProps={{
                                        color: "primary",
                                        variant: "text",
                                        size: "small",
                                        disabled: isFormDisabled,
                                    }}
                                />
                            </div>

                            {isLocked && (
                                <p role="alert" className="text-red-600 text-xs pt-1">
                                    Too many failed attempts. Request a new OTP to try again.
                                </p>
                            )}
                        </motion.div>
                    )}
                </AnimatePresence>

                {step === "email" ? (
                    <Button
                        onClick={handleSendOtp}
                        fullWidth
                        variant="outlined"
                        sx={{ py: "11px" }}
                        disabled={isFormDisabled}
                    >
                        {isSendingOtp ? <CircularProgress size={22} /> : "Send OTP"}
                    </Button>
                ) : (
                    <motion.div
                        key="login-button"
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, delay: 0.1 }}
                    >
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ py: "11px" }}
                            disabled={
                                isFormDisabled ||
                                isLocked ||
                                formik.values.otp.length !== OTP_LENGTH
                            }
                        >
                            {isVerifying ? <CircularProgress size={22} /> : "Login"}
                        </Button>
                    </motion.div>
                )}
            </form>
        </div>
    );
};

export default SellerLoginForm;





// import { useState, useEffect } from 'react';
// import { Button, TextField, InputAdornment, CircularProgress } from "@mui/material";
// import { useFormik } from "formik";
// import { useAppDispatch } from "../../../State/store";
// import { sellerLogin } from "../../../State/seller/sellerAuthSlice";
// import { sendLoginSignupOtp } from "../../../State/authSlice";
// import { toast } from "react-toastify";
// import { useNavigate } from "react-router-dom";
// import * as Yup from "yup";
// import { motion, AnimatePresence } from "framer-motion";
// import { CheckCircle } from "lucide-react";
// import ResendOtpButton from "../../../component/common/ResendOtpButton";

// // ✅ Validation schemas
// const emailSchema = Yup.object({
//     email: Yup.string().email("Invalid email").required("Email is required"),
// });

// const loginSchema = Yup.object({
//     email: Yup.string().email("Invalid email").required("Email is required"),
//     otp: Yup.string().required("OTP is required"),
// });

// const SellerLoginForm = () => {
//     const dispatch = useAppDispatch();
//     const navigate = useNavigate();
//     const [isOtpSent, setIsOtpSent] = useState(false);
//     const [resendTimer, setResendTimer] = useState(0);
//     const [loading, setLoading] = useState(false);

//     const formik = useFormik({
//         initialValues: {
//             email: "",
//             otp: "",
//         },
//         validationSchema: isOtpSent ? loginSchema : emailSchema,
//         onSubmit: async (values) => {
//             try {
//                 const result = await dispatch(sellerLogin(values)).unwrap();
//                 toast.success("Logged in successfully!");
//                 navigate("/seller/dashboard");
//             } catch (error: any) {
//                 console.error("Login failed:", error);
//                 toast.error(
//                     typeof error === "string" ? error : error.message || "Login failed."
//                 );
//             }
//         },
//     });

//     const handleSendOtp = async () => {
//         setLoading(true);
//         try {
//             await emailSchema.validate({ email: formik.values.email });
//             await dispatch(sendLoginSignupOtp({
//                 email: formik.values.email,

//             })).unwrap();
//             toast.success("OTP sent to your email!");
//             setIsOtpSent(true);
//             setResendTimer(30);
//         } catch (error: any) {
//             if (error.name === "ValidationError") {
//                 formik.setFieldError("email", error.message);
//             } else {
//                 toast.error("Failed to send OTP.");
//             }
//         } finally {
//             setLoading(false);
//         }
//     };

//     // Countdown effect
//     useEffect(() => {
//         let timer: NodeJS.Timeout;
//         if (resendTimer > 0) {
//             timer = setTimeout(() => setResendTimer((prev) => prev - 1), 1000);
//         }
//         return () => clearTimeout(timer);
//     }, [resendTimer]);

//     return (
//         <div className="max-w-sm mx-auto p-6 space-y-6 border rounded shadow">
//             <h1 className="text-center font-bold text-2xl text-primary-color pb-4">
//                 Seller Login
//             </h1>

//             <form onSubmit={formik.handleSubmit} className="space-y-5">
//                 <TextField
//                     fullWidth
//                     name="email"
//                     label="Email"
//                     value={formik.values.email}
//                     onChange={formik.handleChange}
//                     onBlur={formik.handleBlur}
//                     error={formik.touched.email && Boolean(formik.errors.email)}
//                     helperText={formik.touched.email && formik.errors.email}
//                     InputProps={{
//                         endAdornment: (
//                             <InputAdornment position="end">
//                                 <AnimatePresence>
//                                     {isOtpSent && (
//                                         <motion.div
//                                             key="success-icon"
//                                             initial={{ scale: 0, opacity: 0 }}
//                                             animate={{ scale: 1, opacity: 1 }}
//                                             exit={{ scale: 0, opacity: 0 }}
//                                             transition={{ type: "spring", stiffness: 300, damping: 15 }}
//                                         >
//                                             <CheckCircle className="text-green-500" size={22} />
//                                         </motion.div>
//                                     )}
//                                 </AnimatePresence>
//                             </InputAdornment>
//                         ),
//                     }}
//                 />

//                 <AnimatePresence>
//                     {isOtpSent && (
//                         <motion.div
//                             key="otp-field"
//                             initial={{ opacity: 0, y: -10 }}
//                             animate={{ opacity: 1, y: 0 }}
//                             exit={{ opacity: 0, y: -10 }}
//                             transition={{ duration: 0.3 }}
//                             className="space-y-2"
//                         >
//                             <p className="font-medium text-sm opacity-60">
//                                 Enter OTP sent to your email
//                             </p>
//                             <TextField
//                                 fullWidth
//                                 name="otp"
//                                 label="OTP"
//                                 value={formik.values.otp}
//                                 onChange={formik.handleChange}
//                                 onBlur={formik.handleBlur}
//                                 error={formik.touched.otp && Boolean(formik.errors.otp)}
//                                 helperText={formik.touched.otp && formik.errors.otp}
//                             />

//                             {/* Resend OTP button */}
//                             <div className="flex items-center justify-between text-sm pt-1">
//                                 <span className="opacity-60">Didn't get it?</span>
//                                 <ResendOtpButton
//                                     onResend={handleSendOtp}
//                                     cooldown={60}
//                                     resendText="Send Again"
//                                     resendPrefixText="Retry in"
//                                     resendSuffixText=" sec"
//                                     buttonProps={{
//                                         color: "primary",
//                                         variant: "text",
//                                         size: "small",
//                                     }}
//                                 />

//                             </div>
//                         </motion.div>
//                     )}
//                 </AnimatePresence>

//                 {!isOtpSent ? (
//                     <Button
//                         onClick={handleSendOtp}
//                         fullWidth
//                         variant="outlined"
//                         sx={{ py: "11px" }}
//                         disabled={loading}
//                     >
//                         {loading ? ( // 👈 show spinner while loading
//                             <CircularProgress size={22} />
//                         ) : (
//                             "Send OTP"
//                         )}
//                     </Button>
//                 ) : (
//                     <motion.div
//                         key="login-button"
//                         initial={{ opacity: 0, y: 10 }}
//                         animate={{ opacity: 1, y: 0 }}
//                         transition={{ duration: 0.3, delay: 0.1 }}
//                     >
//                         <Button
//                             type="submit"
//                             fullWidth
//                             variant="contained"
//                             sx={{ py: "11px" }}
//                         >
//                             Login
//                         </Button>
//                     </motion.div>
//                 )}
//             </form>
//         </div>
//     );
// };

// export default SellerLoginForm;
