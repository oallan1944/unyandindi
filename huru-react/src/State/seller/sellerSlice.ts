import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../config/Api";
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux'

import { AppDispatch, RootState } from "../store";
import { SellerProfileFormValues } from "../../types/sellerProfileFormValuesType";

// const { loading, error } = useAppSelector(state => state.sellers);

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;


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

interface SellerState {
    sellers: any[],
    selectedSeller: any,
    profile: any,
    report: any,
    loading: boolean,
    error: any,

}

const initialState: SellerState = {
    sellers: [],
    selectedSeller: null,
    profile: null,
    report: null,
    loading: false,
    error: null,
}

const sellerSlice = createSlice({
    name: "sellers",
    initialState,
    reducers: {},
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
    }
})

export default sellerSlice.reducer;