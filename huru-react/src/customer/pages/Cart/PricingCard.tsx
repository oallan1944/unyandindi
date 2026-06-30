import { Divider } from '@mui/material'
import React from 'react'
import Cart from './Cart'
import { useAppSelector } from '../../../State/store'


const PricingCard = () => {
    const { cart } = useAppSelector((state) => state.cart)
    return (
        <>
            <div className='space-y-3 p-5'>
                <div className='flex justify-between items-center'>
                    <span> Subtotal</span>
                    <span> ${cart?.totalMrpPrice ?? 0}</span>
                </div>
                <div className='flex justify-between items-center'>
                    <span> Discount</span>
                    <span> ${cart ? cart.totalMrpPrice - cart.totalSellingPrice : 0}</span>
                </div>
                <div className='flex justify-between items-center'>
                    <span> Shipping</span>
                    <span> $3</span>
                </div>
                <div className='flex justify-between items-center'>
                    <span> Platform Fee</span>
                    <span> Free</span>
                </div>
            </div>
            <Divider />
            <div className='flex justify-between items-center p-5 text-primary-color'>
                <span> Total</span>
                <span> $
                    {cart ? cart.totalSellingPrice + 3 // adding shipping fee
                        : 0}
                </span>
            </div>
        </>
    )
}

export default PricingCard
