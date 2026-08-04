import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../config/Api";

interface AdminAuthState {
    jwt: string | null;
    role: string | null;
    isLoggedIn: boolean;
    loading: boolean;
    error: string | null;
    otpSent: boolean;
}

const initialState: AdminAuthState = {
    jwt: localStorage.getItem("admin_jwt"),
    role: localStorage.getItem("admin_role"),
    isLoggedIn: !!localStorage.getItem("admin_jwt"),
    loading: false,
    error: null,
    otpSent: false,
};

export const sendAdminLoginOtp = createAsyncThunk(
    "adminAuth/sendOtp",
    async ({ email }: { email: string }, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/sent/login-signup-otp", { email });
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || "Failed to send OTP");
        }
    }
);

export const adminSignin = createAsyncThunk(
    "adminAuth/signin",
    async ({ email, otp }: { email: string; otp: string }, { rejectWithValue }) => {
        try {
            const response = await api.post("/api/admin/login", { email, otp });
            localStorage.setItem("admin_jwt", response.data.jwt);
            localStorage.setItem("admin_role", response.data.role);
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || "Admin login failed");
        }
    }
);

export const adminLogout = createAsyncThunk("adminAuth/logout", async (navigate: any) => {
    localStorage.removeItem("admin_jwt");
    localStorage.removeItem("admin_role");
    navigate("/admin/login");
});

const adminAuthSlice = createSlice({
    name: "adminAuth",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(sendAdminLoginOtp.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(sendAdminLoginOtp.fulfilled, (state) => { state.loading = false; state.otpSent = true; })
            .addCase(sendAdminLoginOtp.rejected, (state, action) => { state.loading = false; state.error = action.payload as string; })

            .addCase(adminSignin.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(adminSignin.fulfilled, (state, action) => {
                state.loading = false;
                state.isLoggedIn = true;
                state.jwt = action.payload.jwt;
                state.role = action.payload.role;
            })
            .addCase(adminSignin.rejected, (state, action) => { state.loading = false; state.error = action.payload as string; })

            .addCase(adminLogout.fulfilled, (state) => {
                state.isLoggedIn = false;
                state.jwt = null;
                state.role = null;
                state.otpSent = false;
            });
    },
});

export default adminAuthSlice.reducer;