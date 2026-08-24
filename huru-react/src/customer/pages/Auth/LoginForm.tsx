import { useState, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../../../State/store';
import { useFormik } from 'formik';
import { Button, CircularProgress, TextField, InputAdornment, Divider, Typography } from '@mui/material';
import { getRoleFromToken } from '../../../Util/jwtHelpers';
import { fetchUserProfile, sendLoginSignupOtp, signin } from '../../../State/authSlice';
import { useNavigate, useSearchParams } from 'react-router-dom';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle } from 'lucide-react';
import ResendOtpButton from '../../../component/common/ResendOtpButton';
import GoogleAuthButton from './GoogleAuthButton';

const emailSchema = Yup.object({
    email: Yup.string().email('Invalid email format').required('Email is required'),
});

const loginSchema = Yup.object({
    email: Yup.string().email('Invalid email format').required('Email is required'),
    otp: Yup.string().required('OTP is required'),
});

// Maps the typed error codes sent by OAuth2AuthenticationFailureHandler
// (backend) to a user-facing message. Keep in sync with
// extractErrorCode() in that class — a new backend error code that
// isn't added here silently falls through to the generic message,
// which is safe but less helpful than it could be.
const OAUTH_ERROR_MESSAGES: Record<string, string> = {
    unverified_email: 'Your Google account email is not verified. Please verify it with Google first.',
    non_customer_account: 'This email is registered as a seller or admin account. Please sign in with OTP instead.',
    missing_email: "Google didn't share an email address with us. Please try a different Google account.",
    missing_google_id: 'Google sign-in failed to return a valid account identifier. Please try again.',
    provider_mismatch: 'This account uses a different sign-in method.',
    user_not_found: "We couldn't find your account after signing in with Google. Please try again.",
    oauth2_error: 'Google sign-in failed. Please try again or use OTP login.',
};

const LoginForm = () => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const { auth } = useAppSelector((store) => store);
    const [searchParams, setSearchParams] = useSearchParams();

    const [isOtpSent, setIsOtpSent] = useState(false);
    const [resendTimer, setResendTimer] = useState(0);

    const formik = useFormik({
        initialValues: {
            email: '',
            otp: '',
        },
        validationSchema: isOtpSent ? loginSchema : emailSchema,
        onSubmit: async (values) => {
            try {
                const result = await dispatch(signin({ ...values, role: 'CUSTOMER' })).unwrap();
                toast.success('Logged in successfully!');
            } catch (error: any) {
                console.error('Login failed:', error);
                toast.error(error?.message || 'Login failed.');
            }
        },
    });

    const handleSendOtp = async () => {
        try {
            await emailSchema.validate({ email: formik.values.email });
            await dispatch(sendLoginSignupOtp({ email: formik.values.email })).unwrap();
            toast.success('OTP sent to your email!');
            setIsOtpSent(true);
            setResendTimer(60);
        } catch (error: any) {
            if (error.name === 'ValidationError') {
                formik.setFieldError('email', error.message);
            } else {
                toast.error(error?.message || 'Failed to send OTP.');
            }
        }
    };

    useEffect(() => {
        let timer: NodeJS.Timeout;
        if (resendTimer > 0) {
            timer = setTimeout(() => setResendTimer((prev) => prev - 1), 1000);
        }
        return () => clearTimeout(timer);
    }, [resendTimer]);

    useEffect(() => {
        if (auth.jwt) {
            const role = getRoleFromToken(auth.jwt);
            if (role === 'CUSTOMER') {
                dispatch(fetchUserProfile(auth.jwt));
                navigate('/account');
            } else {
                navigate('/');
            }
        }
    }, [auth.jwt, dispatch, navigate]);

    // Handles ?error=<code> appended by OAuth2AuthenticationFailureHandler
    // when Google sign-in fails (unverified email, non-customer account,
    // etc.). Shows a mapped message, then strips the param so a page
    // refresh doesn't re-show the toast.
    useEffect(() => {
        const errorCode = searchParams.get('error');
        if (errorCode) {
            toast.error(OAUTH_ERROR_MESSAGES[errorCode] || OAUTH_ERROR_MESSAGES.oauth2_error);
            const next = new URLSearchParams(searchParams);
            next.delete('error');
            setSearchParams(next, { replace: true });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [searchParams]);

    return (
        <div className="max-w-sm mx-auto p-6 space-y-6 border rounded shadow">
            <h1 className="text-center font-bold text-2xl text-primary-color pb-4">User Login</h1>

            <GoogleAuthButton />

            <Divider>
                <Typography variant="body2" color="text.secondary">OR</Typography>
            </Divider>

            <form onSubmit={formik.handleSubmit} className="space-y-5">
                <TextField
                    fullWidth
                    name="email"
                    label="Email"
                    value={formik.values.email}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    error={formik.touched.email && Boolean(formik.errors.email)}
                    helperText={formik.touched.email && formik.errors.email}
                    InputProps={{
                        endAdornment: (
                            <InputAdornment position="end">
                                <AnimatePresence>
                                    {isOtpSent && (
                                        <motion.div
                                            key="success-icon"
                                            initial={{ scale: 0, opacity: 0 }}
                                            animate={{ scale: 1, opacity: 1 }}
                                            exit={{ scale: 0, opacity: 0 }}
                                            transition={{ type: 'spring', stiffness: 300, damping: 15 }}
                                        >
                                            <CheckCircle className="text-green-500" size={22} />
                                        </motion.div>
                                    )}
                                </AnimatePresence>
                            </InputAdornment>
                        ),
                    }}
                />

                <AnimatePresence>
                    {isOtpSent && (
                        <motion.div
                            key="otp-field"
                            initial={{ opacity: 0, y: -10 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -10 }}
                            transition={{ duration: 0.3 }}
                            className="space-y-2"
                        >
                            <p className="font-medium text-sm opacity-60">Enter OTP sent to your email</p>
                            <TextField
                                fullWidth
                                name="otp"
                                label="OTP"
                                value={formik.values.otp}
                                onChange={formik.handleChange}
                                onBlur={formik.handleBlur}
                                error={formik.touched.otp && Boolean(formik.errors.otp)}
                                helperText={formik.touched.otp && formik.errors.otp}
                            />

                            <div className="flex items-center justify-between text-sm pt-1">
                                <span className="opacity-60">Didn't get it?</span>
                                <ResendOtpButton
                                    onResend={handleSendOtp}
                                    cooldown={resendTimer}
                                    resendText="Send Again"
                                    resendPrefixText="Retry in"
                                    resendSuffixText=" sec"
                                    buttonProps={{
                                        color: 'primary',
                                        variant: 'text',
                                        size: 'small',
                                    }}
                                />
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>

                {!isOtpSent ? (
                    <Button onClick={handleSendOtp} fullWidth variant="outlined" sx={{ py: '11px' }}>
                        {auth.loading ? <CircularProgress size={24} /> : 'Send OTP'}
                    </Button>
                ) : (
                    <motion.div
                        key="login-button"
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, delay: 0.1 }}
                    >
                        <Button type="submit" fullWidth variant="contained" sx={{ py: '11px' }}>
                            Login
                        </Button>
                    </motion.div>
                )}
            </form>
        </div>
    );
};

export default LoginForm;
