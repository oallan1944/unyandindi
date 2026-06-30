import React, { useEffect, useState } from 'react';
import StarIcon from '@mui/icons-material/Star';
import { teal } from '@mui/material/colors';
import { Button, Divider } from '@mui/material';
import {
    Add, AddShoppingCart, FavoriteBorder, LocalShipping, Remove,
    Shield, Wallet, WorkspacePremium
} from '@mui/icons-material';
import SimilarProducts from './SimilarProducts';
import ReviewCard from '../Review/ReviewCard';
import { useParams } from 'react-router-dom';
import { fetchProductById } from '../../../State/customer/ProductSlice';
import { useAppDispatch, useAppSelector } from '../../../State/store';
import useAddToCart from '../Cart/AddItemToCart';
import ColorSelect from '../../../component/common/ColorSelect';
import SizeSelect from '../../../component/common/SizeSelect';

const ProductDetails: React.FC = () => {
    const [quantity, setQuantity] = useState<number>(1);
    const dispatch = useAppDispatch();
    const { productId } = useParams();
    const { product } = useAppSelector(store => store);
    const [activeImage, setActiveImage] = useState<number>(0);

    const [selectedColor, setSelectedColor] = useState<string>('');


    const addToCart = useAddToCart();

    useEffect(() => {
        if (productId) {
            dispatch(fetchProductById(Number(productId)));
        }
    }, [productId]);

    const handleActiveImage = (value: number) => () => {
        setActiveImage(value);
    };

    // const uniqueColors = Array.from(
    //     new Set(product.product?.variants?.map(v => v.color))
    // );

    // const uniqueSizes = Array.from(
    //     new Set(product.product?.variants?.map(v => v.size))
    // );
    // inside component body

    console.log("sizes value:", product.product?.sizes);
    console.log("product.product?.variants:", product.product?.variants);
    console.log("Full product object:", product.product);

    const availableSizes = product.product?.sizes ?? Array.from(
        new Set(product.product?.variants?.map(v => v.size))
    );

    // const availableColors = Array.from(
    //     new Set(product.product?.variants?.map(v => v.color))
    // );


    const availableColors = product.product?.variants && product.product.variants.length
        ? Array.from(new Set(product.product.variants.map(v => v.color)))
        : product.product?.color
            ? [product.product.color]
            : [];


    // const availableColors = product?.variants
    //     ? Array.from(new Set(product.variants.map(v => v.color)))
    //     : [];


    const [selectedSize, setSelectedSize] = useState<string>(availableSizes[0] || '');


    return (
        <div className='px-5 lg:px-20 pt-10'>
            <div className='grid grid-cols-1 lg:grid-cols-2 gap-10'>
                <section className='flex flex-col lg:flex-row gap-5'>
                    <div className='w-full lg:w-[15%] flex flex-wrap lg:flex-col gap-3'>
                        {product.product?.images.map((item, index) => (
                            <img
                                key={index}
                                onClick={handleActiveImage(index)}
                                className='lg:w-full w-[50px] cursor-pointer rounded-md'
                                src={item}
                                alt={`product-${index}`}
                            />
                        ))}
                    </div>
                    <div className='w-full lg:w-[85%]'>
                        <img
                            className='w-full rounded-md'
                            src={product.product?.images[activeImage]}
                            alt={product.product?.title}
                        />
                    </div>
                </section>

                <section>
                    <h1 className='font-bold text-lg text-primary-color'>
                        {product.product?.seller?.businessDetails.businessName}
                    </h1>
                    <p className='text-gray-500 font-semibold'>{product.product?.title}</p>

                    <div className='flex justify-between items-center py-2 border w-[180px] px-3 mt-5'>
                        <div className='flex gap-1 items-center'>
                            <span>4</span>
                            <StarIcon sx={{ color: teal[500], fontSize: "17px" }} />
                        </div>
                        <Divider orientation='vertical' flexItem />
                        <span>234 Ratings</span>
                    </div>

                    <div className='price flex items-center gap-3 mt-5 text-2xl'>
                        <span className='font-sans text-gray-800'>${product.product?.sellingPrice}</span>
                        <span className='line-through text-gray-500'>${product.product?.mrpPrice}</span>
                        <span className='text-primary-color font-semibold'>
                            {product.product?.discountPercent}%
                        </span>
                    </div>

                    <div className='mt-7 space-y-3'>
                        <div className='flex items-center gap-4'>
                            <Shield sx={{ color: teal[500] }} />
                            <p>Authentic & Quality Assured</p>
                        </div>
                        <div className='flex items-center gap-4'>
                            <WorkspacePremium sx={{ color: teal[500] }} />
                            <p>100% Money back guarantee</p>
                        </div>
                        <div className='flex items-center gap-4'>
                            <LocalShipping sx={{ color: teal[500] }} />
                            <p>Free Shipping & Returns</p>
                        </div>
                        <div className='flex items-center gap-4'>
                            <Wallet sx={{ color: teal[500] }} />
                            <p>Pay on delivery might be available</p>
                        </div>
                    </div>

                    {/* Color Selection */}
                    <div className='mt-7'>
                        <h1 className='font-semibold'>SELECT COLOR</h1>
                        <ColorSelect
                            colors={availableColors}
                            selectedColor={selectedColor}
                            onSelectColor={(color) => setSelectedColor(color)}
                        />
                    </div>





                    {/* Size Selection */}
                    <div className='mt-7'>
                        <h1 className='font-semibold'>SELECT SIZE</h1>
                        <SizeSelect
                            sizes={availableSizes}
                            selectedSize={selectedSize}
                            onSelectSize={(size) => setSelectedSize(size)}
                        />
                    </div>


                    {/* Quantity */}
                    <div className='mt-7 space-y-2'>
                        <h1>QUANTITY</h1>
                        <div className='flex items-center gap-2 w-[140px] justify-between'>
                            <Button disabled={quantity === 1} onClick={() => setQuantity(quantity - 1)}>
                                <Remove />
                            </Button>
                            <span>{quantity}</span>
                            <Button onClick={() => setQuantity(quantity + 1)}>
                                <Add />
                            </Button>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className='mt-12 flex items-center gap-5'>
                        <Button
                            onClick={() => {
                                if (product.product?.id) {
                                    addToCart(product.product.id, "M", quantity);
                                }
                            }}
                            fullWidth
                            variant='contained'
                            startIcon={<AddShoppingCart />}
                            sx={{ py: '1rem' }}
                        >
                            Add to Cart
                        </Button>

                        <Button
                            fullWidth
                            variant='outlined'
                            startIcon={<FavoriteBorder />}
                            sx={{ py: '1rem' }}
                        >
                            Wishlist
                        </Button>
                    </div>

                    <div className='mt-5'>
                        <p>{product.product?.description}</p>
                    </div>

                    <div className='mt-10 space-y-5'>
                        <ReviewCard />
                        <Divider />
                    </div>
                </section>
            </div>

            <div className='mt-20'>
                <h1 className='text-lg font-bold'>Similar Products</h1>
                <div className='pt-5'>
                    <SimilarProducts />
                </div>
            </div>
        </div>
    );
};

export default ProductDetails;
