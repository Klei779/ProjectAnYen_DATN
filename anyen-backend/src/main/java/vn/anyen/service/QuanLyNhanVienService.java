package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Đảm bảo tính nhất quán dữ liệu
import vn.anyen.dto.request.QuanLyNhanVienRequest;
import vn.anyen.dto.response.QuanLyNhanVienResponse;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.NhanVienRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuanLyNhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1. Chức năng: Thêm mới nhân viên (Đã dọn dẹp dòng code trùng)
     */
    @Transactional
    public QuanLyNhanVienResponse createQuanLyNhanVien(QuanLyNhanVienRequest request) {

        if (nhanVienRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (nhanVienRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (nhanVienRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        // Tối ưu: Đã xóa đoạn check existsByTenDangNhap bị lặp lại ở đây

        NhanVien nhanVien = NhanVien.builder()
                .hoTen(request.getHoTen().trim())
                .tenDangNhap(request.getTenDangNhap().trim())
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .email(request.getEmail().trim())
                .soDienThoai(request.getSoDienThoai().trim())
                .diaChi(request.getDiaChi())
                .vaiTro(request.getVaiTro())
                .trangThai("Đang làm việc")
                .build();

        nhanVienRepository.save(nhanVien);

        return mapToResponse(nhanVien);
    }

    /**
     * 2. BỔ SUNG: Chức năng cho nhân viên nghỉ việc
     */
    @Transactional
    public QuanLyNhanVienResponse nghiViecNhanVien(Integer maNhanVien) {
        // Tìm nhân viên trong DB, không thấy thì báo lỗi
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với mã: " + maNhanVien));

        // Kiểm tra nếu trạng thái đã là nghỉ việc từ trước
        if ("NGHI_VIEC".equalsIgnoreCase(nhanVien.getTrangThai())) {
            throw new RuntimeException("Nhân viên này đã nghỉ việc từ trước");
        }

        // Cập nhật trạng thái mới và lưu lại
        nhanVien.setTrangThai("NGHI_VIEC");
        nhanVienRepository.save(nhanVien);

        return mapToResponse(nhanVien);
    }

    /**
     * 3. BỔ SUNG: Chức năng lấy danh sách tất cả nhân viên
     */
    @Transactional(readOnly = true) // Tối ưu hiệu năng khi chỉ đọc dữ liệu
    public List<QuanLyNhanVienResponse> getAllNhanVien() {
        // Lấy toàn bộ danh sách Entity dưới Database lên
        List<NhanVien> dsNhanVien = nhanVienRepository.findAll();

        // Sử dụng Stream API để chuyển đổi (map) toàn bộ List Entity thành List Response DTO
        return dsNhanVien.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Hàm Helper dùng chung: Chuyển đổi từ Entity sang Response DTO (Clean Code)
     */
    private QuanLyNhanVienResponse mapToResponse(NhanVien nhanVien) {
        return QuanLyNhanVienResponse.builder()
                .maNhanVien(nhanVien.getMaNhanVien())
                .hoTen(nhanVien.getHoTen())
                .tenDangNhap(nhanVien.getTenDangNhap())
                .email(nhanVien.getEmail())
                .soDienThoai(nhanVien.getSoDienThoai())
                .diaChi(nhanVien.getDiaChi())
                .vaiTro(nhanVien.getVaiTro())
                .trangThai(nhanVien.getTrangThai())
                .build();
    }
}