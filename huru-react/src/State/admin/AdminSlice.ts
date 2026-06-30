import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { HomeCategory } from "../../types/HomeCategoryType";
import { api } from "../../config/Api";


const API_URL = '/admin';

export const updateHomeCategory = createAsyncThunk<HomeCategory,
    { id: number; data: HomeCategory }>
    (
        'homeCategory/updateHomeCategory',
        async ({ id, data }, { rejectWithValue }) => {
            try {
                const response = await api.patch(`${API_URL}/home-category/${id}`, data);
                console.log("category updated ", response)
                return response.data;
            } catch (error: any) {
                console.log("Error", error)
                if (error.response && error.response.data) {
                    return rejectWithValue(error.response.data);
                } else {
                    return rejectWithValue('An error while updating the category');
                }
            }
        }
    );

export const fetchHomeCategories = createAsyncThunk<HomeCategory[]>(
    'homeCategory/fetchHomeCategories',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get(`${API_URL}/home-category`);
            console.log("categories", response.data)
            return response.data;
        } catch (error: any) {
            console.log("error", error.response)
            return rejectWithValue(error.response?.data?.message || 'Failed to fetch categories')
        }
    }
);

interface HomeCategoryState {
    categories: HomeCategory[];
    loading: boolean;
    error: string | null;
    categoryUpdated: boolean;
};

const initialState: HomeCategoryState = {
    categories: [],
    loading: false,
    error: null,
    categoryUpdated: false,
}

// create the slice
const homeCategorySlice = createSlice({
    name: 'homeCategory',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        // Handle the pending state for updateHomeCategory
        builder.addCase(updateHomeCategory.pending, (state) => {
            state.loading = true;
            state.error = null;
            state.categoryUpdated = false;
        });
        // handle fulfilled state
        builder.addCase(updateHomeCategory.fulfilled, (state, action) => {
            state.loading = false;
            state.categoryUpdated = true;
            // find the category by Id
            const index = state.categories.findIndex((category) => category.id)
            if (index !== -1) {
                state.categories[index] = action.payload;
            } else {
                state.categories.push(action.payload);
            }

        });

        // handle reject
        builder.addCase(updateHomeCategory.rejected, (state, action) => {
            state.loading = true;
            state.error = action.payload as string;
        })
        // fetch home category
        builder.addCase(fetchHomeCategories.pending, (state) => {
            state.loading = true;
            state.error = null;
            state.categoryUpdated = false;
        })
        builder.addCase(fetchHomeCategories.fulfilled, (state, action) => {
            state.loading = false;
            state.categories = action.payload;
        })
        builder.addCase(fetchHomeCategories.rejected, (state, action) => {
            state.loading = false;
            state.error = action.payload as string;
        })
    },
});

export default homeCategorySlice.reducer;