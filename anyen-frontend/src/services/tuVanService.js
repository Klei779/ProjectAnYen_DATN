import api from "../api/api.js";

const PUBLIC_BASE_URL = "/api/tu-van";
const STAFF_BASE_URL = "/api/nhan-vien/tu-van";
const guestConfig = () => ({ guestAuth: true });

export const createCustomerChatSession = (tenKhachHang) =>
    api.post(`${PUBLIC_BASE_URL}/phien`, { tenKhachHang });

export const getCustomerChatSession = (tokenPhien) =>
    api.get(
        `${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}`,
        guestConfig()
    );

export const getCustomerMessages = (tokenPhien) =>
    api.get(
        `${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}/tin-nhan`,
        guestConfig()
    );

export const sendCustomerMessage = (tokenPhien, noiDung) =>
    api.post(
        `${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}/tin-nhan`,
        { noiDung },
        guestConfig()
    );

export const markCustomerMessagesRead = (tokenPhien) =>
    api.post(
        `${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}/da-doc`,
        null,
        guestConfig()
    );

export const heartbeatStaffChat = () =>
    api.post(`${STAFF_BASE_URL}/presence/heartbeat`);

export const getStaffPresence = () =>
    api.get(`${STAFF_BASE_URL}/presence/me`);

export const markStaffOffline = () =>
    api.delete(`${STAFF_BASE_URL}/presence/offline`);

export const getStaffChatSessions = () =>
    api.get(`${STAFF_BASE_URL}/phien`);

export const claimStaffChatSession = (maPhien) =>
    api.patch(`${STAFF_BASE_URL}/phien/${maPhien}/nhan`);

export const getStaffMessages = (maPhien) =>
    api.get(`${STAFF_BASE_URL}/phien/${maPhien}/tin-nhan`);

export const sendStaffMessage = (maPhien, noiDung) =>
    api.post(`${STAFF_BASE_URL}/phien/${maPhien}/tin-nhan`, { noiDung });

export const closeStaffChatSession = (maPhien) =>
    api.patch(`${STAFF_BASE_URL}/phien/${maPhien}/dong`);

export const getAiConsultationRequest = (tokenPhien) =>
    api.get(
        `/api/ai/yeu-cau-tu-van/theo-token/${encodeURIComponent(tokenPhien)}`,
        guestConfig()
    );

export function phanTichTinNhanAi(tokenPhien, message) {
    return api.post(
        "/api/ai/yeu-cau-tu-van/phan-tich-tin-nhan",
        {
            tokenPhien,
            message,
        },
        guestConfig()
    );
}
