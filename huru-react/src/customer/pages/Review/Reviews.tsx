import React from 'react'
import ReviewCard from './ReviewCard'
import { Divider } from '@mui/material'

const Reviews = () => {
    return (
        <div className='p-5 lg:px-20 flex flex-col lg:flex-row gap-20'>
            <section className='w-full md:1/2 lg:w-[30%] space-y-2'>
                <img
                    src="https://images.pexels.com/photos/31762174/pexels-photo-31762174/free-photo-of-professional-portrait-in-tan-suit.jpeg?auto=compress&cs=tinysrgb&w=600" alt="" />
                <div>
                    <div>
                        <p className='font-bold text-xl'>Raam Clothing</p>
                        <p className='text-lg text-gray-600'>Women Suit</p>
                    </div>
                </div>
                <div className='price flex items-center gap-3 mt-5 text-2xl' >
                    <span className='font-sans text-gray-800'>
                        $4
                    </span>
                    <span className='line-through text-gray-500'>
                        $5
                    </span>
                    <span className='text-primary-color font-semibold'>
                        60%
                    </span>
                </div>

            </section>
            <section className='space-y-5 w-full'>
                {[1, 1, 1, 1, 1].map((item) =>
                    <div className='space-y-3'>
                        <ReviewCard />
                        <Divider />
                    </div>)}
            </section>

        </div>
    )
}

export default Reviews
