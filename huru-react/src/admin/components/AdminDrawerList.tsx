import { useNavigate } from 'react-router-dom'
import DrawerList from '../../component/DrawerList'
import { useAppDispatch, useAppSelector } from '../../State/store'
import { adminLogout } from '../../State/admin/adminAuthSlice'
import {
    AccountBox, Add, Category, Dashboard, ElectricBolt,
    Home, IntegrationInstructions, LocalOffer, Login, Logout
} from '@mui/icons-material'

const menu = [
    { name: "Dashboard", path: "/admin", icon: <Dashboard className='text-primary-color' />, activeIcon: <Dashboard className='text-white' /> },
    { name: "Coupons", path: "/admin/coupon", icon: <IntegrationInstructions className='text-primary-color' />, activeIcon: <IntegrationInstructions className='text-white' /> },
    { name: "Add New Coupons", path: "/admin/add-coupon", icon: <Add className='text-primary-color' />, activeIcon: <Add className='text-white' /> },
    { name: "Home Page", path: "/admin/home-grid", icon: <Home className='text-primary-color' />, activeIcon: <Home className='text-white' /> },
    { name: "Electronics Category", path: "/admin/electronics-category", icon: <ElectricBolt className='text-primary-color' />, activeIcon: <ElectricBolt className='text-white' /> },
    { name: "Shop By Category", path: "/admin/shop-by-category", icon: <Category className='text-primary-color' />, activeIcon: <Category className='text-white' /> },
    { name: "Deals", path: "/admin/deals", icon: <LocalOffer className='text-primary-color' />, activeIcon: <LocalOffer className='text-white' /> },
];

const AdminDrawerList = ({ toggleDrawer }: any) => {
    const { isLoggedIn } = useAppSelector((state) => state.adminAuth)
    const dispatch = useAppDispatch()
    const navigate = useNavigate()

    const menu2 = [
        {
            name: "Account",
            path: "/admin/account",
            icon: <AccountBox className='text-primary-color' />,
            activeIcon: <AccountBox className='text-white' />
        },
        {
            name: isLoggedIn ? "Logout" : "Login",
            path: isLoggedIn ? "/" : "/admin/login",
            icon: isLoggedIn ? <Logout className='text-primary-color' /> : <Login className='text-primary-color' />,
            activeIcon: isLoggedIn ? <Logout className='text-white' /> : <Login className='text-white' />
        },
    ];

    return (
        <DrawerList
            menu={menu}
            menu2={menu2}
            toggleDrawer={toggleDrawer}
            onLogout={() => dispatch(adminLogout(navigate))}
        />
    )
}

export default AdminDrawerList