import { useEffect } from 'react'
import AdminDrawerList from '../../components/AdminDrawerList'
import AdminRoutes from '../../../Routes/AdminRoutes'
import { useAppDispatch } from '../../../State/store'
import { fetchHomeCategories } from '../../../State/admin/AdminSlice'

const AdminDashboard = () => {
    const toggleDrawer = () => { }
    const dispatch = useAppDispatch();

    useEffect(() => {
        dispatch(fetchHomeCategories())
    }, [])
    return (
        <div>
            <div className='lg:flex lg:h-[90vh]'>
                <section className='hidden lg:block h-full '>
                    <AdminDrawerList toggleDrawer={toggleDrawer} />
                </section>
                <section className='p-10 w-full lg:w-[80%] overflow-y-auto'>
                    <AdminRoutes />
                </section>
            </div>
        </div>
    )
}

export default AdminDashboard
