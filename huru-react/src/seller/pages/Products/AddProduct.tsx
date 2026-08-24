import { useState } from 'react';
import { menLevelTwo } from '../../../Data/Category/Level two/menLevelTwo'
import { womenLevelTwo } from '../../../Data/Category/Level two/womenLevelTwo'
import { furnitureLevelTwo } from '../../../Data/Category/Level two/furnitureLevelTwo'
import { electronicsLevelTwo } from '../../../Data/Category/Level two/electronicsLevelTwo'
import { menLevelThree } from '../../../Data/Category/Level three/menLevelThree'
import { womenLevelThree } from '../../../Data/Category/Level three/womenLevelThree'
import { FurnitureLevelThree } from '../../../Data/Category/Level three/furnitureLevelThree'
import { electronicsLevelThree } from '../../../Data/Category/Level three/electronicsLevelThree'
import { useFormik } from 'formik'
import { uploadToCloudnary } from '../../../Util/UploadToCloudnary'
import { Alert, Button, CircularProgress, FormControl, FormHelperText, Grid, IconButton, InputLabel, MenuItem, Select, Snackbar, TextField } from '@mui/material'
import { AddPhotoAlternate, Close } from '@mui/icons-material'
import { colors } from '../../../Data/Filter/color'
import { mainCategory } from '../../../Data/Category/mainCategory'
import { useAppDispatch } from '../../../State/store'
import { createProduct } from '../../../State/seller/sellerProductSlice'
import { Autocomplete, Chip } from '@mui/material';
import { addProductSchema } from '../../../component/validation/addProductSchema'
import { AddProductForm } from '../../../types/addProductFormType'
import { getToken } from '../../../Util/tokenStorage'



// type ProductFormValues = {
//     title: string;
//     description: string;
//     mrpPrice: string;
//     sellingPrice: string;
//     quantity: string;
//     //color: string[];
//     color: { name: string; hex: string }[];  // <-- specify color shape here
//     images: string[];
//     category: string;
//     category2: string;
//     category3: string;
//     sizes: string[];
// };



const categoryTwo: { [key: string]: any } = {
    men: menLevelTwo,
    women: womenLevelTwo,
    kids: [],
    home_furniture: furnitureLevelTwo,
    beauty: [],
    electronics: electronicsLevelTwo,
};
const categoryThree: { [key: string]: any } = {
    men: menLevelThree,
    women: womenLevelThree,
    kids: [],
    home_furniture: FurnitureLevelThree,
    beauty: [],
    electronics: electronicsLevelThree,
};

// const childCategory = (category: any, parentCategoryId: any) => {
//     return category.filter((child: any) =>
//         child.parentCategoryId === parentCategoryId);
// };


