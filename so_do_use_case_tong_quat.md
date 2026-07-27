# Sơ Đồ Use Case Tổng Quát - Hệ Thống An Yên

Tài liệu này cung cấp **Sơ đồ Use Case (Biểu đồ ca sử dụng)** thể hiện tổng quát mọi chức năng, hành vi của các tác nhân (Actor) khi tương tác với hệ thống An Yên.

## 1. Sơ Đồ Use Case (PlantUML)

Bạn có thể render đoạn code PlantUML dưới đây trong các công cụ hỗ trợ (như PlantUML plugin, planttext.com) để xem dưới dạng hình ảnh.

```plantuml
@startuml UseCaseTongQuat
left to right direction
skinparam packageStyle rectangle
skinparam actorStyle hollow
skinparam usecase {
    BackgroundColor #f9f9f9
    BorderColor #2a75d3
    ArrowColor #2a75d3
}

' ================= ACTORS =================
actor "Người Dùng (Chung)" as User
actor "Khách Hàng" as KH
actor "Nhân Viên" as NV
actor "Đối Tác" as DT
actor "Hotline (CSKH)" as HL
actor "Admin" as AD

' Kế thừa Actor: Các role cụ thể đều kế thừa quyền từ Người Dùng (Chung)
KH -up-|> User
NV -up-|> User
DT -up-|> User
HL -up-|> User
AD -up-|> User

' ================= USE CASES =================
package "Hệ Thống An Yên" {

    ' Nhóm chung
    usecase "Đăng nhập / Đổi mật khẩu" as UC_Login
    usecase "Nhận & Xem Thông báo" as UC_Notif

    ' Khách hàng
    usecase "Xem Sản Phẩm, Dịch Vụ, Tin Tức" as UC_ViewInfo
    usecase "Tìm kiếm & Lọc" as UC_Search
    usecase "Chat / Liên hệ tư vấn" as UC_ChatKH
    usecase "Đăng ký trở thành Đối tác" as UC_RegPartner

    ' Admin
    usecase "Quản lý Nhân Viên" as UC_QLNV
    usecase "Quản lý Khách Hàng (Admin)" as UC_QLKH_AD
    usecase "Duyệt Hồ Sơ Đối Tác" as UC_DuyetDT
    usecase "Duyệt Sản Phẩm" as UC_DuyetSP
    usecase "Duyệt Hợp Đồng" as UC_DuyetHD
    usecase "Quản lý Công Nợ & Thanh toán" as UC_CongNo
    usecase "Thống kê Doanh Thu (Tổng)" as UC_ReportAD
    usecase "Phát hành Thông Báo" as UC_SendNotif

    ' Nhân viên
    usecase "Quản lý Đơn Hàng" as UC_QLDon
    usecase "Quản lý Hợp Đồng (NV)" as UC_QLHD_NV
    usecase "Quản lý Khách Hàng (NV)" as UC_QLKH_NV
    usecase "Quản lý Hóa Đơn" as UC_QLHoaDon
    usecase "Xem Doanh Thu Cá Nhân" as UC_ReportNV

    ' Đối tác
    usecase "Quản lý Sản Phẩm (Tạo, Sửa)" as UC_QLSP_DT
    usecase "Quản lý Combo Sản Phẩm" as UC_QLCombo
    usecase "Theo Dõi Đơn Hàng Của Mình" as UC_TrackOrder
    usecase "Xem Doanh Số & Công Nợ" as UC_ReportDT

    ' Hotline
    usecase "Tiếp nhận Chat & Trả lời KH" as UC_ReplyChat
    usecase "Quản lý Công Việc (Tickets)" as UC_QLTask
    usecase "Tra Cứu Đơn Hàng Nhanh" as UC_LookupOrder
    usecase "Sử Dụng AI Hỗ Trợ" as UC_UseAI
}

' ================= RELATIONS =================

' User Chung
User --> UC_Login
User --> UC_Notif

' Khách Hàng
KH --> UC_ViewInfo
KH --> UC_Search
KH --> UC_ChatKH
KH --> UC_RegPartner

' Admin
AD --> UC_QLNV
AD --> UC_QLKH_AD
AD --> UC_DuyetDT
AD --> UC_DuyetSP
AD --> UC_DuyetHD
AD --> UC_CongNo
AD --> UC_ReportAD
AD --> UC_SendNotif

' Nhân Viên
NV --> UC_QLDon
NV --> UC_QLHD_NV
NV --> UC_QLKH_NV
NV --> UC_QLHoaDon
NV --> UC_ReportNV

' Đối Tác
DT --> UC_QLSP_DT
DT --> UC_QLCombo
DT --> UC_TrackOrder
DT --> UC_ReportDT

' Hotline
HL --> UC_ReplyChat
HL --> UC_QLTask
HL --> UC_LookupOrder
HL --> UC_UseAI

' Includes / Extends (Ví dụ minh họa luồng)
UC_DuyetSP .u.> UC_QLSP_DT : <<extends>>
UC_DuyetHD .u.> UC_QLHD_NV : <<extends>>
UC_CongNo .u.> UC_ReportDT : <<extends>>
UC_ReplyChat .u.> UC_ChatKH : <<extends>>

@enduml
```

