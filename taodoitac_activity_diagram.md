# Sơ đồ Activity Diagram - Chức năng Tạo Đối Tác

Dựa trên mã nguồn frontend (`TrangDangKyDoiTac.vue`) và backend (`QuanLyDoiTacService.java`, `DoiTacXacNhanController.java`), dưới đây là sơ đồ hoạt động chi tiết. Đây là quy trình **2 giai đoạn** với sự tham gia của **2 actor**: Quản trị viên (Admin) và Đối tác.

```mermaid
flowchart TD
    subgraph Admin [Quản trị viên - Giao diện Admin]
        A([Bắt đầu]) --> B[Admin nhập Email đối tác\nvà bấm Gửi lời mời]
        B --> C[POST /api/quan-ly/doi-tac\nTạo lời mời đối tác]
    end

    subgraph Backend1 [Backend - Giai đoạn 1: Tạo lời mời]
        C --> D{Email đã tồn tại\ntrong hệ thống?}
        D -- Có --> E[Trả về lỗi:\nEmail đã được sử dụng]
        D -- Không --> F[Tạo bản ghi DoiTac tạm\nTrạng thái CHỜ XÁC NHẬN\nSinh Token ngẫu nhiên UUID]
        F --> G[Lưu DoiTac vào Database]
        G --> H[Gửi Email mời hợp tác\nguiEmailXacNhan\nKèm link chứa Token]
        H --> I[Trả về kết quả thành công]
    end

    subgraph Email [Hộp thư Đối tác]
        I --> J[Đối tác nhận được\nEmail lời mời hợp tác]
        J --> K[Đối tác bấm vào\nLink xác nhận trong Email]
    end

    subgraph Backend2 [Backend - Giai đoạn 2: Xác thực Token từ link]
        K --> L[GET /api/auth/doi-tac/xac-nhan?token=...\nxacNhanDoiTac]
        L --> M{Token hợp lệ\nvà tồn tại?}
        M -- Không --> N[Trả về lỗi:\nToken không hợp lệ]
        M -- Có --> O{Lời mời\nđã hết hạn?}
        O -- Có --> P[Trả về lỗi:\nLời mời hết hạn 24h]
        O -- Không --> Q[Trả về thông tin DoiTac\nkèm Token cho Frontend]
    end

    subgraph Frontend [Giao diện Đăng ký Đối tác - TrangDangKyDoiTac.vue]
        Q --> R[Hiển thị Form điền thông tin\n3 bước: Thông tin - Tài khoản - Điều khoản]
        R --> S[Bước 1: Nhập Thông tin đối tác\nvalidateStepOne]
        S --> S_Check{Hợp lệ?}
        S_Check -- Không --> S_Err[Hiển thị lỗi\nở Bước 1]
        S_Check -- Có --> T[Bước 2: Nhập Tài khoản\nvalidateStepTwo]
        T --> T_Check{Hợp lệ?\nMật khẩu khớp?}
        T_Check -- Không --> T_Err[Hiển thị lỗi\nở Bước 2]
        T_Check -- Có --> U[Bước 3: Đọc Điều khoản\nvalidateStepThree]
        U --> U_Check{Đã đọc hết\nvà đồng ý?}
        U_Check -- Không --> U_Err[Hiển thị lỗi\nở Bước 3]
        U_Check -- Có --> V[Tổng hợp Payload\nkèm Token]
        V --> W[POST /api/auth/doi-tac/ky-hop-dong\nGửi yêu cầu ký hợp đồng]
    end

    subgraph Backend3 [Backend - Giai đoạn 3: Ký hợp đồng và tạo tài khoản]
        W --> X{Token còn\nhợp lệ?}
        X -- Không --> Y[Trả về lỗi:\nToken hết hạn]
        X -- Có --> Z{Tên đăng nhập\nđã tồn tại?}
        Z -- Có --> Z_Err[Trả về lỗi:\nTên đăng nhập đã dùng]
        Z -- Không --> AA{Số điện thoại\nđã tồn tại?}
        AA -- Có --> AA_Err[Trả về lỗi:\nSố điện thoại đã dùng]
        AA -- Không --> AB{Mã số thuế\nđã tồn tại?}
        AB -- Có --> AB_Err[Trả về lỗi:\nMã số thuế đã dùng]
        AB -- Không --> AC[Cập nhật thông tin DoiTac\nMã hóa mật khẩu BCrypt]
        AC --> AD[Chuyển trạng thái\nthành ĐANG HOẠT ĐỘNG]
        AD --> AE[Xóa Token xác nhận\nConfirmationToken = null]
        AE --> AF[Lưu vào Database]
        AF --> AG[Trả về thành công]
    end

    subgraph Result [Kết quả tại Frontend]
        E -. "Sửa lại email" .-> B
        N -. "Liên hệ Admin" .-> End
        P -. "Liên hệ Admin" .-> End
        Y -. "Liên hệ Admin" .-> End
        Z_Err -. "Quay lại sửa" .-> T
        AA_Err -. "Quay lại sửa" .-> S
        AB_Err -. "Quay lại sửa" .-> S
        S_Err -. "Tiếp tục sửa" .-> S
        T_Err -. "Tiếp tục sửa" .-> T
        U_Err -. "Tiếp tục sửa" .-> U
        AG --> AH[Hiển thị Bước 4: Hoàn tất\nĐăng ký thành công!]
    end

    AH --> End([Kết thúc])
```

### Giải thích các điểm quan trọng:

1. **Quy trình 2 actor, 3 giai đoạn:**
   - **Admin** khởi tạo lời mời → **Hệ thống** gửi Email tự động → **Đối tác** nhận link và điền form.
   - Điều này đảm bảo chỉ những đối tác được **Admin duyệt** từ trước mới có thể đăng ký, tránh mở công khai tạo tài khoản tràn lan.

2. **Token bảo mật:**
   - Token là một chuỗi ngẫu nhiên (`UUID`) được tạo ra và lưu trong Database khi Admin gửi lời mời.
   - Token này được nhúng vào link Email. Khi Đối tác bấm link, Backend kiểm tra Token có tồn tại và còn hợp lệ không.
   - Sau khi Ký hợp đồng thành công, **Token bị xóa** (`confirmationToken = null`) để link trong Email không thể dùng lại được nữa.

3. **Form 3 bước (Stepper):**
   - Frontend chia quá trình điền thông tin thành 3 bước rõ ràng, mỗi bước có hàm `validate` riêng (`validateStepOne`, `validateStepTwo`, `validateStepThree`).
   - Ngay khi ấn Submit cuối cùng, Frontend còn validate lại cả 3 bước một lần nữa trước khi gửi API.

4. **Mã hóa mật khẩu:**
   - Mật khẩu đối tác được mã hóa bằng thuật toán `BCrypt` (`passwordEncoder.encode(...)`) trước khi lưu vào Database. Tuyệt đối không lưu mật khẩu dạng bản rõ.
