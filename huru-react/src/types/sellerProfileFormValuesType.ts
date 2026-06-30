export interface SellerProfileFormValues {
   id?: string;
  sellerName: string;
  mobile: string;
  email: string;
  password: string;
  GSTIN?: string;
  businessDetails: {
    businessName: string;
    businessType: string;
    businessAddress: string;
    businessPhone: string;
  };
  bankDetails: {
    bankName: string;
    accountNumber: string;
    ifscCode: string;
    branch: string;
  };
  pickupAddress: {
    street: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  };
}
