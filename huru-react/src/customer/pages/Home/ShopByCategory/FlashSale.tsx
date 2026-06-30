import React from 'react'
import { HomeCategory } from '../../../../types/HomeCategoryType'

const FlashSale = ({ item }: { item: HomeCategory }) => {
    return (
        <div className='flex flex-col items-center group cursor-pointer gap-2 justify-center'>
            <div className='custom-border w-[350px] h-[450px] lg:w-[350px] lg:h-[500px] rounded-md bg-charcoal overflow-hidden mx-4'>
                <img
                    className='border-x-[7px] border-t-[7px] border-electric-card-color w-full h-[20rem] object-cover object-top group-hover:scale-95 transition-transform duration-700'
                    //className='w-full h-full object-cover object-top rounded-md group-hover:scale-95 transition-transform duration-700'
                    src={item.image}
                    alt={item.name}
                />
            </div>
            <h1 className="text-center text-base">{item.name}</h1>
        </div>
    )
}

export default FlashSale
