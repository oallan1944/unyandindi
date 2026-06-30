import { HomeCategory } from "./HomeCategoryType";

export interface Deal {
    id?: number;
    discount: string;
    category: HomeCategory;
}

export interface ApiResponse {
    message: string;
    status: boolean;
    data: any;
}
export interface DealState {
    deals: Deal[];
    loading: boolean;
    error: string | null;
    dealCreated: boolean,
    dealUpdated: boolean,
}