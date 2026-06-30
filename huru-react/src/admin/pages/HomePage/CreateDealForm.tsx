import { Box, Button, FormControl, InputLabel, MenuItem, Select, TextField, Typography } from '@mui/material'
import { useFormik } from 'formik'
import React from 'react'
import { useAppDispatch, useAppSelector } from '../../../State/store';
import { createDeal } from '../../../State/admin/DealSlice';

const CreateDealForm = () => {
    const dispatch = useAppDispatch();
    const { customer } = useAppSelector(store => store);
    const formik = useFormik({
        initialValues: {
            discount: 0,
            category: ""
        },
        onSubmit: (values) => {
            console.log('submit', values)
            const reqData = {
                discount: values.discount,
                category: {
                    id: values.category
                }
            }
            dispatch(createDeal(reqData))
        }
    })
    return (
        <Box component={"form"} onSubmit={formik.handleSubmit}
            className='space-y-6'
        >
            <Typography variant='h4' className='text-center text-primary-color'>
                Create Deal
            </Typography>
            <TextField
                fullWidth
                name='discount'
                label='Discount'
                value={formik.values.discount}
                onChange={formik.handleChange}
                error={formik.touched.discount && Boolean(formik.errors.discount)}
                helperText={formik.touched.discount && formik.errors.discount}
            />
            <FormControl fullWidth>
                <InputLabel id="demo-simple-select-label">Category</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="demo-simple-select"
                    value={formik.values.category}
                    label="Category"
                    onChange={formik.handleChange}
                >
                    {customer.homePageData?.dealCategories.map((item) =>
                        <MenuItem value={item.id}>{item.name}</MenuItem>)
                    }

                </Select>
            </FormControl>
            <Button sx={{ py: ".9rem" }} fullWidth type='submit' variant='contained'>Create Deal</Button>

        </Box>
    )
}

export default CreateDealForm
