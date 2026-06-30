import React, { useEffect } from 'react'
import { useAppDispatch, useAppSelector } from '../../../State/store'
import { fetchUserOrderHistory } from '../../../State/customer/orderSlice'
import OrdaItem from './OrdaItem'

const Orders = () => {
    const dispatch = useAppDispatch()
    const { order } = useAppSelector(store => store)
    useEffect(() => {
        dispatch(fetchUserOrderHistory(localStorage.getItem("jwt") || ""))
    }, [])
    return (
        <div className='text-sm min-h-screen'>
            <div className=' pb-5'>
                <h1 className='font-semibold'> All Orders</h1>
                <p>From Anytime</p>
            </div>
            <div className='space-y-2'>
                {order.orders.map((order) =>
                    order.orderItems.map((item) =>
                        <OrdaItem order={order} item={item} />))}
            </div>
        </div>
    )
}

export default Orders
