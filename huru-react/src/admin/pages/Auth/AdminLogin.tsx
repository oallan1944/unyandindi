import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../../../State/store'
import { sendAdminLoginOtp, adminSignin } from '../../../State/admin/adminAuthSlice'

const AdminLogin = () => {
    const dispatch = useAppDispatch()
    const navigate = useNavigate()
    const { loading, error, otpSent } = useAppSelector((state) => state.adminAuth)

    const [email, setEmail] = useState('')
    const [otp, setOtp] = useState('')

    const handleSendOtp = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!email) return
        await dispatch(sendAdminLoginOtp({ email }))
    }

    const handleVerifyOtp = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!otp) return
        const result = await dispatch(adminSignin({ email, otp }))
        if (adminSignin.fulfilled.match(result)) {
            navigate('/admin')
        }
    }

    return (
        <div className='flex items-center justify-center min-h-screen bg-gray-50'>
            <div className='w-full max-w-sm p-8 bg-white rounded-lg shadow-md'>
                <h1 className='text-2xl font-semibold text-center text-primary-color mb-6'>
                    Admin Login
                </h1>

                {error && <p className='text-sm text-red-500 mb-4 text-center'>{error}</p>}

                {!otpSent ? (
                    <form onSubmit={handleSendOtp} className='space-y-4'>
                        <div>
                            <label className='block text-sm font-medium text-gray-700 mb-1'>Email</label>
                            <input
                                type='email'
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                placeholder='admin@hurubazar.com'
                                className='w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary-color'
                            />
                        </div>
                        <button type='submit' disabled={loading}
                            className='w-full py-2 bg-primary-color text-white rounded-md hover:opacity-90 disabled:opacity-50'>
                            {loading ? 'Sending OTP...' : 'Send OTP'}
                        </button>
                    </form>
                ) : (
                    <form onSubmit={handleVerifyOtp} className='space-y-4'>
                        <div>
                            <label className='block text-sm font-medium text-gray-700 mb-1'>
                                Enter OTP sent to {email}
                            </label>
                            <input
                                type='text'
                                value={otp}
                                onChange={(e) => setOtp(e.target.value)}
                                required
                                placeholder='6-digit code'
                                className='w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-primary-color'
                            />
                        </div>
                        <button type='submit' disabled={loading}
                            className='w-full py-2 bg-primary-color text-white rounded-md hover:opacity-90 disabled:opacity-50'>
                            {loading ? 'Verifying...' : 'Verify & Login'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    )
}

export default AdminLogin