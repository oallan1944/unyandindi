import React from 'react'
import { HomeCategory } from '../../../../types/HomeCategoryType'
import { Button } from '@mui/material'

const getImageSrc = (image: string) => {
    if (image.startsWith('http')) return image;
    return `/` + image.replace(/^\/+/, ''); // remove any leading slashes and prefix one
};


const ElectricCategoryCard2 = ({ item }: { item: HomeCategory }) => {
    return (
        <div className='grid grid-cols-1 sm:grid-cols-2 bg-brandBlue-color mx-auto overflow-hidden rounded-lg h-full '>
            <div className='flex flex-col justify-center gap-4 sm:pl-3  sm:py-0 text-center sm:text-left order-2 sm:order-1 relative z-1
                        '>
               <h1 className="text-2xl lg:text-4xl font-bold text-white text-center w-full animate-flash">
              Flash Sale
            </h1>
                <h1 className=' text-5x1 sm:text-6x1 lg:text-7x1 font-bold'>
                    {item.name}
                </h1>
                <h1
                    className=' text-5x1 sm:text-6x1 lg:text-7x1 font-bold text-brandWhite-color'>
                    20% off
                </h1>

                <Button variant='contained' className=' text-white cursor-pointer hover:scale-105 duration-300 py-2 px-4  hover:bg-white relative z-1'>
                    Explore
                </Button>
            </div>

            {/* image section */}
            <div className='order-1 sm:order-2'>
                <div>
                    <img src=
                        //  {getImageSrc(item.image)} alt={item.name}
                        // {item.image}

                        "Assets/electricAssets/headphone.png"
                        alt=""
                        className='w-[120px] h-[120px] sm:h-[180px] 
                        sm:scale-105 lg:scale-110 object-contain
                        mx-auto drop-shadow-[-8px_4px_6px_rgba(0,0,0,.4)]
                        relative z-1 '
                    />
                </div>
            </div>

        </div>
    )
}

export default ElectricCategoryCard2