const AddProduct = () => {
    const [uploadImage, setUploadingImage] = useState(false);
    // const [snackbarOpen, setOpenSnackbar] = useState(false);
    const [notification, setNotification] = useState<{ open: boolean, message: string, severity: 'success' | 'error' }>({
        open: false,
        message: '',
        severity: 'success'
    });
    const dispatch = useAppDispatch()

    const formik = useFormik<AddProductForm>({
        initialValues: {
            title: "",
            description: "",
            mrpPrice: 0,
            sellingPrice: 0,
            quantity: 0,
            color: [],
            images: [],
            category: "",
            category2: "",
            category3: "",
            sizes: []
        },
        validationSchema: addProductSchema,
        onSubmit: async (values, { setSubmitting, resetForm }) => {
            console.log(values)
            try {
                const requestPayload = {
                    ...values,
                    color: values.color.map(c => c.name).join(',')
                };
                await dispatch(createProduct({
                    request: requestPayload,
                    jwt: getToken()
                }));
                setNotification({
                    open: true,
                    message: "Product added successfully!",
                    severity: 'success'
                });
                resetForm();
            } catch (error: any) {
                console.error("Product creation failed", error);
                setNotification({
                    open: true,
                    message: error.message || "Failed to add product.",
                    severity: 'error'
                });
            } finally {
                setSubmitting(false);
            }

        },
    });

    const handleImageChange = async (event: any) => {
        const file = event.target.files[0];
        if (!file) return;
        setUploadingImage(true);
        try {

            const image = await uploadToCloudnary(file);
            formik.setFieldValue("images", [...formik.values.images, image]);
        } catch (error: any) {
            console.error("Image upload failed", error);
            setNotification({ open: true, message: error.message || "Failed to upload image.", severity: 'error' });
        } finally {
            setUploadingImage(false);
        }
    };

    const handleRemoveImage = (index: number) => {
        const updatedImages = [...formik.values.images];
        updatedImages.splice(index, 1);
        formik.setFieldValue("images", updatedImages);
    };

    const childCategory = (category: any, parentCategoryId: any) => {
        return category.filter((child: any) => child.parentCategoryId === parentCategoryId

        );
    };

    // const handleCloseSnakbar = () => setOpenSnackbar(false);

    const handleCloseNotification = () => {
        setNotification(prev => ({ ...prev, open: false }));
    };

    return (
        <div>
            <form onSubmit={formik.handleSubmit} className='space-y-4 p-4'>
                <Grid container spacing={2}>
                    <Grid className="flex flex-wrap gap-5" size={{ xs: 12 }}>
                        <input
                            type='file'
                            accept='image/*'
                            id='fileInput'
                            style={{ display: "none" }}
                            onChange={handleImageChange} />

                        <label className='relative' htmlFor='fileInput'>
                            <span className='w-24 h-24 cursor-pointer flex items-center
                            justify-center p-3 border rounded-md border-gray-400'>
                                <AddPhotoAlternate className='text-gray-700' />
                            </span>
                            {uploadImage && (
                                <div className='absolute left-0 right-0 top-0 bottom-0 w-24 flex
                                justify-center items-center'>
                                    <CircularProgress />
                                </div>
                            )}
                        </label>
                        <div className='flex flex-wrap gap-2'>
                            {formik.values.images.map((image, index) => (
                                <div className='relative'>
                                    <img className='w-24 h-24 object-cover'
                                        key={index}
                                        src={image}
                                        alt={`ProductImage ${index + 1}`} />

                                    <IconButton
                                        onClick={() => handleRemoveImage(index)}
                                        className=''
                                        size='small'
                                        color='error'
                                        sx={{
                                            position: "absolute",
                                            top: 0,
                                            right: 0,
                                            outline: "none",
                                        }}
                                    >
                                        <Close sx={{ fontSize: '1rem' }} />
                                    </IconButton>
                                </div>
                            ))}
                        </div>
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                        <TextField
                            fullWidth
                            id='title'
                            name='title'
                            label='Title'
                            value={formik.values.title}
                            onChange={formik.handleChange}
                            error={formik.touched.title && Boolean(formik.errors.title)}
                            helperText={formik.touched.title && formik.errors.title}
                            required />
                    </Grid>
                    <Grid size={{ xs: 12 }} >
                        <TextField
                            multiline
                            rows={4}
                            fullWidth
                            id='description'
                            name='description'
                            label='Description'
                            value={formik.values.description}
                            onChange={formik.handleChange}
                            error={formik.touched.description && Boolean(formik.errors.description)}
                            helperText={formik.touched.description && formik.errors.description}
                            required />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4, lg: 3 }} >
                        <TextField
                            fullWidth
                            id='mrp_price'
                            name='mrpPrice'
                            label='MRP Price'
                            type='number'
                            value={formik.values.mrpPrice}
                            onChange={formik.handleChange}
                            error={formik.touched.mrpPrice && Boolean(formik.errors.mrpPrice)}
                            helperText={formik.touched.mrpPrice && formik.errors.mrpPrice}
                            required />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4, lg: 3 }} >
                        <TextField
                            fullWidth
                            id='sellingPrice'
                            name='sellingPrice'
                            label='Selling Price'
                            type='number'
                            value={formik.values.sellingPrice}
                            onChange={formik.handleChange}
                            error={formik.touched.sellingPrice && Boolean(formik.errors.sellingPrice)}
                            helperText={formik.touched.sellingPrice && formik.errors.sellingPrice}
                            required />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4, lg: 3 }}>
                        <TextField
                            fullWidth
                            id="quantity"
                            name="quantity"
                            label="Quantity"
                            type="number"
                            value={formik.values.quantity}
                            onChange={formik.handleChange}
                            error={formik.touched.quantity && Boolean(formik.errors.quantity)}
                            helperText={formik.touched.quantity && formik.errors.quantity}
                            required
                        />
                    </Grid>

                    <Grid size={{ xs: 12, md: 4, lg: 3 }}>
                        <FormControl
                            fullWidth
                            error={formik.touched.color && Boolean(formik.errors.color)}
                            required
                        >
                            <Autocomplete
                                multiple
                                id="color"
                                options={colors}
                                getOptionLabel={(option) => option.name}
                                value={formik.values.color}
                                onChange={(event, newValue) => {
                                    formik.setFieldValue("color", newValue);
                                }}
                                renderTags={(value, getTagProps) =>
                                    value.map((option, index) => (
                                        <Chip
                                            // key={option.name}
                                            label={option.name}
                                            avatar={
                                                <span
                                                    style={{
                                                        backgroundColor: option.hex,
                                                        border: option.name === "white" ? "1px solid #ccc" : "none",
                                                        width: 16,
                                                        height: 16,
                                                        borderRadius: "50%",
                                                        display: "inline-block"
                                                    }}
                                                />
                                            }
                                            {...getTagProps({ index })}
                                        />
                                    ))
                                }
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        variant="outlined"
                                        label="Colors"
                                        placeholder="Choose colors"
                                        error={formik.touched.color && Boolean(formik.errors.color)}
                                        helperText={
                                            formik.touched.color && typeof formik.errors.color === 'string'
                                                ? formik.errors.color
                                                : undefined
                                        }
                                    />
                                )}
                            />
                        </FormControl>

                    </Grid>
                    <Grid size={{ xs: 12, md: 4, lg: 3 }}>
                        <FormControl
                            fullWidth
                            error={formik.touched.sizes && Boolean(formik.errors.sizes)}
                            required
                        >
                            {/* <InputLabel id='sizes-label'>Sizes</InputLabel> */}


                            <Autocomplete
                                multiple
                                disabled={!['men', 'women', 'kids'].includes(formik.values.category)}
                                freeSolo
                                id="sizes"
                                options={[]} // no predefined options
                                value={formik.values.sizes}
                                onChange={(event, newValue) => {
                                    formik.setFieldValue("sizes", newValue);
                                }}
                                renderTags={(value, getTagProps) =>
                                    value.map((option, index) => (
                                        <Chip
                                            variant="outlined"
                                            label={option}
                                            {...getTagProps({ index })}
                                            key={index}
                                        />
                                    ))
                                }
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        variant="outlined"
                                        label="Sizes"
                                        placeholder="Add size and press Enter"
                                        error={formik.touched.sizes && Boolean(formik.errors.sizes)}
                                        helperText={formik.touched.sizes && formik.errors.sizes}
                                    />
                                )}
                            />
                            {formik.touched.sizes && formik.errors.sizes && (
                                <FormHelperText>{formik.errors.sizes}</FormHelperText>
                            )}
                        </FormControl>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                        <FormControl
                            fullWidth
                            error={formik.touched.category && Boolean(formik.errors.category)}
                            required
                        >
                            <InputLabel id="category-label">Category</InputLabel>
                            <Select
                                labelId='category-label'
                                id='category'
                                name='category'
                                value={formik.values.category}
                                onChange={formik.handleChange}
                                label='Category'
                            >
                                {mainCategory.map((item) => (
                                    <MenuItem
                                        key={item.categoryId}
                                        value={item.categoryId}>
                                        {item.name}
                                    </MenuItem>
                                ))}

                            </Select>
                            <FormHelperText>
                                {formik.touched.category && formik.errors.category}
                            </FormHelperText>
                        </FormControl>
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                        <FormControl
                            fullWidth
                            error={formik.touched.category2 && Boolean(formik.errors.category2)}
                            required
                        >
                            <InputLabel id="category2-label">Second Category</InputLabel>
                            <Select
                                labelId='category2-label'
                                id='category2'
                                name='category2'
                                value={formik.values.category2}
                                onChange={formik.handleChange}
                                label='Second Category'
                            >
                                {formik.values.category &&
                                    categoryTwo[formik.values.category]?.map((item: any) => (
                                        <MenuItem
                                            key={item.categoryId}
                                            value={item.categoryId}>
                                            {item.name}
                                        </MenuItem>
                                    ))}
                            </Select>
                            <FormHelperText>
                                {formik.touched.category2 && formik.errors.category2}
                            </FormHelperText>
                        </FormControl>


                    </Grid>
                    <Grid size={{ xs: 12, md: 4, lg: 4 }}>
                        <FormControl
                            fullWidth
                            error={formik.touched.category3 && Boolean(formik.errors.category3)}
                            required
                        >
                            <InputLabel id="category-label">Third Category</InputLabel>
                            <Select
                                labelId='category-label'
                                id='category3'
                                name='category3'
                                value={formik.values.category3}
                                onChange={formik.handleChange}
                                label='Third Category'
                            >
                                <MenuItem value="">
                                    <em>None</em>
                                </MenuItem>
                                {formik.values.category2 &&
                                    childCategory(
                                        categoryThree[formik.values.category],
                                        formik.values.category2
                                    )?.map((item: any) => (
                                        <MenuItem
                                            key={item.categoryId}
                                            value={item.categoryId}>
                                            {item.name}
                                        </MenuItem>
                                    ))}
                            </Select >

                            <FormHelperText>
                                {formik.touched.category3 && formik.errors.category3}
                            </FormHelperText>

                        </FormControl>
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                        <Button
                            sx={{ p: "14px" }}
                            color='primary'
                            variant='contained'
                            fullWidth
                            type='submit'
                            disabled={formik.isSubmitting}
                        >
                            {formik.isSubmitting ? (
                                <CircularProgress size="1.5rem" />
                            ) : (
                                "Add Product"
                            )}
                        </Button>
                    </Grid>
                </Grid>
            </form>
            {/* Snackbar Notification */}
            <Snackbar open={notification.open} autoHideDuration={6000} onClose={handleCloseNotification} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
                <Alert onClose={handleCloseNotification} severity={notification.severity} sx={{ width: '100%' }}>
                    {notification.message}
                </Alert>
            </Snackbar>
        </div>
    )
}

export default AddProduct