## 2. Giải Thích Các Actor (Tác nhân)

| Actor | Mô tả |
|-------|-------|
| **Khách Hàng** | Người dùng phổ thông truy cập Website (có thể chưa đăng nhập hoặc đã đăng nhập). Nhu cầu chính là tham khảo vật phẩm, dịch vụ tang lễ, cập nhật kiến thức và liên hệ tư vấn. |
| **Nhân Viên** | Người xử lý nghiệp vụ bán hàng. Có nhiệm vụ lập hợp đồng, tạo đơn hàng cho khách hàng, xuất hóa đơn và theo dõi tiến độ dịch vụ. |
| **Đối Tác** | Các nhà cung cấp, xưởng sản xuất vật phẩm tang lễ. Cung cấp sản phẩm lên hệ thống để hưởng phần trăm/giá trị đơn hàng. |
| **Hotline (CSKH)** | Bộ phận trực tổng đài và chat trực tuyến, giúp giải đáp thắc mắc, điều phối yêu cầu cho khách hàng với sự hỗ trợ của Trợ lý AI. |
| **Admin** | Quản trị viên cấp cao nhất. Có quyền quyết định duyệt đối tác, duyệt các sản phẩm mới, kiểm soát hợp đồng, theo dõi báo cáo doanh thu và thanh toán công nợ. |

## 3. Danh Sách Use Case Quan Trọng

### 3.1. Nhóm Dùng Chung
* **Đăng nhập / Đổi mật khẩu:** Dành cho tất cả các tài khoản nội bộ (Admin, NV, ĐT, Hotline) và khách hàng.
* **Nhận & Xem Thông báo:** Quản lý thông báo công việc, trạng thái đơn hàng (sử dụng Realtime).

### 3.2. Nhóm Khách Hàng
* **Xem Sản Phẩm, Dịch Vụ, Tin Tức:** Tìm hiểu các thông tin công khai về dịch vụ mai táng, vật phẩm, cẩm nang.
* **Chat / Liên hệ tư vấn:** Tương tác với Hotline hoặc Chatbot AI để hỏi đáp, yêu cầu dịch vụ.
* **Đăng ký trở thành Đối tác:** Gửi hồ sơ công ty và chờ hệ thống (Admin) duyệt.

### 3.3. Nhóm Quản Trị Viên (Admin)
* **Quản lý Nhân Viên:** Khởi tạo, cấp tài khoản và phân quyền cho nhân sự.
* **Duyệt Hồ Sơ Đối Tác:** Xác nhận tư cách Đối tác (được bán hàng trên nền tảng) qua Email và Hợp đồng hợp tác.
* **Duyệt Sản Phẩm / Hợp Đồng:** Kiểm duyệt các sản phẩm mới do Đối tác tải lên. Duyệt tính hợp lệ của Hợp đồng do Nhân viên lập.
* **Quản lý Công Nợ:** Xem đối soát số tiền cần trả cho từng Đối tác tương ứng với số lượng hàng họ đã bán được trên hệ thống.

### 3.4. Nhóm Nhân Viên
* **Quản lý Đơn Hàng:** Lên đơn vật phẩm, cập nhật trạng thái giao hàng, liên hệ khách.
* **Quản lý Hợp Đồng:** Khởi tạo hợp đồng mẫu cho khách, nạp thông tin để chuyển trạng thái chờ Duyệt.
* **Quản lý Hóa Đơn:** Lập hóa đơn khi chốt xong đơn hàng/hợp đồng.

### 3.5. Nhóm Đối Tác
* **Quản lý Sản Phẩm / Combo:** Nhập thông tin (tên, giá, ảnh...) lên hệ thống chờ Admin duyệt. Tạo các gói Combo để tối ưu doanh thu.
* **Theo Dõi Đơn Hàng Của Mình:** Xem đơn hàng có liên quan tới sản phẩm của mình, biết được có bao nhiêu hàng đã bán.

### 3.6. Nhóm Hotline
* **Tiếp nhận Chat & Trả lời KH:** Giao tiếp trực tiếp với khách từ Website.
* **Sử Dụng AI Hỗ Trợ:** Dùng RAG/Ollama để tra cứu nhanh cẩm nang, quy trình tang lễ giúp phản hồi khách 1 cách chính xác.
* **Quản lý Công Việc (Tickets):** Theo dõi các task cần xử lý trong ca trực.
