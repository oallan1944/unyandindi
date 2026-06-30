import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { Coupon } from "../../types/couponType";
import { api } from "../../config/Api";

// API URL base
const API_URL = "/api/coupons";

// State type
interface CouponState {
    coupons: Coupon[];
    loading: boolean;
    error: string | null;
    couponCreated: boolean;
    couponUpdated: boolean;
}

// Initial state
const initialState: CouponState = {
    coupons: [],
    loading: false,
    error: null,
    couponCreated: false,
    couponUpdated: false,
};

// Async thunks

// Create Coupon
export const createCoupon = createAsyncThunk<
    Coupon,
    { coupon: any; jwt: string },
    { rejectValue: string }
>(
    "coupon/createCoupon",
    async ({ coupon, jwt }, { rejectWithValue }) => {
        try {
            const response = await api.post(`${API_URL}/admin/create`, coupon, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            console.log("Created coupon", response.data);
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data || "Failed to create coupon");
        }
    }
);

// Get All Coupons
export const getAllCoupons = createAsyncThunk<
    Coupon[],
    string,
    { rejectValue: string }
>(
    "coupon/getAllCoupons",
    async (jwt, { rejectWithValue }) => {
        try {
            const response = await api.get(`${API_URL}/admin/all`, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            console.log("Fetched coupons", response.data);
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data || "Failed to fetch coupons");
        }
    }
);

// Update Coupon
export const updateCoupon = createAsyncThunk<
    Coupon,
    { id: number; updatedCoupon: Partial<Coupon>; jwt: string },
    { rejectValue: string }
>(
    "coupon/updateCoupon",
    async ({ id, updatedCoupon, jwt }, { rejectWithValue }) => {
        try {
            const response = await api.put(`${API_URL}/admin/${id}`, updatedCoupon, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            console.log("Updated coupon", response.data);
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data || "Failed to update coupon");
        }
    }
);

// Delete Coupon
export const deleteCoupon = createAsyncThunk<
    number, // return the id on success
    { id: number; jwt: string },
    { rejectValue: string }
>(
    "coupon/deleteCoupon",
    async ({ id, jwt }, { rejectWithValue }) => {
        try {
            await api.delete(`${API_URL}/admin/${id}`, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            console.log("Deleted coupon", id);
            return id;
        } catch (error: any) {
            return rejectWithValue(error.response?.data || "Failed to delete coupon");
        }
    }
);

// Slice
const adminCouponSlice = createSlice({
    name: "coupons",
    initialState,
    reducers: {
        resetCouponFlags: (state) => {
            state.couponCreated = false;
            state.couponUpdated = false;
        },
    },
    extraReducers: (builder) => {
        builder
            // Create Coupon
            .addCase(createCoupon.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(createCoupon.fulfilled, (state, action) => {
                state.loading = false;
                state.couponCreated = true;
                state.coupons.push(action.payload);
            })
            .addCase(createCoupon.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            // Get All Coupons
            .addCase(getAllCoupons.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getAllCoupons.fulfilled, (state, action) => {
                state.loading = false;
                state.coupons = action.payload;
            })
            .addCase(getAllCoupons.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            // Update Coupon
            .addCase(updateCoupon.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(updateCoupon.fulfilled, (state, action) => {
                state.loading = false;
                state.couponUpdated = true;
                const index = state.coupons.findIndex(coupon => coupon.id === action.payload.id);
                if (index !== -1) {
                    state.coupons[index] = action.payload;
                }
            })
            .addCase(updateCoupon.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            // Delete Coupon
            .addCase(deleteCoupon.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(deleteCoupon.fulfilled, (state, action) => {
                state.loading = false;
                state.coupons = state.coupons.filter(coupon => coupon.id !== action.payload);
            })
            .addCase(deleteCoupon.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            });
    },
});

export const { resetCouponFlags } = adminCouponSlice.actions;
export default adminCouponSlice.reducer;
