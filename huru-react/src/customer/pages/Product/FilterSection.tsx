import { Button, Divider, FormControl, FormControlLabel, FormLabel, Radio, RadioGroup, TextField } from '@mui/material'
import { teal } from '@mui/material/colors'
import { useEffect, useState } from 'react'
import { colors } from '../../../Data/Filter/color'
import { useSearchParams } from 'react-router-dom'
import { discount } from '../../../Data/Filter/discount'



const FilterSection: React.FC = () => {
    const [expandColor, setExpandColor] = useState(false);
    const [searchParams, setSearchPrams] = useSearchParams();

    // Free-form min/max price inputs, seeded from the URL so the fields
    // survive a refresh or a shared link.
    const [minPrice, setMinPrice] = useState(searchParams.get("price")?.split("-")[0] ?? "");
    const [maxPrice, setMaxPrice] = useState(searchParams.get("price")?.split("-")[1] ?? "");

    useEffect(() => {
        // Keep the inputs in sync if the filter is cleared/changed elsewhere (e.g. "clear all")
        setMinPrice(searchParams.get("price")?.split("-")[0] ?? "");
        setMaxPrice(searchParams.get("price")?.split("-")[1] ?? "");
    }, [searchParams.get("price")]);

    const handleColorToggle = () => {
        setExpandColor(!expandColor);
    };
    const updateFilterParams = (e: any) => {
        const { value, name } = e.target;
        if (value) {
            searchParams.set(name, value);
        } else {
            searchParams.delete(name);
        }
        setSearchPrams(searchParams);
    };

    const applyPriceFilter = () => {
        // Both blank -> clear the filter entirely.
        if (!minPrice && !maxPrice) {
            searchParams.delete("price");
            setSearchPrams(searchParams);
            return;
        }
        // Encoded as "min-max"; an omitted side is left blank and read as
        // open-ended (Product.tsx treats an empty maxStr as "no upper bound").
        searchParams.set("price", `${minPrice || 0}-${maxPrice || ""}`);
        setSearchPrams(searchParams);
    };
    // const filteredProducts = items.filter((item) => {
    //     if (priceRange) {
    //         const [min, max] = priceRange.split("-").map(Number);
    //         return item.sellingPrice >= min && item.sellingPrice <= max;
    //     }
    //     return true; // if no price param, show all
    // });
    const clearAllFilters = () => {
        console.log("clearAllFilters", searchParams)
        searchParams.forEach((value: any, key: any) => {
            searchParams.delete(key);
        });
        setSearchPrams(searchParams);
    };
    // useEffect(() => {
    //     const color = searchParams.get("color");
    //     const price = searchParams.get("price");
    //     const discount = searchParams.get("discount");

    //     dispatch(fetchAllProducts({ color, price, discount }));
    // }, [searchParams]);


    return (
        <div className='z-50 space-y-5 bg-white'>
            <div className='flex items-center justify-between h-[40px] px-9 lg:border-r'>
                <p className='text-lg font-semibold'> Filters</p>
                <Button onClick={clearAllFilters} size='small' className='text bg-teal-600 cursor-pointer font-semibold'>
                    clear all
                </Button>
            </div>
            <Divider />
            <div className='px-9 space-y-6'>
                <section>
                    <FormControl>
                        <FormLabel
                            sx={{
                                fontSize: "16px",
                                fontWeight: "bold",
                                color: teal[500],
                                pb: "14px"
                            }}
                            className='text-2xl font-semibold'
                            id='color'
                        >
                            Color
                        </FormLabel>
                        <RadioGroup
                            aria-labelledby="color"
                            // defaultValue=""
                            value={searchParams.get("color") || ""}
                            name="color"
                            onChange={updateFilterParams}
                        >
                            {colors.slice(0, expandColor ? colors.length : 5).map((item) => <FormControlLabel
                                 value={item.name} control={<Radio />}
                                label={<div className='flex items-center gap-3'>
                                    <p>{item.name}</p>
                                    <p style={{ backgroundColor: item.hex }}
                                        className={`h-5 w-5 rounded-full
                                 ${item.name === "white" ? "border" : ""}`}>
                                    </p>
                                </div>} />
                            )}
                        </RadioGroup>
                    </FormControl>
                    <div>
                        <Button
                            onClick={handleColorToggle}
                            className='text-primary-color cursor-pointer hover:text-teal-900 flex items-center'>
                            {expandColor ? "hide" : `+${colors.length - 5} more`}
                        </Button>
                    </div>
                </section>
                <section>
                    <FormControl>
                        <FormLabel
                            sx={{
                                fontSize: "16px",
                                fontWeight: "bold",
                                pb: "14px",
                                color: teal[600]
                            }}
                            className='text-2xl font-semibold' id='Price'
                        >
                            Price
                        </FormLabel>
                        <div className='flex items-center gap-2'>
                            <TextField
                                size='small'
                                type='number'
                                label='Min'
                                value={minPrice}
                                onChange={(e) => setMinPrice(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && applyPriceFilter()}
                                inputProps={{ min: 0 }}
                                sx={{ width: '90px' }}
                            />
                            <span>-</span>
                            <TextField
                                size='small'
                                type='number'
                                label='Max'
                                value={maxPrice}
                                onChange={(e) => setMaxPrice(e.target.value)}
                                onKeyDown={(e) => e.key === 'Enter' && applyPriceFilter()}
                                inputProps={{ min: 0 }}
                                sx={{ width: '90px' }}
                            />
                        </div>
                        <Button
                            onClick={applyPriceFilter}
                            size='small'
                            className='text bg-teal-600 cursor-pointer font-semibold w-fit mt-2'
                        >
                            Apply
                        </Button>
                    </FormControl>
                </section>
                <Divider />
                <section>
                    <FormControl>
                        <FormLabel
                            sx={{
                                fontSize: "16px",
                                fontWeight: "bold",
                                pb: "14px",
                                color: teal[600]
                            }}
                            className='text-2xl font-semibold'
                            id='brand'
                        >
                            Discount
                        </FormLabel>
                        <RadioGroup
                            name='discount'
                            onChange={updateFilterParams}
                            aria-labelledby='brand'
                            // defaultValue=''
                            value={searchParams.get("discount") || ""}
                        >
                            {discount.map((item, index) => (
                                <FormControlLabel
                                    key={item.name}
                                    value={item.value}
                                    control={<Radio size='small' />}
                                    label={item.name}
                                />
                            ))}
                        </RadioGroup>
                    </FormControl>
                </section>
            </div>

        </div>
    )
}

export default FilterSection
function dispatch(arg0: any) {
    throw new Error('Function not implemented.')
}

