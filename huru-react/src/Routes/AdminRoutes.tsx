import React from 'react'
import { Route, Routes } from 'react-router-dom'
import Sellerstable from '../admin/pages/sellers/Sellerstable'
import Coupon from '../admin/pages/Coupon/Coupon'
import AddnewCoupon from '../admin/pages/Coupon/AddnewCoupon'
import GridTable from '../admin/pages/HomePage/GridTable'
import ElectronicsTable from '../admin/pages/HomePage/ElectricTable'
import ShopByCategoryTable from '../admin/pages/HomePage/ShopByCategoryTable'
import Deal from '../admin/pages/HomePage/Deal'

const AdminRoutes = () => {
    return (
        <div>
            <Routes>
                <Route path='/' element={<Sellerstable />} />
                <Route path='/coupon' element={<Coupon />} />
                <Route path='/add-coupon' element={<AddnewCoupon />} />
                <Route path='/home-grid' element={<GridTable />} />
                <Route path='/electronics-category' element={<ElectronicsTable />} />
                <Route path='/shop-by-category' element={<ShopByCategoryTable />} />
                <Route path='/deals' element={<Deal />} />

            </Routes>
        </div>
    )
}

export default AdminRoutes
