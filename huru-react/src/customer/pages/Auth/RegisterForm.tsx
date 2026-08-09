import { useFormik } from 'formik'
import { useState } from 'react';
import { useAppDispatch } from '../../../State/store'
import { Button, TextField } from '@mui/material'
import { sendLoginSignupOtp, signup } from '../../../State/authSlice'

const RegisterForm = () => {
    const dispatch = useAppDispatch()
    const [otpSent, setOtpSent] = useState(false)

    const formik = useFormik({
        initialValues: {
            email: "",
            otp: "",
            fullName: "",
        },
        onSubmit: async (values) => {
            try {
                const res = await dispatch(signup({
                    email: values.email,
                    fullName: values.fullName,
                    otp: values.otp,
                })).unwrap()
                console.log("User registered, token: ", res.jwt)
                alert("Registeration Successful")
            } catch (err) {
                console.error(err)
                alert("Registeration failed...")
            }
            console.log("form data", values)

        }
    })

    const handleSendOtp = async () => {
        try {
            await dispatch(sendLoginSignupOtp({
                email: formik.values.email,
                
            })).unwrap()
            setOtpSent(true)
            alert("OTP sent Successfully")
        } catch (err) {
            console.error(err)
            alert("Failed to send OTP")
        }

    }
    return (
        <div>
            <h1 className='text-center font-bold text-xl text-primary-color pb-8'>SignUp</h1>
            <div className='space-y-3'>
                <TextField
                    fullWidth
                    name="email"
                    label="Email"
                    value={formik.values.email}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    error={formik.touched.email && Boolean(formik.errors.email)}
                    helperText={formik.touched.email && formik.errors.email}
                />
                {otpSent &&
                    <div className='space-y-3'>
                        <div className='space-y-2'>
                            <p className=' font-medium text-sm opacity-60'>
                                Enter OTP sent to your email
                            </p>
                            <TextField
                                fullWidth
                                name="otp"
                                label="Otp"
                                value={formik.values.otp}
                                onChange={formik.handleChange}
                                onBlur={formik.handleBlur}
                                error={formik.touched.otp && Boolean(formik.errors.otp)}
                                helperText={formik.touched.otp && formik.errors.otp}
                            />
                        </div>
                        <TextField
                            fullWidth
                            name="fullName"
                            label="Full Name"
                            value={formik.values.fullName}
                            onChange={formik.handleChange}
                            onBlur={formik.handleBlur}
                            error={formik.touched.fullName && Boolean(formik.errors.fullName)}
                            helperText={formik.touched.fullName && formik.errors.fullName}
                        />
                    </div>
                }

                {!otpSent && <Button onClick={handleSendOtp} fullWidth variant='contained' sx={{ py: "11px" }}>
                    Send otp
                </Button>}
                <Button
                    // onClick={handleSendOtp}
                    onClick={() => formik
                        .handleSubmit()

                    }
                    fullWidth variant='contained' sx={{ py: "11px" }}>
                    SignUp
                </Button>
            </div>
        </div>
    )
}

export default RegisterForm
