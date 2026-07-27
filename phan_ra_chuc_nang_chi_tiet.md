# Sơ Đồ Phân Rã Chức Năng Chi Tiết - Hệ Thống An Yên (Phiên bản Hoàn Chỉnh)

Tài liệu này mô tả chi tiết toàn bộ các phân hệ, chức năng và luồng dữ liệu của hệ thống **An Yên** dựa trên cấu trúc thực tế của mã nguồn Frontend và Backend.

## 1. Biểu Đồ Phân Rã Chức Năng Tổng Quan (Mermaid)

```mermaid
graph TB
    subgraph Website["Website (Public / Khách Hàng)"]
        W_Home["Trang chủ"]
        W_Prod["Sản phẩm & Chi tiết"]
        W_Serv["Dịch vụ & Chi tiết"]
        W_News["Tin tức & Chi tiết"]
        W_About["Giới thiệu & Liên hệ"]
        W_Chat["ChatBox & AI Tư vấn"]
        W_Auth["Đăng nhập / Đăng ký"]
        W_Partner["Xác nhận đối tác"]
    end
    
    subgraph Admin["Admin (Quản Trị Viên)"]
        A_Partner["Quản lý Đối tác"]
        A_Emp["Quản lý Nhân viên"]
        A_Cust["Quản lý Khách hàng"]
        A_Contract["Quản lý Hợp đồng"]
        A_Prod["Duyệt Sản phẩm"]
        A_Debt["Thanh toán Công nợ"]
        A_Rev["Thống kê Doanh thu"]
        A_Notif["Thông báo Hệ thống"]
    end
    
    subgraph NhanVien["Nhân viên"]
        NV_Dash["Tổng quan & Doanh thu"]
        NV_Order["Quản lý & Tạo Đơn hàng"]
        NV_Contract["Quản lý & Tạo Hợp đồng"]
        NV_Cust["Quản lý & Thêm Khách hàng"]
        NV_Inv["Hóa đơn của tôi"]
        NV_Notif["Thông báo cá nhân"]
        NV_Acc["Thông tin tài khoản"]
    end
    
    subgraph DoiTac["Đối tác"]
        DT_Dash["Tổng quan & Doanh thu"]
        DT_Prod["Quản lý & Tạo Sản phẩm"]
        DT_Combo["Quản lý & Tạo Combo"]
        DT_Order["Theo dõi Đơn hàng"]
        DT_Notif["Thông báo Đối tác"]
        DT_Acc["Thông tin & Đổi mật khẩu"]
    end
    
    subgraph Hotline["Hotline (CSKH)"]
        HL_Chat["Nhận & Quản lý tin nhắn"]
        HL_Task["Quản lý Công việc"]
        HL_Order["Tra cứu Đơn hàng"]
        HL_AI["Trợ lý AI (AiChatBox)"]
        HL_Notif["Thông báo ca trực"]
    end
    
    subgraph Backend["Backend Micro-services"]
        B_Auth["Authentication & Security"]
        B_User["Quản lý Users (Admin, NV, ĐT, KH)"]
        B_Prod["Product & Combo Service"]
        B_Order["Order & Contract Service"]
        B_Fin["Finance (Hóa đơn, Công nợ, Thống kê)"]
        B_AI["AI & Ollama Integration"]
        B_Util["Upload & Geocoding & Mail"]
        B_Notif["Realtime Notification Service"]
        B_Content["Dịch vụ & Tin tức"]
    end
    
    %% Flows
    Website --> B_Auth
    Website --> B_Prod
    Website --> B_AI
    Website --> B_Content
    
    Admin --> B_User
    Admin --> B_Order
    Admin --> B_Fin
    Admin --> B_Prod
    
    NhanVien --> B_Order
    NhanVien --> B_User
    NhanVien --> B_Fin
    
    DoiTac --> B_Prod
    DoiTac --> B_Order
    DoiTac --> B_Fin
    
    Hotline --> B_AI
    Hotline --> B_Order
    Hotline --> B_Notif
```

## 2. Biểu Đồ Cấu Trúc Thành Phần (PlantUML)

