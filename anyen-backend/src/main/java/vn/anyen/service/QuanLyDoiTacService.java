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

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<QuanLyDoiTacResponse> getAllDoiTac() {
        return doiTacRepository.findAllByOrderByMaDoiTacDesc()
                .stream()
                .filter(doiTac -> !DoiTac.TT_DA_XOA.equals(doiTac.getTrangThai()))
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Tạo đối tác mới với trạng thái "Chờ xác nhận" và gửi email xác nhận.
     * Phần này giữ lại cho người khác làm tiếp.
     */
    @Transactional
    public QuanLyDoiTacResponse createDoiTac(vn.anyen.dto.request.ThemDoiTacRequest request) {

        if (doiTacRepository.existsByEmail(trim(request.getEmail()))) {
            throw new RuntimeException("Email đã được sử dụng bởi đối tác khác");
        }

        String confirmationToken = UUID.randomUUID().toString();

        String tempUsername = "temp_" + UUID.randomUUID().toString().substring(0, 8);
        String tempPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        // Đặt tên tạm thời là phần trước @ của email
        String emailPart = request.getEmail().split("@")[0];

        DoiTac doiTac = DoiTac.builder()
                .tenDoiTac("Đối tác " + emailPart)
                .tenDangNhap(tempUsername)
                .matKhau(tempPassword)
                .email(trim(request.getEmail()))
                .trangThai(DoiTac.TT_CHO_XAC_NHAN)
                .confirmationToken(confirmationToken)
                .build();

        doiTacRepository.save(doiTac);

        guiEmailXacNhan(doiTac, confirmationToken);

        return mapToResponse(doiTac);
    }

    @Transactional
    public QuanLyDoiTacResponse updateDoiTac(Integer maDoiTac, QuanLyDoiTacRequest request) {
        DoiTac doiTac = doiTacRepository.findById(maDoiTac)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác"));

        validateTrungKhiSua(maDoiTac, request);

        doiTac.setTenDoiTac(trim(request.getTenDoiTac()));
        doiTac.setTenDoanhNghiep(trimNullable(request.getTenDoanhNghiep()));
        doiTac.setMaSoThue(trimNullable(request.getMaSoThue()));
        doiTac.setEmail(trim(request.getEmail()));
        doiTac.setSoDienThoai(trim(request.getSoDienThoai()));
        doiTac.setDiaChi(trimNullable(request.getDiaChi()));

        doiTacRepository.save(doiTac);

        return mapToResponse(doiTac);
    }

    @Transactional
    public QuanLyDoiTacResponse updateTrangThai(Integer maDoiTac, Integer trangThai) {
        DoiTac doiTac = doiTacRepository.findById(maDoiTac)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác"));

        if (!DoiTac.TT_DANG_HOAT_DONG.equals(trangThai)
                && !DoiTac.TT_NGUNG_HOAT_DONG.equals(trangThai)) {
            throw new RuntimeException("Trạng thái chỉ được là Đang hoạt động hoặc Ngừng hoạt động");
        }

        doiTac.setTrangThai(trangThai);

        doiTacRepository.save(doiTac);

        return mapToResponse(doiTac);
    }

    @Transactional
    public void deleteDoiTac(Integer maDoiTac) {
        DoiTac doiTac = doiTacRepository.findById(maDoiTac)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác"));

        doiTac.setTrangThai(DoiTac.TT_DA_XOA);

        doiTacRepository.save(doiTac);
    }

    @Transactional
    public DoiTac xacNhanDoiTac(String token) {

        DoiTac doiTac = doiTacRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Token xác nhận không hợp lệ hoặc không tồn tại."));

        if (DoiTac.TT_DA_XOA.equals(doiTac.getTrangThai())) {
            throw new RuntimeException("Lời mời hợp tác đã hết hạn do quá 24h.");
        }

        if (DoiTac.TT_DANG_HOAT_DONG.equals(doiTac.getTrangThai())) {
            return doiTac;
        }

        // Tạm thời để trạng thái Chờ xác nhận trong lúc họ đang điền form
        doiTac.setTrangThai(DoiTac.TT_CHO_XAC_NHAN);
        doiTacRepository.save(doiTac);

        return doiTac;
    }

    @Transactional
    public void kyHopDong(vn.anyen.dto.request.KyHopDongRequest request) {
        DoiTac doiTac = doiTacRepository.findByConfirmationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc không tồn tại. Vui lòng kiểm tra lại link trong email."));

        if (DoiTac.TT_DA_XOA.equals(doiTac.getTrangThai())) {
            throw new RuntimeException("Lời mời hợp tác đã hết hạn do quá 24h. Vui lòng liên hệ với An Yên để được cấp lại lời mời.");
        }

        if (doiTacRepository.existsByTenDangNhap(request.getTenDangNhap())
                && !doiTac.getTenDangNhap().equals(request.getTenDangNhap())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại trong hệ thống. Vui lòng chọn tên khác.");
        }

        if (doiTacRepository.existsBySoDienThoai(trim(request.getSoDienThoai()))) {
            throw new RuntimeException("Số điện thoại đã được sử dụng bởi một tài khoản khác.");
        }

        String maSoThue = trimNullable(request.getMaSoThue());
        if (maSoThue != null && doiTacRepository.existsByMaSoThue(maSoThue)) {
            throw new RuntimeException("Mã số thuế đã tồn tại trong hệ thống.");
        }

        doiTac.setTenDangNhap(trim(request.getTenDangNhap()));
        doiTac.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        doiTac.setTenDoiTac(trim(request.getTenDoiTac()));
        doiTac.setTenDoanhNghiep(trimNullable(request.getTenDoanhNghiep()));
        doiTac.setMaSoThue(trimNullable(request.getMaSoThue()));
        doiTac.setSoDienThoai(trim(request.getSoDienThoai()));
        doiTac.setDiaChi(trimNullable(request.getDiaChi()));

        // Giữ tương thích với code cũ.
        // Nếu muốn đồng bộ trạng thái mới thì có thể đổi "Đã hợp tác" thành "Đang hợp tác".
        doiTac.setTrangThai(DoiTac.TT_DANG_HOAT_DONG);

        doiTac.setConfirmationToken(null);
        doiTacRepository.save(doiTac);
    }

    private void validateTrungKhiSua(Integer maDoiTac, QuanLyDoiTacRequest request) {
        String email = trim(request.getEmail());
        String soDienThoai = trim(request.getSoDienThoai());
        String maSoThue = trimNullable(request.getMaSoThue());

        if (doiTacRepository.existsByEmailAndMaDoiTacNot(email, maDoiTac)) {
            throw new RuntimeException("Email đã được sử dụng bởi đối tác khác");
        }

        if (doiTacRepository.existsBySoDienThoaiAndMaDoiTacNot(soDienThoai, maDoiTac)) {
            throw new RuntimeException("Số điện thoại đã được sử dụng bởi đối tác khác");
        }

        if (maSoThue != null && doiTacRepository.existsByMaSoThueAndMaDoiTacNot(maSoThue, maDoiTac)) {
            throw new RuntimeException("Mã số thuế đã được sử dụng bởi đối tác khác");
        }
    }

    private void guiEmailXacNhan(DoiTac doiTac, String token) {
        try {
            String confirmUrl = "http://localhost:5173/doitac/register?token=" + token;

            Context context = new Context();
            context.setVariable("tenDoiTac", doiTac.getTenDoiTac());
            context.setVariable("tenDoanhNghiep", doiTac.getTenDoanhNghiep());
            context.setVariable("email", doiTac.getEmail());
            context.setVariable("soDienThoai", doiTac.getSoDienThoai());
            context.setVariable("tenDangNhap", doiTac.getTenDangNhap());
            context.setVariable("confirmUrl", confirmUrl);
            context.setVariable("websiteUrl", "http://localhost:5173/");
            context.setVariable("websiteDisplay", "www.anyen.vn");
            context.setVariable("logoUrl", "https://res.cloudinary.com/duongtan/image/upload/v1721030000/logoAnYen.png");
            context.setVariable("footerLogoUrl", "https://res.cloudinary.com/duongtan/image/upload/v1721030000/logoAnYen.png");

            String htmlContent = templateEngine.process("xac-nhan-doi-tac", context);

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

    private QuanLyDoiTacResponse mapToResponse(DoiTac doiTac) {
        return QuanLyDoiTacResponse.builder()
                .maDoiTac(doiTac.getMaDoiTac())
                .tenDoiTac(doiTac.getTenDoiTac())
                .tenDoanhNghiep(doiTac.getTenDoanhNghiep())
                .maSoThue(doiTac.getMaSoThue())
                .tenDangNhap(doiTac.getTenDangNhap())
                .email(doiTac.getEmail())
                .soDienThoai(doiTac.getSoDienThoai())
                .diaChi(doiTac.getDiaChi())
                .trangThai(doiTac.getTrangThai())
                .createdAt(doiTac.getCreatedAt())
                .build();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}