import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../config/Api";
import { SellerProfileFormValues } from "../../types/sellerProfileFormValuesType";
import { Seller } from "../../types/sellerType";

// NOTE: useAppDispatch / useAppSelector already live in store.ts and are
// exported from there. Re-exporting them here duplicated the export and
// created a needless circular import (this file -> store.ts -> this file).
// Import them from "../store" wherever you need them instead:
//   import { useAppDispatch, useAppSelector } from "../store";

export const ACCOUNT_STATUSES = [
    "PENDING_VERIFICATION",
    "ACTIVE",
    "SUSPENDED",
    "DEACTIVATED",
    "BANNED",
    "CLOSED",
] as const;

// Convenience literal union for UI code (dropdown options, color maps).
// The canonical field on Seller stays `accountStatus?: string` per
// sellerType.ts, since that's what the backend actually contracts to.
export type AccountStatus = typeof ACCOUNT_STATUSES[number];

export const fetchSellerProfile = createAsyncThunk("/sellers/fetchSellerProfile",
    async (jwt: string, { rejectWithValue }) => {
        try {
            const response = await api.get("/sellers/profile", {
                headers: {
                    Authorization: `Bearer ${jwt}`,
                },
            })
            console.log("fetch seller profile", response.data)
            return response.data;

        } catch (error: any) {
            console.error("Error fetching seller profile:", error);
            return rejectWithValue(error.response?.data || error.message);
        }
    }
)

// in your authSlice or sellerSlice
export const createSeller = createAsyncThunk<any, SellerProfileFormValues>(
    'seller/createSeller',
    async (payload, { rejectWithValue }) => {

        try {
            const response = await api.post('/sellers', payload)
            return response.data
        } catch (err: any) {
            console.error("API error in createSeller: ", err);
            return rejectWithValue(err.response?.data || err.message);
        }

    }
)

// --- Admin seller list + status management (added) ---
// Lives here because this is the slice already wired to the "seller" store
// key. If admin/AdminSlice.ts turns out to be the intended home for
// admin-facing seller management, move these three exports there instead.

export const fetchSellers = createAsyncThunk<
    Seller[],
    AccountStatus | "ALL" | undefined,
    { rejectValue: string }
>(
    "sellers/fetchSellers",
    async (statusFilter, { rejectWithValue }) => {
        try {
            const params = statusFilter && statusFilter !== "ALL" ? { status: statusFilter } : {};
            const response = await api.get("/api/admin/sellers", { params });
            return response.data;
        } catch (error: any) {
            // Generic message only — avoid leaking backend internals to the UI
            return rejectWithValue(error.response?.data?.message || "Failed to load sellers");
        }
    }
);

export const updateSellerStatus = createAsyncThunk<
    Seller,
    { sellerId: number; accountStatus: AccountStatus },
    { rejectValue: string }
>(
    "sellers/updateStatus",
    async ({ sellerId, accountStatus }, { rejectWithValue }) => {
        try {
            // Backend is PATCH /api/admin/sellers/{id}/status/{status} —
            // status is a path variable, not a body field. The previous
            // version called PUT /api/admin/seller/{id}/status (wrong verb,
            // singular "seller", no status segment, status in the body
            // instead) which matched no route on the backend at all — every
            // status change silently failed and fell through to `rejected`,
            // leaving the sellers list showing stale statuses forever even
            // though the reducer logic for a successful update was correct.
            const response = await api.patch(
                `/api/admin/sellers/${sellerId}/status/${accountStatus}`
            );
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || "Failed to update seller status");
        }
    }
);

interface SellerState {
    sellers: Seller[],
    selectedSeller: any,
    profile: any,
    report: any,
    loading: boolean,
    error: any,
    statusFilter: AccountStatus | "ALL",
    updatingId: number | null,
}

const initialState: SellerState = {
    sellers: [],
    selectedSeller: null,
    profile: null,
    report: null,
    loading: false,
    error: null,
    statusFilter: "ALL",
    updatingId: null,
}

const sellerSlice = createSlice({
    name: "seller",
    initialState,
    reducers: {
        setStatusFilter(state, action) {
            state.statusFilter = action.payload;
        },
    },
    extraReducers: (builder) => {
        builder.addCase(fetchSellerProfile.pending, (state) => {
            state.loading = true;
        })
        builder.addCase(fetchSellerProfile.fulfilled, (state, action) => {
            state.loading = false;
            state.profile = action.payload;
        })
        builder.addCase(fetchSellerProfile.rejected, (state, action) => {
            state.loading = false;
            state.error = action.payload;
        })
        builder.addCase(createSeller.pending, (state) => {
            state.loading = true;
        })
        builder.addCase(createSeller.fulfilled, (state, action) => {
            state.loading = false;
            state.sellers.push(action.payload);  // or set selectedSeller = action.payload
        })
        builder.addCase(createSeller.rejected, (state, action) => {
            state.loading = false;
            state.error = action.payload; // Ensure payload is a string
        })

        // --- added ---
        builder.addCase(fetchSellers.pending, (state) => {
            state.loading = true;
            state.error = null;
        })
        builder.addCase(fetchSellers.fulfilled, (state, action) => {
            state.loading = false;
            state.sellers = action.payload;
        })
        builder.addCase(fetchSellers.rejected, (state, action) => {
            state.loading = false;
            state.error = action.payload;
        })
        builder.addCase(updateSellerStatus.pending, (state, action) => {
            state.updatingId = action.meta.arg.sellerId;
            state.error = null;
        })
        builder.addCase(updateSellerStatus.fulfilled, (state, action) => {
            state.updatingId = null;
            const idx = state.sellers.findIndex((s) => s.id === action.payload.id);
            if (idx !== -1) state.sellers[idx] = action.payload;
        })
        builder.addCase(updateSellerStatus.rejected, (state, action) => {
            state.updatingId = null;
            state.error = action.payload;
        })
    }
})

export const { setStatusFilter } = sellerSlice.actions;
export default sellerSlice.reducer;