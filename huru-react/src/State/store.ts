import { combineReducers, configureStore } from "@reduxjs/toolkit";
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux";
// import { thunk } from "redux-thunk";
import sellerSlice from "./seller/sellerSlice";
import sellerProductSlice from "./seller/sellerProductSlice";
import ProductSlice from "./customer/ProductSlice";
import authSlice from "./authSlice";
import cartSlice from "./customer/cartSlice";
import orderSlice from "./customer/orderSlice";
import wishlistSlice from "./customer/wishlistSlice";
import sellerOrderSlice from "./seller/sellerOrderSlice";
import transactionSlice from "./seller/transactionSlice";
import AdminSlice from "./admin/AdminSlice";
import customerSlice from "./customer/customerSlice";
import DealSlice from "./admin/DealSlice";
import sellerAuthSlice from "./seller/sellerAuthSlice";


const rootReducer = combineReducers({
    seller: sellerSlice,
    sellerProduct: sellerProductSlice,
    product: ProductSlice,
    auth: authSlice,
    cart: cartSlice,
    order: orderSlice,
    wishlist: wishlistSlice,
    customer: customerSlice,

    //seller slice
    sellerOrder: sellerOrderSlice,
    transactions: transactionSlice,
    sellerAuth: sellerAuthSlice,
    //admin slice
    admin: AdminSlice,
    deal: DealSlice,
});
const store = configureStore({
    reducer: rootReducer,
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(), // removed thunk
})

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof rootReducer>;

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

export default store;