import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../config/Api";
import { User } from "../types/userTypes";

export const sendLoginSignupOtp = createAsyncThunk("/auth/sendLoginSignupOtp",
    async ({ email, }: { email: string, }, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/sent/login-signup-otp", {
                email,

            })
            console.log("login/ signup otp", response.data)
            return response.data
        } catch (error: any) {
            console.log("error...", error)
            return rejectWithValue(error.response?.data?.message || "failed to send OTP")
        }
    }
)

export const signin = createAsyncThunk<any, any>(
    "/auth/signing",
    async (loginRequest, { rejectWithValue }) => {
        try {
            // const email = loginRequest.isSeller
            //     ? `seller_${loginRequest.email}`
            //     : loginRequest.email;

            const response = await api.post("/auth/signing", {

                email: loginRequest.email,
                otp: loginRequest.otp,
                role: loginRequest.role
            });

            console.log("login otp", response.data);
            localStorage.setItem("jwt", response.data.jwt);
            return response.data.jwt;
        } catch (error: any) {
            console.log("error...", error);
            return rejectWithValue(error.response?.data?.message || "Login failed");
        }
    }
);


export const signup = createAsyncThunk<any, any>("/auth/signup",
    async (signupRequest, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/signup", signupRequest)
            console.log("login otp", response.data)
            localStorage.setItem("jwt", response.data.jwt)
            return response.data.jwt;
        } catch (error: any) {
            console.log("error...", error)
            return rejectWithValue(error.response?.data?.message || "Signup failed")
        }
    }
)

export const fetchUserProfile = createAsyncThunk<any, any>("/auth/fetchUserProfile",
    async ({ jwt }, { rejectWithValue }) => {
        try {
            const response = await api.get("/users/profile", {
                headers: {
                    Authorization: `Bearer ${jwt}`
                }
            })
            console.log("user profile", response.data)
            return response.data;
        } catch (error) {
            console.log("error...", error)
        }
    }
)

export const logout = createAsyncThunk<any, any>("/auth/logout",
    async (navigate, { rejectWithValue }) => {
        try {
            localStorage.clear()
            console.log("logged out successfully..")
            navigate("/")
        } catch (error) {
            console.log("error...", error)
        }
    }
)

interface AuthState {
    jwt: string | null,
    otpSent: boolean,
    isLoggedIn: boolean,
    user: User | null,
    loading: boolean,
    error: string | null,
    token: string | null
}

const initialState: AuthState = {
    jwt: null,
    otpSent: false,
    isLoggedIn: false,
    user: null,
    loading: false,
    error: null,
    token: null,
}

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {},
    extraReducers: (builder) => {

        builder.addCase(sendLoginSignupOtp.pending, (state) => {
            state.loading = true;
        })
        builder.addCase(sendLoginSignupOtp.fulfilled, (state) => {
            state.loading = false;
            state.otpSent = true;
        })
        builder.addCase(sendLoginSignupOtp.rejected, (state, action) => {
            state.loading = false;
            state.error = action.payload as string
        })

        builder.addCase(signin.fulfilled, (state, action) => {
            state.jwt = action.payload
            state.isLoggedIn = true
            state.token = action.payload
        })
        builder.addCase(signup.fulfilled, (state, action) => {
            state.jwt = action.payload
            state.isLoggedIn = true
        })
        builder.addCase(fetchUserProfile.fulfilled, (state, action) => {
            state.user = action.payload
            state.isLoggedIn = true
        })
        builder.addCase(logout.fulfilled, (state) => {
            state.jwt = null
            state.isLoggedIn = false
            state.user = null
        })
    }
})

export default authSlice.reducer;