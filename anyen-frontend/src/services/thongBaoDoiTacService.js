import api from "../api/api.js";

export async function getThongBaoDoiTac() {
    const response = await api.get("/api/doi-tac/thong-bao");
    return response.data;
}

export async function chapNhanThongBao(maThongBao) {
    const response = await api.post(
        `/api/doi-tac/thong-bao/${maThongBao}/chap-nhan`
    );

    return response.data;
}

export async function tuChoiThongBao(maThongBao, lyDo) {
    const response = await api.post(
        `/api/doi-tac/thong-bao/${maThongBao}/tu-choi`,
        { lyDo }
    );

    return response.data;
}