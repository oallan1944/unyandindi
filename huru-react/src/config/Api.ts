import axios from "axios"

export const API_URL = "http://localhost:5454"

export const api = axios.create({
    baseURL: API_URL,
    headers: {
        "Content-Type": "application/json",
    }
})

api.interceptors.request.use((config) => {
    // Admin-facing endpoints are split across two prefixes in this backend:
    // /api/admin/** (AdminController, AdminCouponController, CategoryController)
    // and a few standalone /admin/** routes with no /api prefix
    // (HomeCategoryController, DealController). Both need admin_jwt attached.
    const isAdminRoute =
        config.url?.startsWith("/api/admin") ||
        config.url?.startsWith("/admin/home-category") ||
        config.url?.startsWith("/admin/deals")

    const token = isAdminRoute
        ? localStorage.getItem("admin_jwt")
        : localStorage.getItem("jwt")

    if (token) {
        config.headers = config.headers ?? {}
        config.headers.Authorization = `Bearer ${token}`
    }

    return config
})

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            const isAdminRoute =
                error.config?.url?.startsWith("/api/admin") ||
                error.config?.url?.startsWith("/admin/home-category") ||
                error.config?.url?.startsWith("/admin/deals")
            if (isAdminRoute) {
                localStorage.removeItem("admin_jwt")
                localStorage.removeItem("admin_role")
                if (window.location.pathname.startsWith("/admin")) {
                    window.location.href = "/admin/login"
                }
            }
        }
        return Promise.reject(error)
    }
)