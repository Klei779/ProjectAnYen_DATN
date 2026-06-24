package vn.anyen.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.anyen.dto.request.QuanLyDoiTacRequest;
import vn.anyen.dto.response.QuanLyDoiTacResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.repository.DoiTacRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuanLyDoiTacService {

    private final DoiTacRepository doiTacRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Tạo đối tác mới với trạng thái "Chờ xác nhận" và gửi email xác nhận
     */
    @Transactional
    public QuanLyDoiTacResponse createDoiTac(QuanLyDoiTacRequest request) {

        if (doiTacRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng bởi đối tác khác");
        }

        if (doiTacRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng bởi đối tác khác");
        }

        if (request.getMaSoThue() != null && !request.getMaSoThue().isBlank()
                && doiTacRepository.existsByMaSoThue(request.getMaSoThue())) {
            throw new RuntimeException("Mã số thuế đã tồn tại trong hệ thống");
        }

        // Sinh token xác nhận
        String confirmationToken = UUID.randomUUID().toString();
        
        // Sinh tài khoản tạm thời để vượt qua ràng buộc NOT NULL của database
        String tempUsername = "temp_" + UUID.randomUUID().toString().substring(0, 8);
        String tempPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        // Tạo entity đối tác
        DoiTac doiTac = DoiTac.builder()
                .tenDoiTac(request.getTenDoiTac().trim())
                .tenDoanhNghiep(request.getTenDoanhNghiep() != null ? request.getTenDoanhNghiep().trim() : null)
                .maSoThue(request.getMaSoThue() != null ? request.getMaSoThue().trim() : null)
                .soTaiKhoan(request.getSoTaiKhoan() != null ? request.getSoTaiKhoan().trim() : null)
                .nganHang(request.getNganHang() != null ? request.getNganHang().trim() : null)
                .tenDangNhap(tempUsername)
                .matKhau(tempPassword)
                .email(request.getEmail().trim())
                .soDienThoai(request.getSoDienThoai().trim())
                .diaChi(request.getDiaChi() != null ? request.getDiaChi().trim() : null)
                .trangThai("Chờ xác nhận")
                .confirmationToken(confirmationToken)
                .build();

        doiTacRepository.save(doiTac);

        // Gửi email xác nhận
        guiEmailXacNhan(doiTac, confirmationToken);

        return mapToResponse(doiTac);
    }

    /**
     * Xác nhận đối tác qua token từ email (Bước 1)
     */
    @Transactional
    public DoiTac xacNhanDoiTac(String token) {

        DoiTac doiTac = doiTacRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Token xác nhận không hợp lệ hoặc đã hết hạn"));

        if ("Đã xác nhận".equals(doiTac.getTrangThai()) || "Đã hợp tác".equals(doiTac.getTrangThai())) {
            // Không throw error nếu đã xác nhận, chỉ trả về để cho họ đi tiếp (mở lại trang).
            return doiTac;
        }

        // Cập nhật trạng thái thành Đã xác nhận (chưa ký hợp đồng nên chưa xóa token)
        doiTac.setTrangThai("Đã xác nhận");
        doiTacRepository.save(doiTac);

        return doiTac;
    }

    /**
     * Ký hợp đồng, lưu tài khoản/mật khẩu và đổi trạng thái (Bước 2)
     */
    @Transactional
    public void kyHopDong(vn.anyen.dto.request.KyHopDongRequest request) {
        DoiTac doiTac = doiTacRepository.findByConfirmationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ. Vui lòng kiểm tra lại link trong email."));

        if (doiTacRepository.existsByTenDangNhap(request.getTenDangNhap()) 
            && !doiTac.getTenDangNhap().equals(request.getTenDangNhap())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại trong hệ thống. Vui lòng chọn tên khác.");
        }

        doiTac.setTenDangNhap(request.getTenDangNhap().trim());
        doiTac.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        doiTac.setTrangThai("Đã hợp tác");
        doiTac.setConfirmationToken(null);
        doiTacRepository.save(doiTac);
    }

    /**
     * Gửi email HTML xác nhận hợp tác đến đối tác
     */
    private void guiEmailXacNhan(DoiTac doiTac, String token) {
        try {
            String confirmUrl = "http://localhost:8080/api/auth/doi-tac/xac-nhan?token=" + token;

            // Chuẩn bị dữ liệu cho template Thymeleaf
            Context context = new Context();
            context.setVariable("tenDoiTac", doiTac.getTenDoiTac());
            context.setVariable("tenDoanhNghiep", doiTac.getTenDoanhNghiep());
            context.setVariable("email", doiTac.getEmail());
            context.setVariable("soDienThoai", doiTac.getSoDienThoai());
            context.setVariable("tenDangNhap", doiTac.getTenDangNhap());
            context.setVariable("confirmUrl", confirmUrl);

            String htmlContent = templateEngine.process("xac-nhan-doi-tac", context);

            // Tạo email MIME (HTML)
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(doiTac.getEmail());
            helper.setSubject("🤝 An Yên — Lời Mời Hợp Tác Chính Thức");
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email xác nhận đến đối tác: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi Entity sang Response DTO
     */
    private QuanLyDoiTacResponse mapToResponse(DoiTac doiTac) {
        return QuanLyDoiTacResponse.builder()
                .maDoiTac(doiTac.getMaDoiTac())
                .tenDoiTac(doiTac.getTenDoiTac())
                .tenDoanhNghiep(doiTac.getTenDoanhNghiep())
                .maSoThue(doiTac.getMaSoThue())
                .soTaiKhoan(doiTac.getSoTaiKhoan())
                .nganHang(doiTac.getNganHang())
                .tenDangNhap(doiTac.getTenDangNhap())
                .email(doiTac.getEmail())
                .soDienThoai(doiTac.getSoDienThoai())
                .diaChi(doiTac.getDiaChi())
                .trangThai(doiTac.getTrangThai())
                .createdAt(doiTac.getCreatedAt())
                .build();
    }
}
