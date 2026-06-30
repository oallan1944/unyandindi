import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { Cart, CartItem } from "../../types/cartType";
import { api } from "../../config/Api";
import { sumCartItemMrpPrice, sumCartItemSellingPrice } from "../../Util/sumCartItemMrpPrice";
import { applyCoupon } from "./couponSlice";



interface CartState {
    cart: Cart | null;
    loading: boolean;
    error: string | null;
}

const initialState: CartState = {
    cart: null,
    loading: false,
    error: null,
};

//define the base url for the API
const API_URL = "/api/cart";

export const fetchUserCart = createAsyncThunk<Cart, string>(
    "cart/fetchUserCart",
    async (jwt: string, { rejectWithValue }) => {
        try {
            const response = await api.get(API_URL, {
                headers: {
                    Authorization: `Bearer ${jwt}`,
                },
            });
            console.log("Cart fetched", response.data);
            return response.data;
        } catch (error: any) {
            console.log("error", error.response);
            return rejectWithValue("Failed to fetch User cart")

        }
    }
);

interface AddItemRequest {
    productId: number | undefined;
    size: string;
    quantity: number;
}

export const addItemToCart = createAsyncThunk<CartItem, { jwt: string | null; request: AddItemRequest }>
    ("cart/AddItemToCart", async ({ jwt, request }, { rejectWithValue }) => {
        try {
            const response = await api.put(`${API_URL}/add`, request, {
                headers: {
                    Authorization: `Bearer ${jwt}`
                },
            });
            console.log("cart added", response.data);
            return response.data;
        } catch (error: any) {
            console.log("error", error.response);
            return rejectWithValue("failed to add item to cart");
        }
    }
    );

export const deleteCartItem = createAsyncThunk<any, { jwt: string; cartItemId: number }>(
    "cart/deleteCartItem", async ({ jwt, cartItemId }, { rejectWithValue }) => {
        try {
            const response = await api.delete(`${API_URL}/item/${cartItemId}`, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            return response.data;
        } catch (error: any) {
            return rejectWithValue(
                error.response.data.message || "Failed to delete Cart Item"
            );
        }
    }
);

export const updateCartItem = createAsyncThunk<any, { jwt: string | null; cartItemId: number; cartItem: any }>
    ("cart/updateCartItem",
        async ({ jwt, cartItemId, cartItem }, { rejectWithValue }) => {
            try {
                const response = await api.put(`${API_URL}/item/${cartItemId}`,
                    cartItem, {
                    headers: { Authorization: `Bearer ${jwt}` },
                }
                );
                return response.data
            } catch (error: any) {
                return rejectWithValue(
                    error.response.data.message || "Failed to update cart Item"
                );
            }
        }
    );

const cartSlice = createSlice({
    name: "cart",
    initialState,
    reducers: {
        resetCartState: (state) => {
            state.cart = null;
            state.loading = false;
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchUserCart.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(
                fetchUserCart.fulfilled, (state,
                    action: PayloadAction<Cart>) => {
                state.cart = action.payload;
                state.loading = false;
            }
            )
            .addCase(fetchUserCart.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            .addCase(addItemToCart.fulfilled,
                (state, action: PayloadAction<CartItem>) => {
                    if (state.cart) {
                        state.cart.cartItems.push(action.payload);
                    }
                    state.loading = false;
                }
            )
            .addCase(addItemToCart.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            .addCase(deleteCartItem.pending, (state) => {
                state.loading = true;
                state.error = null
            })
            .addCase(deleteCartItem.fulfilled, (state, action) => {
                if (state.cart) {
                    state.cart.cartItems = state.cart.cartItems.filter(
                        (item: CartItem) => item.id !== action.meta.arg.cartItemId
                    );
                    const mrpPrice = sumCartItemMrpPrice(state.cart?.cartItems || [])
                    const sellingPrice = sumCartItemSellingPrice(state.cart?.cartItems || [])
                    state.cart.totalSellingPrice = sellingPrice;
                    state.cart.totalMrpPrice = mrpPrice;
                }
                state.loading = false;
            })
            .addCase(updateCartItem.fulfilled, (state, action) => {
                if (state.cart) {
                    const index = state.cart.cartItems.findIndex(
                        (item: CartItem) => item.id === action.meta.arg.cartItemId
                    );
                    if (index !== -1) {
                        state.cart.cartItems[index] = {
                            ...state.cart.cartItems[index],
                            ...action.payload,
                        };
                    }
                    const mrpPrice = sumCartItemMrpPrice(state.cart?.cartItems || [])
                    const sellingPrice = sumCartItemSellingPrice(state.cart?.cartItems || [])
                    state.cart.totalSellingPrice = sellingPrice;
                    state.cart.totalMrpPrice = mrpPrice;

                }
                state.loading=false;
            })
            .addCase(updateCartItem.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            .addCase(applyCoupon.fulfilled, (state, action) => {
                state.loading = false;
                state.cart = action.payload;
            })
    },
});

export default cartSlice.reducer;
export const { resetCartState } = cartSlice.actions;
