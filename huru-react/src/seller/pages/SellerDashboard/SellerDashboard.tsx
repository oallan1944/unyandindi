import React from 'react'
import SellerDrawerlist from '../../components/SellerDrawerlist/SellerDrawerList'
import SellerRoutes from '../../../Routes/SellerRoutes'

const SellerDashboard = () => {

    const toggleDrawer = () => { }
    return (
        <div>
            <div className='lg:flex lg:h-[90vh]'>
                <section className='hidden lg:block h-full '>
                    <SellerDrawerlist toggleDrawer={toggleDrawer} />
                </section>
                <section className='p-10 w-full lg:w-[80%] overflow-y-auto'>
                    <SellerRoutes />
                </section>
            </div>
        </div>
    )
}

export default SellerDashboard
