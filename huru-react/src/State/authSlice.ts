import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { api } from "../config/Api";
import { User } from "../types/userTypes";
import { getToken, setToken, clearToken } from "../Util/tokenStorage";

export const sendLoginSignupOtp = createAsyncThunk(
    "/auth/sendLoginSignupOtp",
    async (
        { email, role }: { email: string; role?: "ROLE_CUSTOMER" | "ROLE_SELLER" | "ROLE_ADMIN" },
        { rejectWithValue }
    ) => {
        try {
            const response = await api.post("/auth/sent/login-signup-otp", {
                email,
                role,
            })
            return response.data
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || "failed to send OTP")
        }
    }
)

export const signin = createAsyncThunk<any, any>(
    "/auth/signing",
    async (loginRequest, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/signing", {

                email: loginRequest.email,
                otp: loginRequest.otp,
                role: loginRequest.role
            });

            console.log("login otp", response.data);
            setToken(response.data.jwt);
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
            setToken(response.data.jwt)
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
            // Was localStorage.clear() — that wipes ALL localStorage keys
            // for the origin, not just the JWT, which risks clobbering
            // unrelated app state (cart drafts, preferences, etc.) that
            // has nothing to do with auth. Clear only the token.
            clearToken()
            console.log("logged out successfully..")
            navigate("/")
        } catch (error) {
            console.log("error...", error)
        }
    }
)

// Matches UserController's actual @RequestMapping("/users") — no /api
// prefix on this controller, unlike some others in this backend.
export const completeUserProfile = createAsyncThunk<
    any,
    { fullName: string; mobile: string },
    { rejectValue: string }
>(
    "/auth/completeUserProfile",
    async (profileData, { rejectWithValue }) => {
        try {
            const response = await api.patch("/users/complete-profile", profileData);
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || "Failed to complete profile");
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
    reducers: {
        // Sets the JWT directly into state — used by the Google OAuth2
        // redirect callback, where the token arrives via a URL query
        // param rather than an API response body. Mirrors what
        // signin.fulfilled/signup.fulfilled already do, so isLoggedIn
        // and downstream effects (fetchUserProfile in App.tsx) behave
        // identically regardless of which login path produced the token.
        setCredentials(state, action: PayloadAction<string>) {
            state.jwt = action.payload;
            state.token = action.payload;
            state.isLoggedIn = true;
            state.error = null;
            setToken(action.payload);
        },
    },
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
        builder.addCase(completeUserProfile.fulfilled, (state, action) => {
            // Response should be the updated User (profileComplete: true).
            // Merging rather than replacing in case the backend returns
            // a partial DTO rather than the full user object.
            state.user = { ...state.user, ...action.payload }
        })
        builder.addCase(completeUserProfile.rejected, (state, action) => {
            state.error = action.payload as string
        })
        builder.addCase(logout.fulfilled, (state) => {
            state.jwt = null
            state.isLoggedIn = false
            state.user = null
        })
    }
})

export const { setCredentials } = authSlice.actions;
export default authSlice.reducer;