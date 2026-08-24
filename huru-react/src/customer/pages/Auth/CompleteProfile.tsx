import { useEffect } from 'react';
import { Button, CircularProgress, TextField, Typography, Paper } from '@mui/material';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { toast } from 'react-toastify';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../../State/store';
import { completeUserProfile } from '../../../State/authSlice';
import { getToken } from '../../../Util/tokenStorage';

// Google gives us fullName already (from the Google profile), so this
// form only collects what Google can't provide: a mobile number.
// fullName is still editable in case the person wants to correct what
// Google sent, or Google returned nothing (CustomOAuth2UserService
// falls back to an empty string when userInfo.getFullName() is null).
const completeProfileSchema = Yup.object({
    fullName: Yup.string().trim().min(2, 'Name is too short').required('Name is required'),
    mobile: Yup.string()
        .trim()
        .matches(/^[0-9]{10}$/, 'Enter a valid 10-digit mobile number')
        .required('Mobile number is required'),
});

const CompleteProfile = () => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const [searchParams] = useSearchParams();
    const { auth } = useAppSelector((store) => store);

    const isNewUser = searchParams.get('newUser') === 'true';

    // Guard: this page requires an authenticated session. If someone
    // lands here with no token (direct URL visit, expired session),
    // send them to login instead of showing a form that will 401.
    useEffect(() => {
        const token = auth.jwt || getToken();
        if (!token) {
            navigate('/login');
        }
    }, [auth.jwt, navigate]);

    const formik = useFormik({
        initialValues: {
            fullName: auth.user?.fullName || '',
            mobile: '',
        },
        enableReinitialize: true,
        validationSchema: completeProfileSchema,
        onSubmit: async (values) => {
            try {
                await dispatch(completeUserProfile(values)).unwrap();
                toast.success('Profile completed!');
                navigate('/');
            } catch (error: any) {
                toast.error(error?.message || 'Failed to save profile. Please try again.');
            }
        },
    });

    return (
        <div className="flex justify-center items-center h-[95vh]">
            <Paper elevation={3} className="max-w-sm w-full p-8 space-y-6">
                <div className="text-center space-y-1">
                    <Typography variant="h5" fontWeight="bold">
                        {isNewUser ? 'Welcome!' : 'One more step'}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        We just need a bit more info to finish setting up your account.
                    </Typography>
                </div>

                <form onSubmit={formik.handleSubmit} className="space-y-5">
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

                    <TextField
                        fullWidth
                        name="mobile"
                        label="Mobile Number"
                        value={formik.values.mobile}
                        onChange={formik.handleChange}
                        onBlur={formik.handleBlur}
                        error={formik.touched.mobile && Boolean(formik.errors.mobile)}
                        helperText={formik.touched.mobile && formik.errors.mobile}
                    />

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        disabled={formik.isSubmitting}
                        sx={{ py: '11px' }}
                    >
                        {formik.isSubmitting ? <CircularProgress size={24} /> : 'Continue'}
                    </Button>
                </form>
            </Paper>
        </div>
    );
};

export default CompleteProfile;
