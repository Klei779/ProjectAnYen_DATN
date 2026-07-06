package vn.anyen.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.anyen.entity.DoiTac;
import vn.anyen.repository.DoiTacRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class QuanLyDoiTacService {

    @Autowired
    private DoiTacRepository doiTacRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${jwt.secret:defaultSecretKeyForJwt123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public List<DoiTac> getAllDoiTac() {
        return doiTacRepository.findAll();
    }

    @Transactional
    public void inviteDoiTac(String email) {
        Optional<DoiTac> existing = doiTacRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new RuntimeException("Email này đã được sử dụng hoặc đã được mời.");
        }

        // Tạo đối tác tạm
        DoiTac doiTac = new DoiTac();
        doiTac.setEmail(email);
        doiTac.setTenDangNhap(email); // Tạm thời dùng email làm tên đăng nhập
        doiTac.setTenDoiTac("Đối tác mới (" + email + ")");
        doiTac.setMatKhau(passwordEncoder.encode("TEMP_PASSWORD")); // Mật khẩu tạm
        doiTac.setTrangThai(DoiTac.TRANG_THAI_CHO_XAC_NHAN);
        
        doiTacRepository.save(doiTac);

        // Tạo token
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L)) // 3 ngày
                .signWith(key)
                .compact();

        // Gửi email HTML
        String link = frontendUrl + "/doitac/register?token=" + token;
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("Thư Mời Hợp Tác - Dịch Vụ Mai Táng An Yên");
            
            String htmlContent = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 0; background-color: #fafafa; border-radius: 12px; overflow: hidden; border: 1px solid #e0e0e0;\">"
                    + "<div style=\"background-color: #ffffff; padding: 25px 20px; text-align: center; border-bottom: 3px solid #d32f2f;\">"
                    + "<h1 style=\"color: #d32f2f; margin: 0; font-size: 32px; font-weight: 700; font-family: 'Georgia', serif; font-style: italic;\">An Yên</h1>"
                    + "<p style=\"color: #b71c1c; margin: 5px 0 0 0; font-size: 11px; font-weight: 600; letter-spacing: 1.5px;\">NƠI GỬI TRỌN NIỀM TIN</p>"
                    + "</div>"
                    + "<div style=\"padding: 40px 30px; background-color: #ffffff;\">"
                    + "<h2 style=\"color: #333333; margin-top: 0; font-size: 20px;\">Kính chào Quý Đối tác,</h2>"
                    + "<p style=\"color: #555555; line-height: 1.6; font-size: 15px;\">Bạn đã nhận được lời mời hợp tác chiến lược từ hệ thống <strong>Dịch vụ mai táng An Yên</strong>. Chúng tôi rất hân hạnh được đồng hành cùng bạn để mang đến những dịch vụ trọn vẹn và ý nghĩa nhất.</p>"
                    + "<p style=\"color: #555555; line-height: 1.6; font-size: 15px;\">Để bắt đầu, vui lòng nhấn vào nút bên dưới để hoàn tất hồ sơ đăng ký đối tác. Lời mời này có hiệu lực trong vòng <strong>3 ngày</strong>.</p>"
                    + "<div style=\"text-align: center; margin: 35px 0;\">"
                    + "<a href=\"" + link + "\" style=\"display: inline-block; padding: 14px 35px; background-color: #d32f2f; color: #ffffff; text-decoration: none; font-weight: bold; border-radius: 50px; font-size: 15px; box-shadow: 0 4px 10px rgba(211,47,47,0.3);\">Hoàn tất Đăng ký ngay</a>"
                    + "</div>"
                    + "<p style=\"color: #757575; font-size: 13px; line-height: 1.5; text-align: center;\">Nếu nút bấm không hoạt động, bạn có thể sao chép và dán đường dẫn sau vào trình duyệt:<br><a href=\"" + link + "\" style=\"color: #d32f2f; word-break: break-all;\">" + link + "</a></p>"
                    + "</div>"
                    + "<div style=\"background-color: #b71c1c; padding: 25px 20px; text-align: center; color: #ffffff;\">"
                    + "<p style=\"font-size: 14px; margin: 0 0 10px 0; font-weight: bold;\">CÔNG TY CỔ PHẦN DỊCH VỤ AN YÊN</p>"
                    + "<p style=\"font-size: 12px; margin: 0 0 5px 0; opacity: 0.8;\">123 Đường An Lạc, Phường Yên Hòa, Quận Cầu Giấy, Hà Nội</p>"
                    + "<p style=\"font-size: 12px; margin: 0; opacity: 0.8;\">Hotline: 1900 1234 &nbsp;|&nbsp; Email: info@anyen.vn</p>"
                    + "<p style=\"font-size: 11px; margin: 15px 0 0 0; opacity: 0.6; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 15px;\">&copy; 2026 Dịch vụ mai táng An Yên. Bảo lưu mọi quyền.</p>"
                    + "</div>"
                    + "</div>";
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gửi email mời đối tác: " + e.getMessage());
        }
    }

    @Transactional
    public void registerDoiTac(String token, String tenDoiTac, String tenDoanhNghiep, String maSoThue, 
                               String tenDangNhap, String matKhau, String soDienThoai, String diaChi) {
        String email;
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            email = claims.getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("Lời mời hợp tác đã hết hạn.");
        } catch (io.jsonwebtoken.JwtException e) {
            throw new RuntimeException("Đường dẫn đăng ký không hợp lệ.");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực đường dẫn: " + e.getMessage());
        }

        DoiTac doiTac = doiTacRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đối tác với email này."));

        if (!doiTac.getTrangThai().equals(DoiTac.TRANG_THAI_CHO_XAC_NHAN)) {
            throw new RuntimeException("Lời mời này không còn hiệu lực hoặc đã được đăng ký.");
        }

        Optional<DoiTac> checkTenDangNhap = doiTacRepository.findByTenDangNhap(tenDangNhap);
        if (checkTenDangNhap.isPresent() && !checkTenDangNhap.get().getMaDoiTac().equals(doiTac.getMaDoiTac())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại, vui lòng chọn tên đăng nhập khác.");
        }

        doiTac.setTenDoiTac(tenDoiTac);
        doiTac.setTenDoanhNghiep(tenDoanhNghiep);
        doiTac.setMaSoThue(maSoThue);
        doiTac.setTenDangNhap(tenDangNhap);
        doiTac.setMatKhau(passwordEncoder.encode(matKhau));
        doiTac.setSoDienThoai(soDienThoai);
        doiTac.setDiaChi(diaChi);
        doiTac.setTrangThai(DoiTac.TRANG_THAI_HOAT_DONG);
        
        doiTacRepository.save(doiTac);
    }

    // Chạy mỗi giờ kiểm tra các lời mời quá hạn 3 ngày
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cancelExpiredInvitations() {
        LocalDateTime baNgayTruoc = LocalDateTime.now().minusDays(3);
        List<DoiTac> expiredList = doiTacRepository.findByTrangThaiAndCreatedAtBefore(DoiTac.TRANG_THAI_CHO_XAC_NHAN, baNgayTruoc);
        
        for (DoiTac dt : expiredList) {
            dt.setTrangThai(DoiTac.TRANG_THAI_NGUNG_HOAT_DONG);
            doiTacRepository.save(dt);
        }
    }
}
