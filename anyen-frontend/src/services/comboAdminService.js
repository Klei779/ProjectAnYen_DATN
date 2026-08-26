import api from "../api/api.js";

const BASE_URL = "/api/admin/combo";

const buildMultipart = (data, coverFiles = [], processFiles = []) => {
    const payload = new FormData();
    payload.append(
        "data",
        new Blob([JSON.stringify(data)], { type: "application/json" })
    );

    coverFiles.forEach((file) => {
        payload.append("anhDaiDien", file);
    });

    processFiles.forEach((file) => {
        payload.append("anhQuyTrinh", file);
    });

    return payload;
};

export const getCombosAdmin = () => api.get(BASE_URL);

export const getComboAdmin = (comboId) =>
    api.get(`${BASE_URL}/${comboId}`);

export const getSanPhamComboAdmin = () =>
    api.get(`${BASE_URL}/san-pham`);

export const createComboAdmin = (
    data,
    coverFiles,
    processFiles
) => api.post(
    BASE_URL,
    buildMultipart(data, coverFiles, processFiles),
    { timeout: 120000 }
);

export const updateComboAdmin = (
    comboId,
    data,
    coverFiles,
    processFiles
) => api.put(
    `${BASE_URL}/${comboId}`,
    buildMultipart(data, coverFiles, processFiles),
    { timeout: 120000 }
);

export const updateTrangThaiComboAdmin = (comboId, trangThai) =>
    api.patch(`${BASE_URL}/${comboId}/trang-thai`, { trangThai });