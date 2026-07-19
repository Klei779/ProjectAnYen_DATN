import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 30000
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");

        const isPublicAiApi =
            config.url === "/api/ai/chat" ||
            config.url === "/api/ai/health";

        if (token && !isPublicAiApi) {
            config.headers.Authorization =
                `Bearer ${token}`;
        } else {
            delete config.headers.Authorization;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = error.response?.status;

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