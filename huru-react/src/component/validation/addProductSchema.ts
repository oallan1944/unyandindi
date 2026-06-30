import * as Yup from 'yup';

export const addProductSchema = Yup.object({
    title: Yup.string().required("Product title is required"),
    description: Yup.string().required("Product description is required"),
    mrpPrice: Yup.number()
        .typeError("MRP Price must be a number")
        .positive("MRP Price must be positive")
        .required("MRP Price is required"),
    sellingPrice: Yup.number()
        .typeError("Selling Price must be a number")
        .positive("Selling Price must be positive")
        .max(Yup.ref("mrpPrice"), "Selling Price cannot exceed MRP Price")
        .required("Selling Price is required"),
    quantity: Yup.number()
        .typeError("Quantity must be a number")
        .integer("Quantity must be a whole number")
        .positive("Quantity must be positive")
        .required("Quantity is required"),
    color: Yup.array()
        .of(
            Yup.object({
                name: Yup.string().required("Color name is required"),
                hex: Yup.string()
                    .matches(/^#([0-9A-F]{3}){1,2}$/i, "Invalid color hex code")
                    .required("Color hex is required"),
            })
        )
        .min(1, "At least one color is required")
        .required("Color is required"),

    images: Yup.array()
        .of(Yup.string().url("Each image must be a valid URL"))
        .min(1, "At least one product image is required"),
    category: Yup.string().required("Main Category is required"),
    category2: Yup.string().required("Second Category is required"),
    category3: Yup.string().required("Third Category is required"),

    sizes: Yup.array()
        .of(Yup.string().required("Size cannot be empty"))
        .when('category', {
            is: (category: string) =>
                category === 'men' || category === 'women' || category === 'kids',
            then: (schema) =>
                schema.min(1, "At least one size is required for this category"),
            otherwise: (schema) => schema.notRequired(),
        }),
});