```plantuml
@startuml PhanRaChucNangChiTiet
skinparam packageStyle rectangle
skinparam backgroundColor #ffffff
skinparam shadowing false

package "Website (Frontend: Vue)" {
    [TrangChu.vue]
    [TrangSanPham.vue]
    [ChiTietSanPham.vue]
    [TrangDichVu.vue]
    [TrangTinTuc.vue]
    [ChatBox.vue]
    [XacNhanDoiTac.vue]
}

package "Admin (Frontend: Vue)" {
    [TrangQLDoiTac.vue]
    [TrangQLNhanVien.vue]
    [TrangQLKhachHang.vue]
    [TrangQLHopDong.vue]
    [TrangDuyetSanPham.vue]
    [TrangThanhToanCongNo.vue]
    [TrangThongBaoAD.vue]
}

package "Nhân Viên (Frontend: Vue)" {
    [TrangQLDonHang.vue]
    [TrangQLHopDong.vue]
    [TrangQLKhachHang.vue]
    [TrangHoaDonCuaToi.vue]
    [TrangThongKeDoanhThuNV.vue]
    [PopTaoDonHang.vue]
    [PopTaoHopDong.vue]
}

package "Đối Tác (Frontend: Vue)" {
    [TrangQLSanPham.vue]
    [TrangTaoSanPham.vue]
    [TrangQLCombo.vue]
    [TrangQLDonHang.vue]
    [TrangThongKeDoanhThuDT.vue]
    [TrangDangKyDoiTac.vue]
}

package "Hotline (Frontend: Vue)" {
    [TrangNhanTin.vue]
    [TrangQLCongViec.vue]
    [TrangQLDonHang.vue]
    [AiChatBox.vue]
}

package "Backend Controllers (Spring Boot)" {
    [AuthController]
    [ThongKeDoanhThuController]
    [AiController / OllamaTestController]
    [DonHangController / HopDongController]
    [SanPhamController / ComboDoiTacController]
    [CongNoController / HoaDonController]
    [UploadController / GeocodingController]
    [ThongBaoController / RealtimeService]
}

"Website (Frontend: Vue)" ..> "Backend Controllers (Spring Boot)"
"Admin (Frontend: Vue)" ..> "Backend Controllers (Spring Boot)"
"Nhân Viên (Frontend: Vue)" ..> "Backend Controllers (Spring Boot)"
"Đối Tác (Frontend: Vue)" ..> "Backend Controllers (Spring Boot)"
"Hotline (Frontend: Vue)" ..> "Backend Controllers (Spring Boot)"

@enduml
```

## 3. Chi Tiết Các Chức Năng Theo Phân Hệ (Role)

### 3.1. Website (Dành cho Khách Hàng / Public)
*Đây là bộ mặt của nền tảng, cho phép khách truy cập tham khảo, tương tác và mua sắm.*
- **Trang Chủ (`TrangChu.vue`):** Hiển thị banner, giới thiệu tổng quan, dịch vụ nổi bật, sản phẩm mới.
- **Sản Phẩm (`TrangSanPham.vue`, `ChiTietSanPham.vue`):** Danh mục sản phẩm tang lễ, bộ lọc, tìm kiếm, xem chi tiết, thông số, hình ảnh.
- **Dịch Vụ (`TrangDichVu.vue`, `TrangDichVuChiTiet.vue`):** Hiển thị các gói dịch vụ, thông tin các hạng mục đi kèm.
- **Tin Tức & Giới Thiệu (`TrangTinTuc.vue`, `ChiTietTinTuc.vue`, `TrangGioiThieu.vue`):** Blog kiến thức, cẩm nang, giới thiệu công ty An Yên.
- **Liên Hệ (`TrangLienHe.vue`, `PopLienHeHotline.vue`):** Form gửi yêu cầu, tích hợp bản đồ.
- **Tương Tác AI & Chat (`ChatBox.vue`):** Khách hàng chat với Hotline hoặc sử dụng trợ lý AI tư vấn tự động.
- **Xác Thực & Đăng Ký (`PopDangNhap.vue`, `XacNhanDoiTac.vue`):** Cho phép các bên đăng nhập, khách hàng xác nhận email lời mời.

