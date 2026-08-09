import { Box, Grid, Typography } from "@mui/material";
import { homeCategories } from "../../../Data/Category/homeCategories";
import { useNavigate } from "react-router-dom";

const AllCategoriesSheet = ({ setShowSheet }) => {
    const navigate = useNavigate();

    return (
        <Box className="p-4">
            <Grid container spacing={2}>
                {homeCategories.map((category) => (
                    <Grid
                        item
                        xs={6}
                        sm={4}
                        md={3}
                        key={category.categoryId}
                        onClick={() => {
                            navigate(`/category/${category.categoryId}`);
                            setShowSheet(false);
                        }}
                        className="cursor-pointer"
                    >
                        <Box className="flex flex-col items-center">
                            <img
                                src={category.image}
                                alt={category.name}
                                className="w-20 h-20 object-cover rounded"
                            />
                            <Typography className="text-sm text-center mt-2">
                                {category.name}
                            </Typography>
                        </Box>
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
};

export default AllCategoriesSheet;
