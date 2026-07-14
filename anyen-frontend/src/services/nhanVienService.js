import axios from "axios";

const API = "http://localhost:8080/api/nhan-vien/don-hang";

export const getNhanVienDeXuat = (lat, lng) => {
    const token = localStorage.getItem("token");

    return axios.get(
        `${API}/de-xuat-nhan-vien`,
        {
            params: { lat, lng },
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );
};