### 3.2. Quản Trị Viên (Admin)
*Phân hệ có quyền lực cao nhất, vận hành và kiểm soát chất lượng toàn hệ thống.*
- **Quản lý Nhân Sự (`TrangQLNhanVien.vue`):** Cấp tài khoản, phân quyền, khóa tài khoản nhân viên.
- **Quản lý Đối Tác (`TrangQLDoiTac.vue`):** Duyệt hồ sơ đăng ký đối tác, quản lý thông tin, chấm dứt hợp tác.
- **Quản lý Khách Hàng (`TrangQLKhachHang.vue`):** Theo dõi toàn bộ dữ liệu người dùng, khách hàng của hệ thống.
- **Kiểm Duyệt Nội Dung (`TrangDuyetSanPham.vue`, `PopXemSanPham.vue`):** Xem xét, duyệt hoặc từ chối các sản phẩm/combo do đối tác tải lên trước khi xuất hiện trên Website.
- **Quản lý Giao Dịch (`TrangQLHopDong.vue`):** Theo dõi tiến độ hợp đồng, kiểm soát rủi ro pháp lý.
- **Tài Chính & Kế Toán (`TrangThanhToanCongNo.vue`, `ThongKeDoanhThuController`):** Theo dõi doanh thu tổng, thanh toán phần chia sẻ doanh thu cho đối tác, quản lý công nợ.
- **Hệ Thống Thông Báo (`TrangThongBaoAD.vue`):** Đẩy thông báo (Broadcast) đến các phân hệ khác.

### 3.3. Nhân Viên (NhanVien)
*Lực lượng nòng cốt xử lý nghiệp vụ bán hàng, hợp đồng và chăm sóc khách.*
- **Tổng Quan & Doanh Thu (`TrangTongQuan.vue`, `TrangThongKeDoanhThuNV.vue`):** Xem KPI cá nhân, số lượng đơn hàng/hợp đồng đã chốt.
- **Nghiệp Vụ Đơn Hàng (`TrangQLDonHang.vue`, `PopTaoDonHang.vue`, `PopChiTietDonHang.vue`):** Khởi tạo đơn mới cho khách, cập nhật trạng thái đơn (chờ xử lý -> đang giao -> hoàn thành).
- **Nghiệp Vụ Hợp Đồng (`TrangQLHopDong.vue`, `PopTaoHopDong.vue`, `PreviewHopDong.vue`):** Soạn thảo hợp đồng từ mẫu, xin duyệt, in PDF.
- **Chăm Sóc Khách Hàng (`TrangQLKhachHang.vue`, `PopThemKhachHang.vue`, `PopLichSuKhachHang.vue`):** Quản lý tệp khách hàng cá nhân, xem lịch sử mua hàng, nhu cầu.
- **Nghiệp Vụ Hóa Đơn (`TrangHoaDonCuaToi.vue`, `PopTaoHoaDon.vue`):** Xuất hóa đơn cho đơn hàng/hợp đồng đã hoàn thành.
- **Thông Báo (`TrangThongBaoNV.vue`):** Nhận thông báo công việc, đơn hàng mới.

### 3.4. Đối Tác (DoiTac)
*Nhà cung cấp sản phẩm (vật phẩm tang lễ) tham gia vào hệ sinh thái An Yên.*
- **Đăng Ký Tài Khoản (`TrangDangKyDoiTac.vue`):** Quy trình onboarding cung cấp thông tin doanh nghiệp, chờ Admin duyệt.
- **Dashboard Doanh Số (`TrangTongQuan.vue`, `TrangThongKeDoanhThuDT.vue`):** Thống kê sản phẩm bán chạy, doanh thu thực tế nhận được.
- **Quản Lý Sản Phẩm (`TrangQLSanPham.vue`, `TrangTaoSanPham.vue`):** Đăng tải sản phẩm mới (tên, giá, hình ảnh, mô tả), quản lý tồn kho, ẩn/hiện sản phẩm.
- **Quản Lý Combo (`TrangQLCombo.vue`, `TaoCombo.vue`):** Đóng gói nhiều sản phẩm thành 1 gói combo để tăng doanh số.
- **Quản Lý Đơn Hàng (`TrangQLDonHang.vue`):** Xem các đơn hàng phát sinh chứa sản phẩm của mình, phối hợp giao hàng.
- **Thông Báo & Tài Khoản (`TrangThongBaoDT.vue`, `TrangThongTinTK.vue`):** Nhận thông báo khi sản phẩm được duyệt, đổi mật khẩu.

### 3.5. Hotline (CSKH)
*Đội ngũ trực tổng đài, hỗ trợ giải đáp nhanh cho khách hàng.*
- **Tương Tác Khách Hàng (`TrangNhanTin.vue`, `TrangQuanLyTinNhan.vue`):** Tiếp nhận chat từ Website, trả lời thắc mắc, phân loại yêu cầu.
- **Quản Lý Công Việc (`TrangQLCongViec.vue`):** Theo dõi các ticket, yêu cầu hỗ trợ chưa giải quyết.
- **Tra Cứu Nhanh (`TrangQLDonHang.vue`):** Xem thông tin đơn hàng để báo cáo tình trạng cho khách.
- **Trợ Lý AI (`AiChatBox.vue`):** Sử dụng AI RAG để tra cứu nhanh các chính sách, quy định của công ty hỗ trợ công việc.

