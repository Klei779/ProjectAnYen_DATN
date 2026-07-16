import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 30000
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;

        /*
         * Chỉ xóa đăng nhập khi token không hợp lệ/hết hạn.
         * 403 chỉ là không đủ quyền, không được logout.
         */
        if (status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("user");

            window.dispatchEvent(
                new Event("session-updated")
            );
        }

        return Promise.reject(error);
    }
);

export default api;