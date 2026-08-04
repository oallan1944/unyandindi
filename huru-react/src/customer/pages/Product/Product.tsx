import React, { useEffect, useState } from 'react'
import FilterSection from './FilterSection'
import ProductCard from './ProductCard'
import {
    Box, Divider, FormControl, IconButton,
    InputLabel, MenuItem, Pagination,
    Select, useMediaQuery, useTheme
} from '@mui/material'
import { FilterAlt } from '@mui/icons-material'
import { useAppDispatch, useAppSelector } from '../../../State/store'
import { fetchAllProducts } from '../../../State/customer/ProductSlice'
import { useParams, useSearchParams } from 'react-router-dom'
import AsyncStateWrapper from '../../../component/AsyncStateWrapper'
import ErrorBoundary from '../../../component/ErrorBoundary'


// ✅ type-safe sort options — prevents typos and out-of-range MUI values
type SortOption = "" | "price_low" | "price_high" | "discount" | "newest"

const SORT_OPTIONS: { value: SortOption; label: string }[] = [
    { value: "",           label: "Default"          },
    { value: "price_low",  label: "Price: Low - High" },
    { value: "price_high", label: "Price: High - Low" },
    { value: "discount",   label: "Biggest Discount"  },
    { value: "newest",     label: "Newest First"      },
]

const Product = () => {
    const theme = useTheme()
    const isLarge = useMediaQuery(theme.breakpoints.up("lg"))

    // ✅ initialized as "" — matches MUI Select default option, eliminates undefined warning
    const [sort, setSort] = useState<SortOption>("")
    const [page, setPage] = useState(1)

    const dispatch = useAppDispatch()
    const [searchParams] = useSearchParams()
    const { category } = useParams()
    const { product: productState } = useAppSelector(store => store)

    const handleSortChange = (event: any) => {
        setSort(event.target.value as SortOption)
        setPage(1) // ✅ reset to page 1 when sort changes
    }

    const handlePageChange = (_: any, value: number) => {
        setPage(value)
    }

    const loadProducts = () => {
        const priceParam = searchParams.get('price')
        let minPrice: number | undefined
        let maxPrice: number | undefined

        if (priceParam) {
            const [minStr, maxStr] = priceParam.split('.')
            const minNum = Number(minStr)
            const maxNum = Number(maxStr)
            if (!isNaN(minNum)) minPrice = minNum
            if (!isNaN(maxNum)) maxPrice = maxNum
        }

        const color = searchParams.get('color') || ''
        const minDiscountParam = searchParams.get('discount')
        const minDiscount = minDiscountParam ? Number(minDiscountParam) : undefined
        const pageNumber = page - 1

        const newFilter: any = {
            category,
            color,
            pageNumber,
            sort: sort || undefined  // ✅ don't send empty string to backend
        }

        if (minPrice !== undefined) newFilter.minPrice = minPrice
        if (maxPrice !== undefined) newFilter.maxPrice = maxPrice
        if (minDiscount !== undefined) newFilter.minDiscount = minDiscount

        dispatch(fetchAllProducts(newFilter))
    }

    useEffect(() => {
        loadProducts()
    }, [dispatch, searchParams, page, category, sort])

    return (
        <div className='-z-10 mt-10'>
            <div>
                <h1 className='text-3xl text-center font-bold text-gray-700 pb-5 px-9 uppercase'>
                    {category
                        ? category.replace(/_/g, ' ')
                        : 'All Products'}
                </h1>
            </div>

            <div className='lg:flex'>
                {/* Filter Sidebar */}
                <section className='filter_section hidden lg:block w-[20%]'>
                    <FilterSection />
                </section>

                <div className='w-full lg:w-[80%] space-y-5'>
                    {/* Sort & Mobile Filter Bar */}
                    <div className='flex justify-between items-center px-9 h-[40px]'>
                        <div className='relative w-[50%]'>
                            {!isLarge && (
                                <IconButton>
                                    <FilterAlt />
                                </IconButton>
                            )}
                            {!isLarge && (
                                <Box>
                                    <FilterSection />
                                </Box>
                            )}
                        </div>

                        {/* ✅ type-safe sort select — no more MUI undefined warning */}
                        <FormControl size='small' sx={{ width: "200px" }}>
                            <InputLabel id="sort-label">Sort</InputLabel>
                            <Select
                                labelId="sort-label"
                                value={sort}
                                label="Sort"
                                onChange={handleSortChange}
                                displayEmpty
                            >
                                {SORT_OPTIONS.map(option => (
                                    <MenuItem key={option.value} value={option.value}>
                                        {option.label}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </div>

                    <Divider />

                    {/* ── Product Grid ── */}
                    <ErrorBoundary>
                        <AsyncStateWrapper
                            loading={productState.loading}
                            error={productState.error}
                            empty={
                                !productState.loading &&
                                !productState.error &&
                                productState.products.length === 0
                            }
                            onRetry={loadProducts}
                            emptyMessage={
                                category
                                    ? `No products found in "${category.replace(/_/g, ' ')}".`
                                    : "No products found."
                            }
                        >
                            <section className='product_section grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-y-5 px-5 justify-center'>
                                {productState.products.map((item) => (
                                    <ProductCard key={item.id} item={item} />
                                ))}
                            </section>
                        </AsyncStateWrapper>
                    </ErrorBoundary>

                    {/* Pagination — only when products loaded */}
                    {!productState.loading &&
                     !productState.error &&
                     productState.products.length > 0 && (
                        <div className='flex justify-center py-10'>
                            <Pagination
                                onChange={handlePageChange}
                                count={productState.totalPages || 1}
                                page={page}
                                variant="outlined"
                                color='primary'
                            />
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}

export default Product