## 4. Chi Tiết Backend (API Services - Cốt Lõi Hệ Thống)

Dựa trên cấu trúc Controller thực tế:

| Khối Chức Năng | Thành Phần Controller | Trách Nhiệm Chi Tiết |
|----------------|-----------------------|----------------------|
| **Authentication & Users** | `AuthController`, `TaiKhoanController`, `DoiTacTaiKhoanController`, `NhanVienTaiKhoanController` | Xử lý Login, JWT Token, đổi mật khẩu, phân quyền role, quản lý profile. |
| **Quản Lý Cốt Lõi** | `QuanLyNhanVienController`, `QuanLyKhachHang`, `QuanLyDoiTacController`, `DoiTacXacNhanController` | Các API CRUD dành cho Admin để vận hành hệ thống tài khoản. Xử lý email xác nhận. |
| **Sản Phẩm & Dịch Vụ** | `SanPhamController`, `SanPhamDoiTacController`, `ComboDoiTacController`, `DichVuController` | Xử lý logic đăng sản phẩm, duyệt sản phẩm, tính toán giá trị combo, danh mục dịch vụ. |
| **Giao Dịch (Core)** | `DonHangController`, `NhanVienDonHangController`, `DoiTacDonHangController`, `HopDongController`, `QuanLyHopDongController` | State machine cho Đơn hàng/Hợp đồng. Ràng buộc quyền: NV tạo, ĐT xem, Admin duyệt. |
| **Tài Chính** | `HoaDonController`, `HoaDonCuaToiController`, `CongNoController`, `ThongKeDoanhThuController` | Kết xuất hóa đơn, tính toán doanh thu theo thời gian, tính số tiền An Yên phải trả cho Đối tác (Công nợ). |
| **Tương Tác Khách** | `KhachHangController`, `TuVanKhachController`, `TuVanNhanVienController`, `HotlineCongViecController` | Quản lý logic chat, giao việc cho Hotline, lưu vết lịch sử tư vấn. |
| **Hệ Thống Trợ Lý AI** | `AiController`, `OllamaTestController`, `YeuCauTuVanAiController` | Giao tiếp với model Ollama cục bộ hoặc server AI. Xử lý logic NLP, trả lời tự động cho khách hoặc trợ lý tư vấn cho nhân viên. |
| **Utility & Integration**| `UploadController`, `GeocodingController`, `ThongBaoController`, `DoiTacThongBaoController`, `TinTucController`, `LienHeController` | Upload ảnh lên Cloudinary, tính toán vị trí Google Maps/OSM, gửi thông báo Realtime, quản lý bài viết blog. |

## 5. Ma Trận Quyền Hạn (Role - Access Matrix)

| Chức Năng Cốt Lõi | Website (Khách) | Admin | Nhân Viên | Đối Tác | Hotline |
|-------------------|-----------------|-------|-----------|---------|---------|
| Đăng nhập / Đổi Pass | ❌ (Chỉ xem) | ✅ | ✅ | ✅ | ✅ |
| Xem Sản Phẩm / Dịch Vụ| ✅ | ✅ | ✅ | ✅ | ✅ |
| Đăng Sản Phẩm / Combo | ❌ | ❌ | ❌ | ✅ | ❌ |
| Duyệt Sản Phẩm | ❌ | ✅ | ❌ | ❌ | ❌ |
| Tạo Đơn Hàng / Hợp Đồng| ❌ | ❌ | ✅ | ❌ | ❌ |
| Theo Dõi Đơn Hàng | ❌ | ❌ | ✅ | ✅ | ✅ |
| Hỗ Trợ Trực Tuyến | ✅ (Người hỏi) | ❌ | ❌ | ❌ | ✅ (Người trả lời)|
| Sử Dụng AI Assistant | ✅ (Chatbot) | ❌ | ❌ | ❌ | ✅ (Trợ lý nghiệp vụ)|
| Xem Báo Cáo Doanh Thu | ❌ | ✅ (Tổng) | ✅ (Cá nhân) | ✅ (Sản phẩm ĐT) | ❌ |
| Thanh Toán / Công Nợ | ❌ | ✅ | ❌ | ✅ (Xem) | ❌ |
