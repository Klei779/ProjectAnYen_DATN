import axios from "axios";

const CUSTOMER_CHAT_STORAGE_KEY = "anyen_customer_chat_session";

const api = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 30000,
});

function getGuestChatToken() {
    try {
        const session = JSON.parse(
            localStorage.getItem(CUSTOMER_CHAT_STORAGE_KEY) || "null"
        );
        return session?.guestToken || null;
    } catch {
        return null;
    }
}

api.interceptors.request.use(
    (config) => {
        if (config.guestAuth) {
            const guestToken = getGuestChatToken();

            if (guestToken) {
                config.headers.Authorization = `Bearer ${guestToken}`;
            } else {
                delete config.headers.Authorization;
            }

            return config;
        }

        const token = localStorage.getItem("token");

        const isPublicAiApi =
            config.url === "/api/ai/chat" ||
            config.url === "/api/ai/health";

        if (token && !isPublicAiApi) {
            config.headers.Authorization = `Bearer ${token}`;
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
            if (error.config?.guestAuth) {
                localStorage.removeItem(CUSTOMER_CHAT_STORAGE_KEY);
                window.dispatchEvent(new Event("guest-chat-expired"));
            } else {
                localStorage.removeItem("token");
                localStorage.removeItem("user");
                window.dispatchEvent(new Event("session-updated"));
            }
        }

        return Promise.reject(error);
    }
);

export default api;
