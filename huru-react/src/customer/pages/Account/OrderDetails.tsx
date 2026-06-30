
import { Box, Button, Divider } from '@mui/material'
import React, { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import OrderStepper from './OrderStepper'
import PaymentsIcon from '@mui/icons-material/Payments';
import { useAppDispatch, useAppSelector } from '../../../State/store';
import { fetchOrderById, fetchOrderItemById } from '../../../State/customer/orderSlice';



const OrderDetails = () => {
    const navigate = useNavigate()
    const { orderId, orderItemId } = useParams();
    const { order } = useAppSelector(store => store);

    const dispatch = useAppDispatch();
    useEffect(() => {
        dispatch(fetchOrderById({ orderId: Number(orderId), jwt: localStorage.getItem("jwt") || "" }))
        dispatch(fetchOrderItemById({
            orderItemId: Number(orderItemId), jwt: localStorage.getItem
                ("jwt") || ""
        }))
    }, [])
    return (
        <Box className='space-y-5' >
            <section className='flex flex-col gap-5 justify-center items-center'>
                <img className='w-[100px]' src={order.orderItem?.product.images[0]} alt="" />
                <div className='text-sm space-y-1 text-center' >
                    <h1 className='font-bold'>
                        {order.orderItem?.product.seller?.businessDetails.businessName}
                    </h1>
                    <p>
                        {order.orderItem?.product.title}
                    </p>
                    <p>
                        <strong>size : </strong>
                        M
                    </p>
                </div>
                <div>
                    <Button onClick={() => navigate(`/reviews/${5}/create`)}>
                        Write Review
                    </Button>
                </div>
            </section>
            <section className='border p-5'>
                <OrderStepper orderStatus={"SHIPPED"} />
            </section>
            <div className='border p-5'>
                <h1 className='font-bold pb-3'>Delivery Address</h1>
                <div className='text-sm space-y-2'>
                    <div className='flex gap-5 font-medium'>
                        <p>{order.currentOrder?.shippingAddress.name}</p>
                        <Divider flexItem orientation='vertical' />
                        <p>{order.currentOrder?.shippingAddress.mobile}</p>
                    </div>
                    <p>
                        {order.currentOrder?.shippingAddress.address}{" "}
                        {order.currentOrder?.shippingAddress.state}{" "}
                        {order.currentOrder?.shippingAddress.city},{" -"}
                        {order.currentOrder?.shippingAddress.pinCode}
                    </p>
                </div>
            </div>
            <div className='border space-y-4'>
                <div className='flex justify-between text-sm pt-5 px-5'>
                    <div className='space-y-1'>
                        <p className='font-bold'>Total Item Price</p>
                        <p>You Saved <span className='text-green-500 font-mediun text-xs'> ${"2"}.00</span> On this Item</p>

                    </div>
                    <p className='font-medium'>${order.orderItem?.product.sellingPrice}.00</p>
                </div>
                <div className='px-5'>
                    <div className='bg-teal-50 px-5 py-2 text-xs font-medium flex items-center gap-3'>
                        <PaymentsIcon />
                        <p>Pay On Delivery</p>
                    </div>
                </div>

                <Divider />
                <div className='px-5 pb-5'>
                    <p className='text-xs'>
                        Sold by:
                        <strong>
                            {order.orderItem?.product.seller?.businessDetails.businessName}
                        </strong></p>
                </div>
                <div className='p-10'>
                    <Button
                        disabled={false}
                        color='error' sx={{ py: "0.7rem" }} className='' variant='outlined' fullWidth
                    >
                        {false ? "order canceled" : "Cancel Order"}
                    </Button>
                </div>
            </div>
        </Box>
    )
}

export default OrderDetails
