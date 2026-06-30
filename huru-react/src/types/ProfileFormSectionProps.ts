import { ChangeEvent } from "react";

import { FormikErrors, FormikTouched } from "formik";
import { SellerProfileFormValues } from "./sellerProfileFormValuesType";

export interface ProfileFormSectionProps {
    values: SellerProfileFormValues;
    handleChange: (e: ChangeEvent<any>) => void;
    setFieldValue: (field: string, value: any, shouldValidate?: boolean) => void;
    errors: FormikErrors<SellerProfileFormValues>;
    touched: FormikTouched<SellerProfileFormValues>;
    disabled: boolean;
}
