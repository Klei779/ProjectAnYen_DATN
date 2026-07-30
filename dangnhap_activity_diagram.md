# Sơ đồ Activity Diagram - Chức năng Đăng Nhập

Dựa trên mã nguồn frontend (`PopDangNhap.vue`) và backend (`AuthService.java`), dưới đây là sơ đồ hoạt động chi tiết cho luồng đăng nhập.

```mermaid
flowchart TD
    subgraph Frontend [Giao diện - PopDangNhap.vue]
        A([Bắt đầu]) --> B[Người dùng chọn loại tài khoản\nNHÂN VIÊN hoặc ĐỐI TÁC]
        B --> C[Nhập Tên đăng nhập và Mật khẩu]
        C --> D[Bấm nút ĐĂNG NHẬP\nhandleLogin]
        D --> E{Tên đăng nhập\nhoặc mật khẩu trống?}
        E -- Có --> F[Hiển thị lỗi:\nVui lòng nhập đầy đủ]
        E -- Không --> G{Số lần thất bại\n>= 10 lần?}
        G -- Có --> H{Đã xác nhận\nCaptcha Turnstile?}
        H -- Chưa --> I[Hiển thị lỗi:\nVui lòng xác nhận không phải robot]
        H -- Rồi --> J[Gửi yêu cầu POST\n/api/auth/login\nkèm loaiTaiKhoan + captchaToken]
        G -- Không --> J
    end

    subgraph Backend [Backend - AuthService.java]
        J --> K{Có captchaToken\ntrong request?}
        K -- Có --> L[Xác thực Captcha\nvới Cloudflare Turnstile\nverifyTurnstile]
        L --> L_Check{Captcha hợp lệ?}
        L_Check -- Không --> L_Err[Trả về\nsuccess = false]
        L_Check -- Có --> M
        K -- Không --> M{loaiTaiKhoan\nlà gì?}

        M -- NHAN_VIEN --> N[Tìm Nhân viên theo\nTên đăng nhập trong DB]
        N --> N_Check{Tài khoản\ntồn tại?}
        N_Check -- Không --> O[Trả về success = false]
        N_Check -- Có --> P{Trạng thái\nbị vô hiệu hóa?}
        P -- Có --> O
        P -- Không --> Q[So sánh mật khẩu\nbcrypt.matches]
        Q --> Q_Check{Mật khẩu\nkhớp?}
        Q_Check -- Không --> O
        Q_Check -- Có --> R[Xác định Vai trò\nADMIN / HOTLINE / NHANVIEN]
        R --> S[Sinh JWT Token\njwtService.generateToken]
        S --> T[Trả về success = true\nkèm Token + thông tin Nhân viên]

        M -- DOI_TAC --> U[Tìm Đối tác theo\nTên đăng nhập trong DB]
        U --> U_Check{Tài khoản\ntồn tại?}
        U_Check -- Không --> O
        U_Check -- Có --> V[So sánh mật khẩu\nbcrypt.matches]
        V --> V_Check{Mật khẩu\nkhớp?}
        V_Check -- Không --> O
        V_Check -- Có --> W[Sinh JWT Token\nROLE_DOITAC\njwtService.generateToken]
        W --> X[Trả về success = true\nkèm Token + thông tin Đối tác]
    end

    subgraph Frontend_Result [Xử lý kết quả - PopDangNhap.vue]
        L_Err -. "Thử lại" .-> C
        O -. "Tăng loginFailures++" .-> Y[Hiển thị lỗi:\nSai tài khoản hoặc mật khẩu]

        T --> Z{success = true?}
        X --> Z

        Z -- Không --> Y
        Z -- Có --> AA[Lưu Token vào localStorage\nlocalStorage.setItem token]
        AA --> AB[Lưu thông tin User vào localStorage\nlocalStorage.setItem user]
        AB --> AC[Phát sự kiện session-updated\nCập nhật giao diện Header]
        AC --> AD{Vai trò là gì?}

        AD -- DOITAC --> AE[Chuyển trang:\n/doi-tac/thong-tin-tai-khoan]
        AD -- ADMIN --> AF[Chuyển trang:\n/admin/thong-tin-tai-khoan]
        AD -- HOTLINE --> AG[Chuyển trang:\n/hotline/thong-tin-tai-khoan]
        AD -- NHANVIEN --> AH[Chuyển trang:\n/nhan-vien/thong-tin-tai-khoan]
        AD -- Không xác định --> AI[Hiển thị lỗi:\nKhông xác định được quyền truy cập]
    end

    Y -. "Tiếp tục thử lại" .-> C
    AI -. "Tiếp tục thử lại" .-> C
    AE & AF & AG & AH --> End([Kết thúc])
```

### Giải thích các điểm quan trọng:

1. **Chọn loại tài khoản (Tab):**
   - Giao diện có 2 tab: **Nhân viên An Yên** và **Đối tác**.
   - Người dùng chọn tab tương ứng trước khi đăng nhập. Frontend sẽ gửi thêm trường `loaiTaiKhoan = "NHAN_VIEN"` hoặc `"DOI_TAC"` lên Backend để Backend biết cần tìm trong bảng nào.

2. **Chống Bot bằng Captcha (Turnstile):**
   - Sau **10 lần đăng nhập thất bại**, giao diện tự động hiển thị ô xác nhận Captcha của **Cloudflare Turnstile**.
   - Backend cũng tự kiểm tra lại Captcha nếu Frontend gửi kèm token, bằng cách gọi lên API của Cloudflare để xác minh.

3. **Xác thực mật khẩu BCrypt:**
   - Mật khẩu trong Database được lưu dưới dạng **mã hóa BCrypt**. Backend dùng hàm `passwordEncoder.matches(matKhauNguoiDung, matKhauTrongDB)` để so sánh, không bao giờ so sánh trực tiếp bằng chuỗi thô.

4. **JWT Token:**
   - Sau khi xác thực thành công, Backend sinh ra một chuỗi **JWT Token** mã hóa bên trong đó có: ID, tên đăng nhập và vai trò (`ROLE_ADMIN`, `ROLE_HOTLINE`, `ROLE_NHANVIEN`, `ROLE_DOITAC`).
   - Frontend lưu Token vào `localStorage`. Từ đó về sau, mọi request API khác đều gắn Token này vào Header `Authorization: Bearer <token>`.

5. **Điều hướng theo vai trò:**
   - Sau khi đăng nhập thành công, Frontend đọc trường `vaiTroChiTiet` từ response và tự động điều hướng người dùng đến đúng trang Dashboard tương ứng với vai trò của họ (DOITAC, ADMIN, HOTLINE, NHANVIEN).
