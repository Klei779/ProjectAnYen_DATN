import api from "../api/api.js";

/**
 * Lấy danh sách đơn hàng cho đối tác
 * @param {Object} params - Tham số phân trang, lọc, tìm kiếm
 */
export async function getDonHangsDoiTac(params = {}) {
  try {
    // Dữ liệu Mock JSON dựa theo Database:
    const mockData = [
      {
        maDonHang: 1,
        maCode: "DH001",
        tenKhachHang: "Nguyễn Văn An",
        cccd: "079203001234",
        soDienThoai: "0901234567",
        email: "an@gmail.com",
        diaChi: "Quận 1, TP.HCM",
        ngayDat: "01/06/2026",
        nhanVien: "Võ Thị Mai",
        ghiChu: "Khách yêu cầu tổ chức trong ngày",
        trangThai: "Đã xác nhận",
        trangThaiLichSu: [
          { buoc: "Đặt hàng", thoiGian: "01/06/2026 08:00", trangThai: "done" },
          { buoc: "Đã xác nhận", thoiGian: "01/06/2026 09:00", trangThai: "active" },
          { buoc: "Đang xử lý", thoiGian: null, trangThai: "pending" },
          { buoc: "Hoàn thành", thoiGian: null, trangThai: "pending" }
        ],
        tepDinhKem: { ten: "Hop_dong_DH001.pdf", dungLuong: "1.2 MB", url: "#" },
        sanPhams: [
          { stt: 1, ten: "Quan tài gỗ thông tiêu chuẩn", soLuong: 1, donGia: 8500000, thanhTien: 8500000 },
          { stt: 2, ten: "Vòng hoa chia buồn", soLuong: 1, donGia: 1500000, thanhTien: 1500000 },
          { stt: 3, ten: "Xe tang lễ 16 chỗ", soLuong: 1, donGia: 5000000, thanhTien: 5000000 }
        ],
        tongCong: 15000000
      },
      {
        maDonHang: 2,
        maCode: "DH002",
        tenKhachHang: "Trần Thị Bình",
        cccd: "079204005678",
        soDienThoai: "0912345678",
        email: "binh@gmail.com",
        diaChi: "Thủ Đức, TP.HCM",
        ngayDat: "02/06/2026",
        nhanVien: "Đặng Hoàng Nam",
        ghiChu: "Khách chọn gói cao cấp",
        trangThai: "Đang xử lý",
        trangThaiLichSu: [
          { buoc: "Đặt hàng", thoiGian: "02/06/2026 08:00", trangThai: "done" },
          { buoc: "Đã xác nhận", thoiGian: "02/06/2026 09:00", trangThai: "done" },
          { buoc: "Đang xử lý", thoiGian: "02/06/2026 10:00", trangThai: "active" },
          { buoc: "Hoàn thành", thoiGian: null, trangThai: "pending" }
        ],
        tepDinhKem: { ten: "Hop_dong_DH002.pdf", dungLuong: "2.5 MB", url: "#" },
        sanPhams: [
          { stt: 1, ten: "Quan tài gỗ căm xe cao cấp", soLuong: 1, donGia: 18000000, thanhTien: 18000000 },
          { stt: 2, ten: "Bình tro cốt sứ trắng", soLuong: 1, donGia: 2500000, thanhTien: 2500000 },
          { stt: 3, ten: "Bàn thờ tang lễ", soLuong: 1, donGia: 3200000, thanhTien: 3200000 }
        ],
        tongCong: 23700000
      },
      {
        maDonHang: 3,
        maCode: "DH003",
        tenKhachHang: "Lê Minh Cường",
        cccd: "079205009999",
        soDienThoai: "0987654321",
        email: "cuong@gmail.com",
        diaChi: "Biên Hòa, Đồng Nai",
        ngayDat: "03/06/2026",
        nhanVien: "Võ Thị Mai",
        ghiChu: "Khách cần tư vấn thêm",
        trangThai: "Chờ thanh toán",
        trangThaiLichSu: [
          { buoc: "Đặt hàng", thoiGian: "03/06/2026 08:00", trangThai: "done" },
          { buoc: "Đã xác nhận", thoiGian: null, trangThai: "pending" },
          { buoc: "Đang xử lý", thoiGian: null, trangThai: "pending" },
          { buoc: "Hoàn thành", thoiGian: null, trangThai: "pending" }
        ],
        sanPhams: [
          { stt: 1, ten: "Bình tro cốt sứ trắng", soLuong: 1, donGia: 2500000, thanhTien: 2500000 },
          { stt: 2, ten: "Vòng hoa chia buồn", soLuong: 2, donGia: 1500000, thanhTien: 3000000 }
        ],
        tongCong: 5500000
      }
    ];

    return {
      items: mockData,
      total: mockData.length
    };
  } catch (error) {
    console.error("Lỗi getDonHangsDoiTac:", error);
    throw error;
  }
}

/**
 * Lấy chi tiết đơn hàng cho đối tác
 * @param {String} maDonHang 
 */
export async function getChiTietDonHangDoiTac(maDonHang) {
  try {
    // Để kết nối DB, bỏ comment đoạn code dưới đây:
    // const response = await api.get(`/api/doitac/don-hang/${maDonHang}`);
    // return response.data;
    
    return null; // Handle via mock for now
  } catch (error) {
    console.error("Lỗi getChiTietDonHangDoiTac:", error);
    throw error;
  }
}

/**
 * Cập nhật trạng thái đơn hàng (Ví dụ: Xác nhận đã giao)
 * @param {String} maDonHang 
 * @param {Object} payload 
 */
export async function updateTrangThaiDonHang(maDonHang, payload) {
  try {
    // Cập nhật lên API
    // const response = await api.post(`/api/doitac/don-hang/${maDonHang}/trang-thai`, payload);
    // return response.data;
    
    return { success: true };
  } catch (error) {
    console.error("Lỗi updateTrangThaiDonHang:", error);
    throw error;
  }
}
