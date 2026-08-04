import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { api } from "../../config/Api";

export interface Category {
    id?: number;
    name: string;
    categoryId: string;
    parentCategory?: Category | null;
    level: number;
}

const API_URL = '/api/admin/categories';

export const createCategory = createAsyncThunk<Category, Category>(
    'category/createCategory',
    async (data, { rejectWithValue }) => {
        try {
            const response = await api.post(API_URL, data);
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || 'Failed to create category');
        }
    }
);

export const fetchCategories = createAsyncThunk<Category[]>(
    'category/fetchCategories',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get(API_URL);
            return response.data;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || 'Failed to fetch categories');
        }
    }
);

interface CategoryState {
    categories: Category[];
    loading: boolean;
    error: string | null;
}

const initialState: CategoryState = {
    categories: [],
    loading: false,
    error: null,
};

const categorySlice = createSlice({
    name: 'category',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(createCategory.pending, (state) => {
            state.loading = true;
            state.error = null;
        });
        builder.addCase(createCategory.fulfilled, (state, action) => {
            state.loading = false;
            state.categories.push(action.payload);
        });
        builder.addCase(createCategory.rejected, (state, action) => {
            state.loading = false;
            state.error = action.payload as string;
        });

        builder.addCase(fetchCategories.pending, (state) => {
            state.loading = true;
            state.error = null;
        });
        builder.addCase(fetchCategories.fulfilled, (state, action) => {
            state.loading = false;
            state.categories = action.payload;
        });
        builder.addCase(fetchCategories.rejected, (state, action) => {
            state.loading = false;
            state.error = action.payload as string;
        });
    },
});

export default categorySlice.reducer;