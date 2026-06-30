import * as Yup from "yup";

export const profileValidationSchema = Yup.object().shape({
    sellerName: Yup.string().required("Name is required"),
    email: Yup.string().email("Invalid email").required("Email is required"),
    mobile: Yup.string()
        .matches(/^[0-9]{10}$/, "Must be a 10 digit number")
        .required("Mobile is required"),
    GSTIN: Yup.string()
        .matches(/^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$/, "Invalid GSTIN")
        .required("GSTIN is required"),

    businessDetails: Yup.object().shape({
        businessName: Yup.string().required("Business Name is required"),
    }),

    bankDetails: Yup.object().shape({
        accountHolderName: Yup.string().required("Account Holder Name is required"),
        accountNumber: Yup.string()
            .matches(/^[0-9]{9,18}$/, "Account Number must be 9-18 digits")
            .required("Account Number is required"),
        ifscCode: Yup.string()
            .matches(/^[A-Z]{4}0[A-Z0-9]{6}$/, "Invalid IFSC code")
            .required("IFSC Code is required"),
    }),

    pickupAddress: Yup.object().shape({
       // address: Yup.string().required("Address is required"),
        city: Yup.string().required("City is required"),
        state: Yup.string().required("State is required"),
        mobile: Yup.string()
            .matches(/^[0-9]{10}$/, "Mobile must be 10 digits")
            .required("Pickup mobile is required"),
        pinCode: Yup.string()
            .matches(/^[0-9]{5}$/, "Pin Code must be 6 digits")
            .required("Pin Code is required"),
    }),
});
