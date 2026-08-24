import { useEffect } from 'react';
import { ThemeProvider } from '@mui/material';
import Navbar from './customer/components/Navbar/navbar';
import customTheme from './Theme/customTheme';
import Home from './customer/pages/Home/Home';
import Product from './customer/pages/Product/Product';
import ProductDetails from './customer/pages/ProductDetails/ProductDetails';
import Reviews from './customer/pages/Review/Reviews';
import Cart from './customer/pages/Cart/Cart';
import Checkout from './customer/pages/Checkout/Checkout';
import Account from './customer/pages/Account/Account';
import { Route, Routes, useNavigate } from 'react-router-dom';
import BecomeSeller from './customer/pages/Become Seller/BecomeSeller';
import SellerDashboard from './seller/pages/SellerDashboard/SellerDashboard';
import AdminDashboard from './admin/pages/Dashboard/AdminDashboard';
import { useAppDispatch, useAppSelector } from './State/store';
import { fetchSellerProfile } from './State/seller/sellerSlice';
import Auth from './customer/pages/Auth/Auth';
import CompleteProfile from './customer/pages/Auth/CompleteProfile';
import { fetchUserProfile, setCredentials } from './State/authSlice';
import PaymentSuccess from './customer/pages/PaymentSuccess';
import Wishlist from './customer/Wishlist/Wishlist';
import { ToastContainer } from 'react-toastify';
import SellerLoginForm from './customer/pages/Become Seller/SellerLoginForm';
import { fetchHomePageData } from './State/customer/customerSlice';
import { getToken } from './Util/tokenStorage';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import './App.css';



function App() {
    const dispatch = useAppDispatch();
    const { seller, auth } = useAppSelector(store => store);
    const navigate = useNavigate();

    // Captures the JWT that OAuth2AuthenticationSuccessHandler appends
    // to the redirect URL after Google sign-in completes (target can be
    // "/", "/complete-profile", or "/account/settings" depending on
    // account state — this effect runs on every page since App wraps
    // all routes, so it catches the token regardless of which target
    // was used).
    //
    // Runs first (before the fetchUserProfile effect below) so that by
    // the time that effect's dependency on auth.jwt changes, the token
    // is already in Redux state and session storage.
    //
    // Only the `token` param is stripped from the URL — `newUser` and
    // `googleLinked` are left in place since /complete-profile and
    // /account/settings read those to decide what to show. Per the
    // backend handler's own documentation, moving the token out of the
    // URL immediately (via replaceState, not just a value in memory)
    // is required so it doesn't linger in browser history.
    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
        if (token) {
            dispatch(setCredentials(token));
            params.delete('token');
            const remaining = params.toString();
            const cleanUrl =
                window.location.pathname +
                (remaining ? `?${remaining}` : '') +
                window.location.hash;
            window.history.replaceState({}, document.title, cleanUrl);
        }
    }, [dispatch]);

    useEffect(() => {
        dispatch(fetchSellerProfile(getToken() || ""));
        dispatch(fetchHomePageData()); // ← replaces createHomeCategories(homeCategories)
    }, [dispatch]);

    useEffect(() => {
        if (seller.profile) {
            navigate("/seller");
        }
    }, [seller.profile, navigate]);

    useEffect(() => {
        dispatch(fetchUserProfile({ jwt: auth.jwt || getToken() }));
    }, [auth.jwt, dispatch]);

    return (
        <ThemeProvider theme={customTheme}>
            <ToastContainer position="top-right" autoClose={3000} />
            <div>
                <Navbar />
                <Routes>
                    <Route path="/seller/login" element={<SellerLoginForm />} />
                    <Route path="/seller/dashboard" element={<SellerDashboard />} />
                    <Route path='/' element={<Home />} />
                    <Route path='/login' element={<Auth />} />
                    <Route path='/products/:category' element={<Product />} />
                    <Route path='/reviews/:productId' element={<Reviews />} />
                    <Route path='/product-details/:categoryId/:name/:productId' element={<ProductDetails />} />
                    <Route path='/cart' element={<Cart />} />
                    <Route path='/wishlist' element={<Wishlist />} />
                    <Route path='/checkout' element={<Checkout />} />
                    <Route path='/payment-success/:orderId' element={<PaymentSuccess />} />
                    <Route path='/become-seller' element={<BecomeSeller />} />
                    <Route path='/account/*' element={<Account />} />
                    <Route path='/seller/*' element={<SellerDashboard />} />
                    <Route path='/admin/*' element={<AdminDashboard />} />
                    <Route path='/complete-profile' element={<CompleteProfile />} />
                </Routes>
            </div>
        </ThemeProvider>
    );
}

export default App;
