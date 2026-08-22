package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.constants.AppLabels;
import vn.anyen.dto.request.CapNhatNhanVienRequest;
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

    @Transactional
    public QuanLyNhanVienResponse createQuanLyNhanVien(QuanLyNhanVienRequest request) {
        String tenDangNhap = request.getTenDangNhap().trim();
        String email = request.getEmail().trim();
        String soDienThoai = request.getSoDienThoai().trim();

        if (nhanVienRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (nhanVienRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (nhanVienRepository.existsBySoDienThoai(soDienThoai)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        NhanVien nhanVien = NhanVien.builder()
                .hoTen(request.getHoTen().trim())
                .tenDangNhap(tenDangNhap)
                .matKhau(passwordEncoder.encode(request.getMatKhau()))
                .email(email)
                .soDienThoai(soDienThoai)
                .quanHuyen(request.getQuanHuyen().trim())
                .phuongXa(request.getPhuongXa().trim())
                .tinhThanh(request.getTinhThanh().trim())
                .soNhaDuong(request.getSoNhaDuong().trim())
                .vaiTro(request.getVaiTro())
                .trangThai(NhanVien.TRANG_THAI_HOAT_DONG)
                .build();

        return mapToResponse(nhanVienRepository.save(nhanVien));
    }

    /**
     * Cập nhật thông tin nhân viên.
     * SecurityConfig chỉ cho ROLE_ADMIN, tương ứng nhân viên có VaiTro = 1,
     * truy cập endpoint quản lý nhân viên.
     */
    @Transactional
    public QuanLyNhanVienResponse capNhatNhanVien(
            Integer maNhanVien,
            QuanLyNhanVienRequest request
    ) {
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với mã: " + maNhanVien
                ));

        String tenDangNhap = request.getTenDangNhap().trim();
        String email = request.getEmail().trim();
        String soDienThoai = request.getSoDienThoai().trim();

        if (nhanVienRepository.existsByTenDangNhapAndMaNhanVienNot(
                tenDangNhap, maNhanVien)) {
            throw new RuntimeException("Tên đăng nhập đã được nhân viên khác sử dụng");
        }
        if (nhanVienRepository.existsByEmailAndMaNhanVienNot(email, maNhanVien)) {
            throw new RuntimeException("Email đã được nhân viên khác sử dụng");
        }
        if (nhanVienRepository.existsBySoDienThoaiAndMaNhanVienNot(
                soDienThoai, maNhanVien)) {
            throw new RuntimeException("Số điện thoại đã được nhân viên khác sử dụng");
        }

        nhanVien.setHoTen(request.getHoTen().trim());
        nhanVien.setTenDangNhap(tenDangNhap);
        nhanVien.setEmail(email);
        nhanVien.setSoDienThoai(soDienThoai);
           nhanVien.setQuanHuyen(request.getQuanHuyen().trim());
                nhanVien.setPhuongXa(request.getPhuongXa().trim());
           nhanVien.setTinhThanh(request.getTinhThanh().trim());
                nhanVien.setSoNhaDuong(request.getSoNhaDuong().trim());
        nhanVien.setVaiTro(request.getVaiTro());

        // Mật khẩu là tùy chọn khi sửa. Để trống/null thì giữ mật khẩu cũ.
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            nhanVien.setMatKhau(passwordEncoder.encode(request.getMatKhau().trim()));
        }

        return mapToResponse(nhanVienRepository.save(nhanVien));
    }

    @Transactional
    public QuanLyNhanVienResponse nghiViecNhanVien(Integer maNhanVien) {
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với mã: " + maNhanVien
                ));

        if (NhanVien.TRANG_THAI_NGHI_VIEC.equals(nhanVien.getTrangThai())) {
            throw new RuntimeException("Nhân viên này đã nghỉ việc từ trước");
        }

        nhanVien.setTrangThai(NhanVien.TRANG_THAI_NGHI_VIEC);
        return mapToResponse(nhanVienRepository.save(nhanVien));
    }
    @Transactional
    public QuanLyNhanVienResponse khoaTaiKhoan(Integer maNhanVien) {
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với mã: " + maNhanVien
                ));

        if (NhanVien.TRANG_THAI_KHOA.equals(nhanVien.getTrangThai())) {
            throw new RuntimeException("Nhân viên này đã khóa từ trước");
        }

        nhanVien.setTrangThai(NhanVien.TRANG_THAI_KHOA);
        return mapToResponse(nhanVienRepository.save(nhanVien));
    }
    @Transactional
    public QuanLyNhanVienResponse moKhoaTaiKhoan(Integer maNhanVien) {
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy nhân viên với mã: " + maNhanVien
                ));

        if (NhanVien.TRANG_THAI_HOAT_DONG.equals(nhanVien.getTrangThai())) {
            throw new RuntimeException("Nhân viên này đã mở khóa");
        }

        nhanVien.setTrangThai(NhanVien.TRANG_THAI_HOAT_DONG);
        return mapToResponse(nhanVienRepository.save(nhanVien));
    }

    @Transactional(readOnly = true)
    public List<QuanLyNhanVienResponse> getAllNhanVien() {
        return nhanVienRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private QuanLyNhanVienResponse mapToResponse(NhanVien nhanVien) {
        return QuanLyNhanVienResponse.builder()
                .maNhanVien(nhanVien.getMaNhanVien())
                .hoTen(nhanVien.getHoTen())
                .tenDangNhap(nhanVien.getTenDangNhap())
                .email(nhanVien.getEmail())
                .soDienThoai(nhanVien.getSoDienThoai())
                .quanHuyen(nhanVien.getQuanHuyen())
                .phuongXa(nhanVien.getPhuongXa())
                .tinhThanh(nhanVien.getTinhThanh())
                .soNhaDuong(nhanVien.getSoNhaDuong())
                .vaiTro(nhanVien.getVaiTro())
                .trangThai(nhanVien.getTrangThai())
                .tenTrangThai(AppLabels.getLabel(
                        AppLabels.TRANG_THAI_NHAN_VIEN,
                        nhanVien.getTrangThai()
                ))
                .tenVaiTro(AppLabels.getLabel(AppLabels.TEN_VAI_TRO,nhanVien.getVaiTro()))
                .build();
    }
}