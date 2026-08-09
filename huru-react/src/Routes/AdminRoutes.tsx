import { Route, Routes } from 'react-router-dom'
import Sellerstable from '../admin/pages/sellers/Sellerstable'
import Coupon from '../admin/pages/Coupon/Coupon'
import AddnewCoupon from '../admin/pages/Coupon/AddnewCoupon'
import GridTable from '../admin/pages/HomePage/GridTable'
import ElectronicsTable from '../admin/pages/HomePage/ElectricTable'
import ShopByCategoryTable from '../admin/pages/HomePage/ShopByCategoryTable'
import Deal from '../admin/pages/HomePage/Deal'
import AdminLogin from '../admin/pages/Auth/AdminLogin'
import ProtectedAdminRoute from '../Routes/ProtectedAdminRoute'

const AdminRoutes = () => {
    return (
        <div>
            <Routes>
                {/* Public — must stay outside the guard */}
                <Route path='/login' element={<AdminLogin />} />

                {/* Everything else requires an active admin session */}
                <Route element={<ProtectedAdminRoute />}>
                    <Route path='/' element={<Sellerstable />} />
                    <Route path='/coupon' element={<Coupon />} />
                    <Route path='/add-coupon' element={<AddnewCoupon />} />
                    <Route path='/home-grid' element={<GridTable />} />
                    <Route path='/electronics-category' element={<ElectronicsTable />} />
                    <Route path='/shop-by-category' element={<ShopByCategoryTable />} />
                    <Route path='/deals' element={<Deal />} />
                </Route>
            </Routes>
        </div>
    )
}

export default AdminRoutes