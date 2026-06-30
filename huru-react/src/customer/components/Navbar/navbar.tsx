import { Box, Divider } from "@mui/material";
import NavbarTop from "./NavbarTop";
import NavbarBottom from "./NavbarBottom";

const Navbar = () => {
    return (
        <Box className="sticky top-0 left-0 right-0 bg-white" sx={{ zIndex: 2 }}>
            <NavbarTop />
            <Divider />
            <NavbarBottom />
            <Divider />
        </Box>
    );
};

export default Navbar;
