import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import { Order, OrderItem, OrderState } from "../../types/orderType";
import { api } from "../../config/Api";
import { Address } from "../../types/userTypes";
import axios from "axios";
import { getToken } from "../../Util/tokenStorage";

const initialState: OrderState = {
    orders: [],
    orderItem: null,
    currentOrder: null,
    paymentOrder: null,
    loading: false,
    error: null,
    orderCanceled: false
};

const API_URL = "/api/orders";
// fetch user order history

export const fetchUserOrderHistory = createAsyncThunk<Order[], string>(
    "orders/fetchUserOrderHistory",
    async (jwt, { rejectWithValue }) => {
        try {
            const response = await api.get(`${API_URL}/user`, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            console.log("Order history fetched", response.data);
            return response.data;
        } catch (error: any) {
            console.log("Error", error.response);
            return rejectWithValue(
                error.response.data.error || "Failed to fetch order History"
            );
        }
    }
);

// fetch order by Id
export const fetchOrderById = createAsyncThunk<Order,
    { orderId: number; jwt: string }
>("orders/fetchOrderById",
    async ({ orderId, jwt }, { rejectWithValue }) => {
        try {
            const response = await api.get(`${API_URL}/${orderId}`, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            console.log("Order Fetched", response.data);
            return response.data;
        } catch (error: any) {
            console.log("error", error.response);
            return rejectWithValue("Failed to fetch order");
        }
    });

// create a new order
export const createOrder = createAsyncThunk<any,
    { address: Address; jwt: string, paymentGateway: string }>
    ("orders/createOrder", async ({ address, jwt, paymentGateway },
        { rejectWithValue }
    ) => {
        try {
            const response = await api.post(API_URL, address, {
                headers: { Authorization: `Bearer ${jwt}` },
                params: { paymentMethod: paymentGateway }
            });
            console.log("order created,  ", response.data);
            if (response.data.payment_link_url) {
                window.location.href = response.data.payment_link_url
            }
            return response.data;
        } catch (error: any) {
            console.log("error", error.response);
            return rejectWithValue("Failed to create order")
        }
    });

export const fetchOrderItemById = createAsyncThunk<
    OrderItem,
    { orderItemId: number; jwt: string }
>("orders/fetchOrderItemById",
    async ({ orderItemId, jwt }, { rejectWithValue }) => {
        try {
            const response = await api.get(`${API_URL}/item/${orderItemId}`, {
                headers: { Authorization: `Bearer ${jwt}` },
            });
            console.log("order item fetched", response.data);
            return response.data;
        } catch (error: any) {
            console.log("error ", error.response);
            return rejectWithValue("Failed to fetch order item")
        }
    }
);

// payment success handler

export const paymentSuccess = createAsyncThunk<
    any,
    { paymentId: string; jwt: string, paymentLinkId: string },
    { rejectValue: string }
>("orders/paymentSuccess",
    async ({ paymentId, jwt, paymentLinkId }, { rejectWithValue }) => {
        try {
            const response = await api.get(`/api/payment/${paymentId}`, {
                headers: {
                    Authorization: `Bearer ${jwt}`,
                },
                params: { paymentLinkId }
            });
            console.log("payment success", response.data)
            return response.data
        } catch (error: any) {
            console.log("error", error.response)
            if (error.response) {
                return rejectWithValue(error.response.data.message);
            }
            return rejectWithValue("Failed to process payment")
        }
    }
);
export const cancelOrder = createAsyncThunk<Order, any>(
    "orders/cancelOrder",
    async (orderId, { rejectWithValue }) => {
        try {
            const response = await api.put(`${API_URL}/${orderId}/cancel`, {}, {
                headers: {
                    Authorization: `Bearer ${getToken()}`,
                },
            });
            console.log("cancel order", response.data)
            return response.data;
        } catch (error: any) {
            console.log("error", error.response)
            if (axios.isAxiosError(error) && error.response) {
                return rejectWithValue(error.response.data);
            }
            return rejectWithValue('An Error occured while cancelling the order. ')
        }
    }
);

const orderSlice = createSlice({
    name: "orders",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            //fetch user order history
            .addCase(fetchUserOrderHistory.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.orderCanceled = false;
            })
            .addCase(
                fetchUserOrderHistory.fulfilled,
                (state, action: PayloadAction<Order[]>) => {
                    state.orders = action.payload;
                    state.loading = false;
                }
            )
            .addCase(fetchUserOrderHistory.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            // fetch order by id
            .addCase(fetchOrderById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchOrderById.fulfilled,
                (state, action: PayloadAction<Order>) => {
                    state.currentOrder = action.payload;
                    state.loading = false;
                }
            )
            .addCase(fetchOrderById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })

            //create new order
            .addCase(createOrder.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(createOrder.fulfilled, (state, action: PayloadAction<any>) => {
                state.paymentOrder = action.payload;
                state.loading = false;
            })
            .addCase(createOrder.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            //fetch order item by id
            .addCase(fetchOrderItemById.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchOrderItemById.fulfilled, (state, action) => {
                state.loading = false;
                state.orderItem = action.payload;
            })
            .addCase(fetchOrderItemById.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            //payment success handler
            .addCase(paymentSuccess.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(paymentSuccess.fulfilled, (state, action) => {
                state.loading = false;
                console.log("payment Successful", action.payload)
            })
            .addCase(paymentSuccess.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            .addCase(cancelOrder.pending, (state) => {
                state.loading = true;
                state.error = null;
                state.orderCanceled = false;
            })
            .addCase(cancelOrder.fulfilled, (state, action) => {
                state.loading = false;
                state.orders = state.orders.map((order) =>
                    order.id === action.payload.id ? action.payload : order
                );
                state.orderCanceled = true;
                state.currentOrder = action.payload
            })
            .addCase(cancelOrder.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            });
    },
});
export default orderSlice.reducer;