
export interface SellerLoginRequest {
    email: string;
    otp: string;
}

export interface SellerLoginResponse {
    jwt: string;
    seller: Seller;
}

export interface SellerAuthState {
    seller: Seller | null;
    loading: boolean;
    error: string | null;
}


export interface PickUpAddress {
    name: string;
    mobile: string;
    pinCode: string;
    address: string;
    locality: string;
    city: string;
    state: string;
}
export interface BankDetails {
    accountNumber: string;
    ifscCode: string;
    accountHoldername: string;
}

export interface BusinessDetails {
    businessName: string;
}
export interface Seller {
    id?: number;
    mobile: string;
    otp: string;
    GSTIN: string;
    pickupAddress: PickUpAddress;
    bankDetails: BankDetails;
    sellerName: string;
    email: string;
    businessDetails: BusinessDetails;
    password: string;
    accountStatus?: string;
}
export interface SellerReport {
    id: number;
    seller: Seller;
    totalEarnings: number;
    totalSales: number;
    totalRefunds: number;
    totalTax: number;
    netEarnings: number;
    totalOrders: number;
    canceledOrders: number;
    totalTransactions: number;
}