import api from "../api/api.js";

// Lấy danh sách thông báo cho nhân viên
export async function getThongBaoNhanVien() {
    const response = await api.get("/api/nhan-vien/thong-bao");
    return response.data;
}

// Đếm thông báo chưa đọc cho nhân viên
export async function countThongBaoChuaDoc() {
    const response = await api.get("/api/nhan-vien/thong-bao/chua-doc");
    return response.data;
}

// Đánh dấu đã đọc một thông báo
export async function danhDauDaDoc(maThongBao) {
    const response = await api.put(`/api/nhan-vien/thong-bao/${maThongBao}/da-doc`);
    return response.data;
}

// Đánh dấu tất cả đã đọc
export async function danhDauTatCaDaDoc() {
    const response = await api.put("/api/nhan-vien/thong-bao/da-doc-tat-ca");
    return response.data;
}

// Chấp nhận thông báo công việc
export async function chapNhanThongBao(maThongBao) {
    const response = await api.put(`/api/nhan-vien/thong-bao/${maThongBao}/chap-nhan`);
    return response.data;
}

// Từ chối thông báo công việc
export async function tuChoiThongBao(maThongBao, lyDo) {
    const response = await api.put(`/api/nhan-vien/thong-bao/${maThongBao}/tu-choi`, { lyDo });
    return response.data;
}

// Lấy thông báo cho hotline
export async function getThongBaoHotline() {
    const response = await api.get("/api/nhan-vien/thong-bao/hotline");
    return response.data;
}

// Giao công việc từ hotline
export async function giaoCongViec(data) {
    const response = await api.post("/api/nhan-vien/thong-bao/giao-cong-viec", data);
    return response.data;
}
