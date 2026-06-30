import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { HomeCategory, HomeData } from "../../types/HomeCategoryType";
import { api } from "../../config/Api";

// ✅ fetch home page data — uncommented and fixed endpoint
export const fetchHomePageData = createAsyncThunk<HomeData>(
    "home/fetchHomePageData",
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/home');
            console.log("home page data:", response.data);
            return response.data;
        } catch (error: any) {
            const errorMessage = error.response?.data?.message 
                || error.message 
                || "Failed to fetch home data";
            console.log("fetchHomePageData error:", errorMessage);
            return rejectWithValue(errorMessage);
        }
    }
);

// ✅ admin action — creates/updates home categories
export const createHomeCategories = createAsyncThunk<HomeData, HomeCategory[]>(
    'home/createHomeCategories',
    async (homeCategories, { rejectWithValue }) => {
        try {
            const response = await api.post('/home/categories', homeCategories);
            console.log("home categories created:", response.data);
            return response.data;
        } catch (error: any) {
            const errorMessage = error.response?.data?.message 
                || error.message 
                || "Failed to create home categories";
            console.log("createHomeCategories error:", errorMessage);
            return rejectWithValue(errorMessage);
        }
    }
);

interface HomeState {
    homePageData: HomeData | null;
    homeCategories: HomeCategory[];
    loading: boolean;
    error: string | null;
}

const initialState: HomeState = {
    homePageData: null,
    homeCategories: [],
    loading: false,
    error: null,
};

const homeSlice = createSlice({
    name: "home",
    initialState,
    reducers: {
        clearCustomerError: (state) => {
            state.error = null;
        },
        setHomeCategories: (state, action) => {
            state.homeCategories = action.payload;
        }
    },
    extraReducers: (builder) => {
        builder
            // ── fetchHomePageData ────────────────────────────
            .addCase(fetchHomePageData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchHomePageData.fulfilled, (state, action: PayloadAction<HomeData>) => {
                state.loading = false;
                state.homePageData = action.payload;
            })
            .addCase(fetchHomePageData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string 
                    || "Failed to fetch home page data";
            })

            // ── createHomeCategories ─────────────────────────
            .addCase(createHomeCategories.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(createHomeCategories.fulfilled, (state, action: PayloadAction<HomeData>) => {
                state.loading = false;
                state.homePageData = action.payload;
            })
            .addCase(createHomeCategories.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string 
                    || "Failed to create home categories";
            });
    },
});

export const { clearCustomerError, setHomeCategories } = homeSlice.actions;
export default homeSlice.reducer;