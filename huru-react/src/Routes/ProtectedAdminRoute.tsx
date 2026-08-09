import { Navigate, Outlet } from 'react-router-dom'
import { useAppSelector } from '../State/store'

const ProtectedAdminRoute = () => {
    const { isLoggedIn } = useAppSelector((state) => state.adminAuth)
    return isLoggedIn ? <Outlet /> : <Navigate to="/admin/login" replace />
}

export default ProtectedAdminRoute