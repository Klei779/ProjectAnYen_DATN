# Sơ đồ Activity Diagram - Chức năng Tạo Sản Phẩm của Đối Tác

Dựa trên mã nguồn frontend (`TrangTaoSanPham.vue`) và backend (`SanPhamDoiTacService.java`), dưới đây là sơ đồ hoạt động (Activity Diagram) chi tiết cho luồng tạo sản phẩm mới của đối tác:

```mermaid
flowchart TD
    subgraph Frontend [Giao diện Đối tác - TrangTaoSanPham.vue]
        A([Bắt đầu]) --> B[Người dùng nhập thông tin sản phẩm]
        B --> V_FE[Kiểm tra tính hợp lệ dữ liệu\nvalidateProduct]
        V_FE --> V_Check{Dữ liệu hợp lệ?}
        V_Check -- Không --> V_Err[Báo lỗi & Cuộn đến ô bị lỗi\nscrollToFirstError]
        V_Check -- Có --> C[Tải lên hình ảnh chính và gallery\nUpload API]
        
        C --> D{Có hình ảnh trong chi tiết?}
        D -- Có --> E[Tải lên hình ảnh chi tiết\nUpload API]
        D -- Không --> F[Tạo Payload Request\nBuild Payload]
        E --> F
        F --> G[Gửi yêu cầu POST\n/api/doi-tac/san-pham]
    end

    subgraph Backend [Hệ thống Backend - SanPhamDoiTacService.java]
        G --> H[Trích xuất mã đối tác từ Token\ngetMaDoiTac]
        H --> I{Xác thực Token hợp lệ?}
        I -- Không --> J[Trả về lỗi 401/403]
        I -- Có --> K[Kiểm tra dữ liệu đầu vào\nvalidateRequest]
        K --> L{Dữ liệu hợp lệ?}
        L -- Không --> M[Trả về lỗi 400 Bad Request]
        L -- Có --> N[Kiểm tra Đối tác trong DB]
        N --> O{Đối tác tồn tại?}
        O -- Không --> P[Trả về lỗi 404 Not Found]
        O -- Có --> Q[Khởi tạo Entity SanPham\nMap dữ liệu từ Request]
        Q --> R[Thiết lập trạng thái\nCHỜ XÁC NHẬN]
        R --> S[Lưu Sản Phẩm\nsanPhamDoiTacRepository.save]
        S --> T[Lưu Chi tiết sản phẩm\nsanPhamChiTietRepository.save]
        T --> U[Lưu Hình ảnh sản phẩm\nsanPhamHinhAnhRepository.save]
        U --> V[Tạo Thông Báo DUYET_SAN_PHAM\nCho nhân viên duyệt]
        V --> W[Lưu Thông Báo\nthongBaoRepository.save]
        W --> X[Trả về kết quả\nSanPhamDoiTacResponse]
    end

    subgraph Database [Cơ sở dữ liệu]
        S -.-> DB1[(Bảng san_pham)]
        T -.-> DB2[(Bảng san_pham_chi_tiet)]
        U -.-> DB3[(Bảng san_pham_hinh_anh)]
        W -.-> DB4[(Bảng thong_bao)]
    end

    subgraph Frontend_Feedback [Xử lý kết quả tại Frontend]
        J --> Y[Hiển thị lỗi hệ thống/xác thực]
        M --> Y
        P --> Y
        X --> Z[Hiển thị thông báo thành công,\nReset Form / Chuyển trang]
    end

    V_Err -. "Tiếp tục sửa lỗi" .-> B
    Y -. "Tiếp tục nhập liệu" .-> B
    Z --> End([Kết thúc])
```

### Giải thích các bước quan trọng:

1. **Frontend**: 
   - Trước khi upload ảnh hay gửi API, hệ thống chạy hàm `validateProduct()` để kiểm tra các trường bắt buộc (tên, giá tiền > 0, số lượng không âm, tôn giáo, màu sắc...). Nếu lỗi sẽ hiển thị cảnh báo đỏ và tự động cuộn màn hình tới ô bị lỗi.
   - Đối tác cần upload ảnh lên server trước qua API `/api/upload` để lấy URL ảnh, sau đó mới tổng hợp URL ảnh vào Payload.
   - Có 2 loại ảnh cần upload: Ảnh gallery (ảnh sản phẩm) và ảnh nằm trong nội dung mô tả chi tiết.
   
2. **Backend**:
   - Backend lấy ID đối tác trực tiếp từ Token bảo mật (đảm bảo tính toàn vẹn, đối tác này không thể tạo sản phẩm cho đối tác khác).
   - **Quy tắc nghiệp vụ quan trọng**: Khi đối tác tạo sản phẩm, sản phẩm không được bán ngay mà bắt buộc gán `trangThai = TRANG_THAI_CHO_XAC_NHAN`.
   - Một thông báo loại `DUYET_SAN_PHAM` được sinh ra tự động gửi cho nhân viên quản trị để họ vào kiểm tra và duyệt sản phẩm này.
   - Dữ liệu được lưu vào 3 bảng chính về sản phẩm: `san_pham`, `san_pham_chi_tiet`, `san_pham_hinh_anh` và 1 bảng `thong_bao`.
