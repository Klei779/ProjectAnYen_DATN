import api from "../api/api.js";

const PUBLIC_BASE_URL = "/api/tu-van";
const STAFF_BASE_URL = "/api/nhan-vien/tu-van";

export const createCustomerChatSession = (tenKhachHang) =>
    api.post(`${PUBLIC_BASE_URL}/phien`, { tenKhachHang });

export const getCustomerChatSession = (tokenPhien) =>
    api.get(`${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}`);

export const getCustomerMessages = (tokenPhien) =>
    api.get(`${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}/tin-nhan`);

export const sendCustomerMessage = (tokenPhien, noiDung) =>
    api.post(
        `${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}/tin-nhan`,
        { noiDung }
    );

export const markCustomerMessagesRead = (tokenPhien) =>
    api.post(`${PUBLIC_BASE_URL}/phien/${encodeURIComponent(tokenPhien)}/da-doc`);

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
