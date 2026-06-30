import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../config/Api";
import { SellerAuthState, SellerLoginRequest, SellerLoginResponse } from "../../types/sellerType";




// Async thunk
export const sellerLogin = createAsyncThunk<
    SellerLoginResponse,
    SellerLoginRequest,
    { rejectValue: string }
>(
    "sellerAuth/sellerLogin",
    async (loginRequest, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/signing", {
                email: `seller_${loginRequest.email}`,
                otp: loginRequest.otp,
            });

            const jwt = response.data.jwt;
            localStorage.setItem("jwt", jwt);

            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || "Login failed");
        }
    }
);



// State
 const initialState: SellerAuthState = {
    seller: null,
    loading: false,
    error: null,
};

// Slice
 const sellerAuthSlice = createSlice({
    name: "sellerAuth",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(sellerLogin.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(sellerLogin.fulfilled, (state, action) => {
                state.loading = false;
                state.seller = action.payload.seller;
                state.error = null;
            })
            .addCase(sellerLogin.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || "Login failed";
            });
    },
});

export default sellerAuthSlice.reducer;
