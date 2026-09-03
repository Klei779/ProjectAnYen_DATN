package vn.anyen.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.QuenMatKhauRequest;
import vn.anyen.dto.response.QuenMatKhauResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class QuenMatKhauService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    /*
     * Bỏ các ký tự dễ nhầm:
     * I, O, l, o, 0, 1.
     */
    private static final String UPPERCASE =
            "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final String LOWERCASE =
            "abcdefghijkmnopqrstuvwxyz";

    private static final String DIGITS =
            "23456789";

    private static final String SPECIALS =
            "@#$%";

    private static final String ALL_CHARACTERS =
            UPPERCASE
                    + LOWERCASE
                    + DIGITS
                    + SPECIALS;

    private final NhanVienRepository nhanVienRepository;

    private final DoiTacRepository doiTacRepository;

    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Nếu quá trình gửi mail bị lỗi thì transaction bị rollback,
     * mật khẩu cũ trong database vẫn được giữ lại.
     */
    @Transactional
    public QuenMatKhauResponse quenMatKhau(
            QuenMatKhauRequest request
    ) {
        String email = request
                .getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        String matKhauMoi =
                taoMatKhauNgauNhien(10);

        String tenNguoiDung;
        String tenDangNhap;

        if (
                "NHAN_VIEN".equals(
                        request.getLoaiTaiKhoan()
                )
        ) {
            NhanVien nhanVien =
                    nhanVienRepository
                            .findByEmailIgnoreCase(email)
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Không tìm thấy tài khoản nhân viên có email này"
                                    )
                            );

            if (
                    !NhanVien.TRANG_THAI_HOAT_DONG
                            .equals(
                                    nhanVien.getTrangThai()
                            )
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tài khoản nhân viên đã ngừng hoạt động"
                );
            }

            /*
             * Database chỉ lưu mật khẩu đã mã hóa BCrypt.
             */
            nhanVien.setMatKhau(
                    passwordEncoder.encode(
                            matKhauMoi
                    )
            );

            /*
             * Thực hiện update database ngay trong transaction.
             */
            nhanVienRepository.saveAndFlush(
                    nhanVien
            );

            tenNguoiDung =
                    nhanVien.getHoTen();

            tenDangNhap =
                    nhanVien.getTenDangNhap();

        } else if (
                "DOI_TAC".equals(
                        request.getLoaiTaiKhoan()
                )
        ) {
            DoiTac doiTac =
                    doiTacRepository
                            .findByEmailIgnoreCase(email)
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Không tìm thấy tài khoản đối tác có email này"
                                    )
                            );

            if (
                    !DoiTac.TT_DANG_HOAT_DONG
                            .equals(
                                    doiTac.getTrangThai()
                            )
            ) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tài khoản đối tác chưa hoạt động hoặc đã bị khóa"
                );
            }

            doiTac.setMatKhau(
                    passwordEncoder.encode(
                            matKhauMoi
                    )
            );

            doiTacRepository.saveAndFlush(
                    doiTac
            );

            tenNguoiDung =
                    doiTac.getTenDoiTac();

            tenDangNhap =
                    doiTac.getTenDangNhap();

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Loại tài khoản không hợp lệ"
            );
        }

        /*
         * Gửi mật khẩu chưa mã hóa cho người dùng.
         */
        guiEmailMatKhauMoi(
                email,
                tenNguoiDung,
                tenDangNhap,
                matKhauMoi
        );

        return QuenMatKhauResponse.builder()
                .success(true)
                .message(
                        "Mật khẩu mới đã được gửi đến email của bạn"
                )
                .build();
    }

    /**
     * Tạo mật khẩu gồm:
     * - Ít nhất 1 chữ hoa
     * - Ít nhất 1 chữ thường
     * - Ít nhất 1 chữ số
     * - Ít nhất 1 ký tự đặc biệt
     */
    private String taoMatKhauNgauNhien(
            int doDai
    ) {
        if (doDai < 8) {
            throw new IllegalArgumentException(
                    "Độ dài mật khẩu phải từ 8 ký tự"
            );
        }

        List<Character> characters =
                new ArrayList<>();

        characters.add(
                randomCharacter(UPPERCASE)
        );

        characters.add(
                randomCharacter(LOWERCASE)
        );

        characters.add(
                randomCharacter(DIGITS)
        );

        characters.add(
                randomCharacter(SPECIALS)
        );

        while (
                characters.size() < doDai
        ) {
            characters.add(
                    randomCharacter(
                            ALL_CHARACTERS
                    )
            );
        }

        Collections.shuffle(
                characters,
                SECURE_RANDOM
        );

        StringBuilder password =
                new StringBuilder(doDai);

        for (
                Character character :
                characters
        ) {
            password.append(character);
        }

        return password.toString();
    }

    private char randomCharacter(
            String source
    ) {
        return source.charAt(
                SECURE_RANDOM.nextInt(
                        source.length()
                )
        );
    }

    private void guiEmailMatKhauMoi(
            String email,
            String tenNguoiDung,
            String tenDangNhap,
            String matKhauMoi
    ) {
        if (
                fromEmail == null
                        || fromEmail.isBlank()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Chưa cấu hình MAIL_USERNAME để gửi email"
            );
        }

        try {
            MimeMessage mimeMessage =
                    javaMailSender
                            .createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);

            helper.setTo(email);

            helper.setSubject(
                    "An Yên - Mật khẩu đăng nhập mới"
            );

            helper.setText(
                    taoNoiDungEmail(
                            tenNguoiDung,
                            tenDangNhap,
                            matKhauMoi
                    ),
                    true
            );

            javaMailSender.send(
                    mimeMessage
            );

        } catch (
                MessagingException
                | MailException exception
        ) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể gửi email mật khẩu mới. Vui lòng thử lại",
                    exception
            );
        }
    }

    private String taoNoiDungEmail(
            String tenNguoiDung,
            String tenDangNhap,
            String matKhauMoi
    ) {
        String safeName =
                escapeHtml(
                        tenNguoiDung == null
                                || tenNguoiDung.isBlank()
                                ? "Quý khách"
                                : tenNguoiDung
                );

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="
                    margin:0;
                    padding:24px;
                    background:#f5f3ef;
                    font-family:Arial,sans-serif;
                    color:#253047;
                ">
                    <div style="
                        max-width:600px;
                        margin:auto;
                        background:#ffffff;
                        border-radius:14px;
                        overflow:hidden;
                        border:1px solid #e6dfd5;
                    ">
                        <div style="
                            padding:22px 28px;
                            background:#7a0011;
                            color:#ffffff;
                        ">
                            <h2 style="margin:0;">
                                AN YÊN
                            </h2>

                            <p style="margin:6px 0 0;">
                                Khôi phục mật khẩu tài khoản
                            </p>
                        </div>

                        <div style="padding:28px;">
                            <p>
                                Xin chào
                                <strong>%s</strong>,
                            </p>

                            <p>
                                Hệ thống đã tiếp nhận yêu cầu
                                quên mật khẩu của bạn.
                            </p>

                            <div style="
                                padding:18px;
                                background:#f8f5f0;
                                border-radius:10px;
                                border-left:4px solid #7a0011;
                            ">
                                <p style="margin:0 0 10px;">
                                    Tên đăng nhập:
                                    <strong>%s</strong>
                                </p>

                                <p style="margin:0;">
                                    Mật khẩu mới:
                                </p>

                                <div style="
                                    margin-top:8px;
                                    font-size:24px;
                                    font-weight:700;
                                    letter-spacing:2px;
                                    color:#7a0011;
                                ">
                                    %s
                                </div>
                            </div>

                            <p style="margin-top:22px;">
                                Vui lòng đăng nhập bằng mật khẩu
                                trên và đổi lại mật khẩu sau khi
                                đăng nhập.
                            </p>

                            <p style="
                                color:#667085;
                                font-size:13px;
                            ">
                                Nếu bạn không thực hiện yêu cầu
                                này, hãy liên hệ quản trị viên
                                An Yên.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                safeName,
                escapeHtml(tenDangNhap),
                escapeHtml(matKhauMoi)
        );
    }

    private String escapeHtml(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }
}