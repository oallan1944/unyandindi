import {
    Avatar,
    Box,
    Button,
    IconButton,
    InputBase,
    useMediaQuery,
    useTheme,
    Badge,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import { AddShoppingCart, FavoriteBorder, Storefront } from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { useAppSelector } from "../../../State/store";

const NavbarTop = () => {
    const theme = useTheme();
    const isLarge = useMediaQuery(theme.breakpoints.up("lg"));
    const navigate = useNavigate();
    const { auth, cart } = useAppSelector((store) => store);

    return (
        <Box className="flex items-center justify-between px-5 lg:px-20 h-[60px]">
            {/* Brand + Search */}
            <Box className="flex items-center gap-4">
                <h1
                    onClick={() => navigate("/")}
                    className="logo cursor-pointer 
                    font-semibold tracking-widest
                    text-lg md:text-2xl sm:3x1 uppercase
                    text-mainBody-color  "
                >
                    Huru
                    {/* Huru Market */}
                </h1>

                {isLarge && (
                    <Box className="flex items-center px-2 rounded border text-mainBody-color border-gray-300" sx={{ minWidth: "300px" }}>

                        <InputBase placeholder="Find a product" sx={{ ml: 1, flex: 1 }} />
                        <SearchIcon sx={{ fontSize: 22 }} />
                    </Box>
                )}
            </Box>

            {/* Actions */}
            <Box className="flex items-center gap-1 lg:gap-6">
                {auth.user ? (
                    <Button onClick={() => navigate("/account/orders")} className="flex items-center gap-2" >
                        <Avatar sx={{ width: 29, height: 29 }} />
                        <span className="font-semibold hidden lg:block">{auth.user?.fullName}</span>
                    </Button>
                ) : (
                    <Button variant="contained" onClick={() => navigate("/login")}>
                        Login
                    </Button>
                )}

                <IconButton onClick={() => navigate("/wishlist")}>
                    <FavoriteBorder sx={{ fontSize: 29 }} />
                </IconButton>

                <IconButton
                    // className="bg-orange-500"
                    onClick={() => navigate("/cart")}>
                    <Badge badgeContent={cart.cart?.totalItem || 0} color="primary" showZero overlap="circular">
                        <AddShoppingCart sx={{ fontSize: 29, color: "gray" }} />
                    </Badge>
                </IconButton>

                {isLarge && (
                    <Button onClick={() => navigate("/become-seller")} startIcon={<Storefront />} variant="outlined">
                        Become Seller
                    </Button>
                )}
            </Box>
        </Box>
    );
};

export default NavbarTop;
