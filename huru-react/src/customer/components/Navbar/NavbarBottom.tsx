import {
    Box,
    Divider,
    useTheme,
    Drawer,
    Button,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import CloseIcon from "@mui/icons-material/Close";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { mainCategory } from "../../../Data/Category/mainCategory";
import CategorySheet from "./CategorySheet";
import AllCategory from "../../pages/Home/ShopByCategory/AllCategory";
// import AllCategoriesSheet from "../AllCategories";

const NavbarBottom = () => {
    const [selectedCategory, setSelectedCategory] = useState("men");
    const [showCategorySheet, setShowCategorySheet] = useState(false);
    //  const [showAllCategoriesSheet, setShowAllCategoriesSheet] = useState(false);

    const [drawerOpen, setDrawerOpen] = useState(false);
    const theme = useTheme();
    const navigate = useNavigate();

    const handleDrawerToggle = () => {
        setDrawerOpen(!drawerOpen);
    };

    return (
        <Box className="relative" >
            <Box
                className=" flex items-center top-0  px-5 lg:px-20 h-[60px] bg-white z-20"
                sx={{ zIndex: 20 }}
                onMouseLeave={() => setShowCategorySheet(false)}
            >
                {/* All Categories Button */}
                <Button
                    onClick={handleDrawerToggle}
                    sx={{
                        borderRadius: "10px",
                        textTransform: "none",
                        padding: "6px 12px",
                        minWidth: "150px",
                        height: "45px",
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                        backgroundColor: "#f9f9f9",
                        color: "#333",
                        "&:hover": { backgroundColor: "#f0f0f0" },
                    }}
                >
                    {drawerOpen ? <CloseIcon fontSize="small" /> : <MenuIcon fontSize="small" />}
                    <span className="text-sm font-medium">All Categories</span>
                    <ArrowDropDownIcon fontSize="small" />
                </Button>

                {/* <Button
                    onClick={() => {
                        setShowAllCategoriesSheet(!showAllCategoriesSheet);
                        setShowCategorySheet(false);
                    }}

                >
                    {showAllCategoriesSheet ? <CloseIcon fontSize="small" /> : <MenuIcon fontSize="small" />}
                    <span className="text-sm font-medium">All Categories</span>
                    <ArrowDropDownIcon fontSize="small" />
                </Button> */}


                {/* Vertical Divider */}
                <Divider
                    orientation="vertical"
                    sx={{
                        height: "45px",
                        marginX: 2,
                        alignSelf: "center",
                        display: { xs: "none", sm: "block" },
                    }}
                />

                {/* Categories List */}
                <ul className="flex items-center font-medium text-gray-800 overflow-x-auto">
                    {mainCategory.map((item) => (
                        <li
                            key={item.categoryId}
                            // onMouseLeave={() => setShowCategorySheet(false)}
                            onMouseEnter={() => {
                                setShowCategorySheet(true);
                                setSelectedCategory(item.categoryId);
                            }}
                            className="hover:text-primary-color hover:border-b-2 h-[60px] px-4 border-primary-color flex items-center cursor-pointer"
                        >
                            {item.name}
                        </li>
                    ))}
                </ul>

                {/* Slide-in Drawer for All Categories */}
                <Drawer
                    anchor="left"
                    open={drawerOpen}
                    onClose={handleDrawerToggle}
                    PaperProps={{
                        sx: {
                            width: "80%",
                            maxWidth: "300px",
                            paddingTop: 2,
                            transition: "transform 0.3s ease",
                        },
                    }}
                >
                    <Box sx={{ paddingX: 2, fontWeight: "bold", fontSize: "1.1rem" }}>
                        All Categories
                    </Box>
                    <Divider sx={{ marginY: 1 }} />

                    <Box className="flex flex-col gap-4 px-4 py-2">
                        {mainCategory.map((mainItem) => (
                            <React.Fragment key={mainItem.categoryId}>
                                {/* Main category title */}
                                <div className="font-semibold text-gray-700">{mainItem.name}</div>

                                {/* Subcategories sorted alphabetically */}
                                <Box className="flex flex-col gap-2 items-start pl-2">
                                    {mainItem.levelTwoCategory
                                        ?.slice() // make a shallow copy before sorting
                                        .sort((a, b) => a.name.localeCompare(b.name))
                                        .map((subItem) => (
                                            <div
                                                key={subItem.categoryId}
                                                onClick={() => {
                                                    navigate(`/Home/ShopByCategory/${subItem.categoryId}`);
                                                    setDrawerOpen(false);
                                                }}
                                                className="w-full"
                                            >
                                                <AllCategory item={subItem} />
                                            </div>
                                        ))}
                                </Box>

                                <Divider sx={{ marginY: 1 }} />
                            </React.Fragment>
                        ))}
                    </Box>
                </Drawer>


                {/* CategorySheet Hover Dropdown */}
                {/* All Categories Dropdown Sheet
                {showAllCategoriesSheet && (
                    <Box
                        onMouseLeave={() => setShowAllCategoriesSheet(false)}
                        className="absolute top-[60px] left-0 right-0 border bg-white shadow z-10"
                    >
                        <CategorySheet setShowSheet={setShowAllCategoriesSheet} />
                    </Box>
                )} */}

                {showCategorySheet && (
                    <Box
                        onMouseEnter={() => setShowCategorySheet(true)}
                        onMouseLeave={() => setShowCategorySheet(false)}
                        className="absolute top-[60px] left-0 right-0 border bg-white shadow z-10"
                    >
                        <CategorySheet
                            selectedCategory={selectedCategory}
                            setShowSheet={setShowCategorySheet}
                        />
                    </Box>
                )}
            </Box>
        </Box >
    );
};

export default NavbarBottom;
