import api from "../api/api.js";

export const getNhanVienDeXuat = (lat, lng) => {
    return api.get(
        "/api/nhan-vien/don-hang/de-xuat-nhan-vien",
        {
            params: {
                lat,
                lng
            }
        }
    );
};
