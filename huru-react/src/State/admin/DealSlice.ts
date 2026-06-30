import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { ApiResponse, Deal } from "../../types/DealType";
import { api } from "../../config/Api";

const initialState: DealState = {
    deals: [],
    loading: false,
    error: null,
    dealCreated: false,
    dealUpdated: false,
};
interface DealState {
    deals: Deal[];
    loading: boolean;
    error: string | null;
    dealUpdated: boolean;
    dealCreated: boolean;
};

export const createDeal = createAsyncThunk(
    "deals/createDeal",
    async (deal: any, { rejectWithValue }) => {
        try {
            const response = await api.post("/admin/deals", deal, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("jwt")}`,
                },
            });
            console.log("Deal Created", response.data);
            return response.data
        } catch (error: any) {
            console.log("error", error.response)
            return rejectWithValue(error.response?.data?.message || 'Failed to create deal')
        }
    }
);

export const getAllDeals = createAsyncThunk(
    "deals/getAllDeals",
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get("/admin/deals", {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("jwt")}`,
                },
            });
            console.log("get All Deals", response.data);
            return response.data;
        } catch (error: any) {
            console.log("error", error.response)
            return rejectWithValue(error.response?.data?.message || 'Failed to fetch deals')
        }
    }
)

// create thunk for deleteing deal
export const deleteDeal = createAsyncThunk<ApiResponse, number>(
    "deals/deleteDeal",
    async (id: number, { rejectWithValue }) => {
        try {
            const response = await api.delete(`/admin/deals/${id}`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("jwt")}`,
                },
            });
            return response.data;
        } catch (error: any) {
            console.log("error", error.response);
            return rejectWithValue(
                error.response?.data?.message || "Failed to delete deal"
            )
        }
    }
);
// Update deal thunk
export const updateDeal = createAsyncThunk<
    ApiResponse, // return type of fulfilled action
    { id: number; updatedDeal: Partial<Deal> }, // arg type
    { rejectValue: string } // rejectWithValue type
>(
    "deals/updateDeal",
    async ({ id, updatedDeal }, { rejectWithValue }) => {
        try {
            const response = await api.put(`/admin/deals/${id}`, updatedDeal, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("jwt")}`,
                },
            });
            console.log("Deal Updated", response.data);
            return response.data;
        } catch (error: any) {
            console.log("error", error.response);
            return rejectWithValue(
                error.response?.data?.message || "Failed to update deal"
            );
        }
    }
);

// Slice
const dealSlice = createSlice({
    name: "deals",
    initialState,
    reducers: {
        resetDealFlags: (state) => {
            state.dealCreated = false;
            state.dealUpdated = false;
        },
    },
    extraReducers: (builder) => {
        builder
            // Create Deal
            .addCase(createDeal.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(createDeal.fulfilled, (state, action: PayloadAction<Deal>) => {
                state.loading = false;
                state.dealCreated = true;
                state.deals.push(action.payload);
            })
            .addCase(createDeal.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            // Get All Deals
            .addCase(getAllDeals.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getAllDeals.fulfilled, (state, action) => {
                state.loading = false;
                state.deals = action.payload;
            })
            .addCase(getAllDeals.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            // Delete Deal
            .addCase(deleteDeal.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(deleteDeal.fulfilled, (state, action) => {
                state.loading = false;
                state.deals = state.deals.filter(deal => deal.id !== action.meta.arg);
            })
            .addCase(deleteDeal.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            // Update Deal
            .addCase(updateDeal.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.dealUpdated = false
            })
            .addCase(updateDeal.fulfilled, (state, action) => {
                state.loading = false;
                state.dealUpdated = true;
                const index = state.deals.findIndex((deal) => deal.id === action.payload.data.id);
                if (index !== -1) {
                    state.deals[index] = action.payload.data;
                }
            })
            .addCase(updateDeal.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            });
    },
});

export const { resetDealFlags } = dealSlice.actions;
export default dealSlice.reducer;

