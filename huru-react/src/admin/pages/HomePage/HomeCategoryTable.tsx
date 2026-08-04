import * as React from 'react';
import { styled } from '@mui/material/styles';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { Button } from '@mui/material';
import { Add, Edit } from '@mui/icons-material';
import { HomeCategory } from '../../../types/HomeCategoryType';
import AddHomeCategory from './AddHomeCategory';

const getImageSrc = (image?: string) => {
    if (!image) return "/placeholder.png";
    if (image.startsWith("http")) return image.trim();
    return "/" + image.replace(/^\/+/, "");
};

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white
    },
    [`&.${tableCellClasses.body}`]: {
        fontSize: 14
    }
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    "&:nth-of-type(odd)": {
        backgroundColor: theme.palette.action.hover
    },
    "&:last-child td, &:last-child th": {
        border: 0
    }
}));

type HomeSection = "GRID" | "SHOP_BY_CATEGORIES" | "ELECTRIC_CATEGORIES" | "DEALS";

interface Props {
    data: HomeCategory[];
    section: HomeSection;
}

export default function HomeCategoryTable({ data, section }: Props) {
    const [selectedCategory, setSelectedCategory] = React.useState<HomeCategory | null>(null);
    const [open, setOpen] = React.useState(false);

    const handleEditClick = (category: HomeCategory) => {
        setSelectedCategory(category);
        setOpen(true);
    };

    const handleAddClick = () => {
        setSelectedCategory(null);
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
        setSelectedCategory(null);
    };

    return (
        <>
            <div className='flex justify-end mb-3'>
                <Button variant='contained' startIcon={<Add />} onClick={handleAddClick}>
                    Add {section.replace(/_/g, " ").toLowerCase()}
                </Button>
            </div>

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 700 }} aria-label="home category table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>No</StyledTableCell>
                            <StyledTableCell>ID</StyledTableCell>
                            <StyledTableCell>Image</StyledTableCell>
                            <StyledTableCell align="right">Category</StyledTableCell>
                            <StyledTableCell align="right">Update</StyledTableCell>
                        </TableRow>
                    </TableHead>

                    <TableBody>
                        {data.length === 0 && (
                            <StyledTableRow>
                                <StyledTableCell colSpan={5} align="center">
                                    No categories found
                                </StyledTableCell>
                            </StyledTableRow>
                        )}

                        {data.map((category, index) => (
                            <StyledTableRow key={category.id ?? index}>
                                <StyledTableCell>{index + 1}</StyledTableCell>
                                <StyledTableCell>{category.id}</StyledTableCell>
                                <StyledTableCell>
                                    <img
                                        src={getImageSrc(category.image)}
                                        alt={category.name || "category image"}
                                        style={{ width: 70, height: 70, objectFit: "contain", borderRadius: 8 }}
                                        onError={(e) => {
                                            (e.currentTarget as HTMLImageElement).src = "/placeholder.png";
                                        }}
                                    />
                                </StyledTableCell>
                                <StyledTableCell align="right">{category.categoryId}</StyledTableCell>
                                <StyledTableCell align="right">
                                    <Button
                                        variant="contained"
                                        color="primary"
                                        size="small"
                                        onClick={() => handleEditClick(category)}
                                    >
                                        <Edit fontSize="small" />
                                    </Button>
                                </StyledTableCell>
                            </StyledTableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <AddHomeCategory
                open={open}
                handleClose={handleClose}
                section={section}
                category={selectedCategory}
            />
        </>
    );
}