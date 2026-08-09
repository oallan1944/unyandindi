import { Box, Button, Grid, TextField } from '@mui/material'
import { useFormik } from 'formik'
import * as Yup from 'yup'
import { useAppDispatch } from '../../../State/store';
import { createOrder } from '../../../State/customer/orderSlice';

const AddressFormSchema = Yup.object().shape({
    name: Yup.string().required('Name is Required'),
    mobile: Yup.string()
        .required('Mobile number is Required')
        .matches(/^\d{10}$/, 'Invalid mobile number'), // any 10 digits
    pinCode: Yup.string()
        .required('Pin Code is Required')
        .matches(/^\d{5}$/, 'Invalid pin code'), // any 5 digits
    address: Yup.string().required('Address is Required'),
    city: Yup.string().required('City is Required'),
    state: Yup.string().required('State is Required'),
    locality: Yup.string().required('Locality is Required')
});

const AddressForm = ({ paymentGateway }: any) => {
    // create order function
    const dispatch = useAppDispatch();

    const formik = useFormik({
        initialValues: {
            name: '',
            mobile: '',
            pinCode: '',
            address: '',
            city: '',
            state: '',
            locality: ' '
        },
        validationSchema: AddressFormSchema,
        onSubmit: (values) => {
            // submit form
            console.log(values)
            dispatch(createOrder({
                address: values,
                jwt: localStorage.getItem("jwt") || "",
                paymentGateway,
            }))
        }
    })
    return (
        <Box sx={{ max: 'auto' }}>
            <p className='text-xl font-bold text-center pb-5'> Contact Details</p>
            <form onSubmit={formik.handleSubmit}>
                <Grid container spacing={4}>
                    <Grid size={{ xs: 12 }}>
                        <TextField
                            fullWidth
                            name='name'
                            label='Name'
                            value={formik.values.name}
                            onChange={formik.handleChange}
                            error={formik.touched.name && Boolean(formik.errors.name)}
                            helperText={formik.touched.name && formik.errors.name}
                        />
                    </Grid>
                    <Grid size={{ xs: 6 }}>
                        <TextField
                            fullWidth
                            name='mobile'
                            label='Mobile'
                            value={formik.values.mobile}
                            onChange={formik.handleChange}
                            error={formik.touched.mobile && Boolean(formik.errors.mobile)}
                            helperText={formik.touched.mobile && formik.errors.mobile}
                        />
                    </Grid>
                    <Grid size={{ xs: 6 }}>
                        <TextField
                            fullWidth
                            name='pinCode'
                            label='Pin Code'
                            value={formik.values.pinCode}
                            onChange={formik.handleChange}
                            error={formik.touched.pinCode && Boolean(formik.errors.pinCode)}
                            helperText={formik.touched.pinCode && formik.errors.pinCode}
                        />
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                        <TextField
                            fullWidth
                            name='address'
                            label='Address'
                            value={formik.values.address}
                            onChange={formik.handleChange}
                            error={formik.touched.address && Boolean(formik.errors.address)}
                            helperText={formik.touched.address && formik.errors.address}
                        />
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                        <TextField
                            fullWidth
                            name='locality'
                            label='Locality'
                            value={formik.values.locality}
                            onChange={formik.handleChange}
                            error={formik.touched.locality && Boolean(formik.errors.locality)}
                            helperText={formik.touched.locality && formik.errors.locality}
                        />
                    </Grid>
                    <Grid size={{ xs: 6 }}>
                        <TextField
                            fullWidth
                            name='city'
                            label='City'
                            value={formik.values.city}
                            onChange={formik.handleChange}
                            error={formik.touched.city && Boolean(formik.errors.city)}
                            helperText={formik.touched.city && formik.errors.city}
                        />
                    </Grid>
                    <Grid size={{ xs: 6 }}>
                        <TextField
                            fullWidth
                            name='state'
                            label='State'
                            value={formik.values.state}
                            onChange={formik.handleChange}
                            error={formik.touched.state && Boolean(formik.errors.state)}
                            helperText={formik.touched.state && formik.errors.state}
                        />
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                        <Button fullWidth type='submit' variant='contained' sx={{ py: "14px" }}>
                            Add Address
                        </Button>
                    </Grid>
                </Grid>
            </form>
        </Box >
    )
}

export default AddressForm
