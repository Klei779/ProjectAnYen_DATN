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
import vn.anyen.dto.request.KyHopDongRequest;
import vn.anyen.dto.request.QuanLyDoiTacRequest;
import vn.anyen.dto.request.ThemDoiTacRequest;
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

    /**
     * Lấy danh sách đối tác, không lấy đối tác đã xóa mềm.
     */
    @Transactional(readOnly = true)
    public List<QuanLyDoiTacResponse> getAllDoiTac() {
        return doiTacRepository.findAllByOrderByMaDoiTacDesc()
                .stream()
                .filter(doiTac ->
                        !DoiTac.TT_DA_XOA.equals(doiTac.getTrangThai())
                )
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Tạo lời mời đối tác mới.
     * Đối tác mới được đặt trạng thái chờ xác nhận.
     */
    @Transactional
    public QuanLyDoiTacResponse createDoiTac(
            ThemDoiTacRequest request
    ) {
        String email = trim(request.getEmail());

        if (email.isEmpty()) {
            throw new RuntimeException(
                    "Email đối tác không được để trống"
            );
        }

        if (doiTacRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "Email đã được sử dụng bởi đối tác khác"
            );
        }

        String confirmationToken =
                UUID.randomUUID().toString();

        String tempUsername =
                "temp_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String tempPassword =
                passwordEncoder.encode(
                        UUID.randomUUID().toString()
                );

        String emailPart = email.contains("@")
                ? email.substring(0, email.indexOf("@"))
                : email;

        DoiTac doiTac = DoiTac.builder()
                .tenDoiTac("Đối tác " + emailPart)
                .tenDangNhap(tempUsername)
                .matKhau(tempPassword)
                .email(email)
                .trangThai(DoiTac.TT_CHO_XAC_NHAN)
                .confirmationToken(confirmationToken)
                .build();

        doiTacRepository.save(doiTac);

        guiEmailXacNhan(
                doiTac,
                confirmationToken
        );

        return mapToResponse(doiTac);
    }

    /**
     * Cập nhật thông tin đối tác.
     */
    @Transactional
    public QuanLyDoiTacResponse updateDoiTac(
            Integer maDoiTac,
            QuanLyDoiTacRequest request
    ) {
        DoiTac doiTac =
                getDoiTacDangTonTai(maDoiTac);

        validateTrungKhiSua(
                maDoiTac,
                request
        );

        doiTac.setTenDoiTac(
                trim(request.getTenDoiTac())
        );

        doiTac.setTenDoanhNghiep(
                trimNullable(
                        request.getTenDoanhNghiep()
                )
        );

        doiTac.setMaSoThue(
                trimNullable(
                        request.getMaSoThue()
                )
        );

        doiTac.setEmail(
                trim(request.getEmail())
        );

        doiTac.setSoDienThoai(
                trim(request.getSoDienThoai())
        );

        doiTac.setDiaChi(
                trimNullable(
                        request.getDiaChi()
                )
        );

        doiTac.setQuanHuyen(
                trimNullable(
                        request.getQuanHuyen()
                )
        );

        doiTac.setTinhThanh(
                trimNullable(
                        request.getTinhThanh()
                )
        );

        doiTacRepository.save(doiTac);

        return mapToResponse(doiTac);
    }

    /**
     * Cập nhật trạng thái hợp tác:
     *
     * 0 = Ngưng hợp tác.
     * 1 = Đang hợp tác.
     */
    @Transactional
    public QuanLyDoiTacResponse updateTrangThai(
            Integer maDoiTac,
            Integer trangThai
    ) {
        DoiTac doiTac =
                getDoiTacDangTonTai(maDoiTac);

        if (trangThai == null) {
            throw new RuntimeException(
                    "Trạng thái đối tác không được để trống"
            );
        }

        boolean laDangHopTac =
                DoiTac.TT_DANG_HOAT_DONG.equals(
                        trangThai
                );

        boolean laNgungHopTac =
                DoiTac.TT_NGUNG_HOAT_DONG.equals(
                        trangThai
                );

        if (!laDangHopTac && !laNgungHopTac) {
            throw new RuntimeException(
                    "Trạng thái chỉ được là Đang hợp tác hoặc Ngưng hợp tác"
            );
        }

        if (DoiTac.TT_CHO_XAC_NHAN.equals(
                doiTac.getTrangThai()
        )) {
            throw new RuntimeException(
                    "Đối tác đang chờ xác nhận, chưa thể thay đổi trạng thái hợp tác"
            );
        }

        doiTac.setTrangThai(trangThai);

        doiTacRepository.save(doiTac);

        return mapToResponse(doiTac);
    }

    /**
     * Xóa mềm đối tác.
     *
     * Chỉ được xóa nếu tất cả sản phẩm của đối tác
     * chưa từng xuất hiện trong chi tiết đơn hàng.
     */
    @Transactional
    public void deleteDoiTac(
            Integer maDoiTac
    ) {
        DoiTac doiTac =
                getDoiTacDangTonTai(maDoiTac);

        if (!kiemTraCoTheXoa(maDoiTac)) {
            throw new RuntimeException(
                    "Không thể xóa đối tác vì sản phẩm của đối tác đã phát sinh đơn hàng"
            );
        }

        /*
         * Không xóa vật lý để tránh lỗi khóa ngoại
         * với sản phẩm, công nợ, thông báo đối tác...
         */
        doiTac.setTrangThai(
                DoiTac.TT_DA_XOA
        );

        doiTacRepository.save(doiTac);
    }

    /**
     * Xác nhận token từ email.
     */
    @Transactional
    public DoiTac xacNhanDoiTac(
            String token
    ) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException(
                    "Token xác nhận không hợp lệ"
            );
        }

        DoiTac doiTac =
                doiTacRepository
                        .findByConfirmationToken(
                                token.trim()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Token xác nhận không hợp lệ hoặc không tồn tại."
                                )
                        );

        if (DoiTac.TT_DA_XOA.equals(
                doiTac.getTrangThai()
        )) {
            throw new RuntimeException(
                    "Lời mời hợp tác đã hết hạn do quá 24h."
            );
        }

        if (DoiTac.TT_DANG_HOAT_DONG.equals(
                doiTac.getTrangThai()
        )) {
            return doiTac;
        }

        /*
         * Giữ trạng thái chờ xác nhận trong lúc
         * đối tác điền thông tin ký hợp đồng.
         */
        doiTac.setTrangThai(
                DoiTac.TT_CHO_XAC_NHAN
        );

        doiTacRepository.save(doiTac);

        return doiTac;
    }

    /**
     * Đối tác hoàn tất đăng ký và ký hợp đồng.
     */
    @Transactional
    public void kyHopDong(
            KyHopDongRequest request
    ) {
        if (
                request.getToken() == null
                        || request.getToken()
                        .trim()
                        .isEmpty()
        ) {
            throw new RuntimeException(
                    "Token xác nhận không hợp lệ"
            );
        }

        DoiTac doiTac =
                doiTacRepository
                        .findByConfirmationToken(
                                request.getToken().trim()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Token không hợp lệ hoặc không tồn tại. "
                                                + "Vui lòng kiểm tra lại link trong email."
                                )
                        );

        if (DoiTac.TT_DA_XOA.equals(
                doiTac.getTrangThai()
        )) {
            throw new RuntimeException(
                    "Lời mời hợp tác đã hết hạn do quá 24h. "
                            + "Vui lòng liên hệ với An Yên để được cấp lại lời mời."
            );
        }

        String tenDangNhap =
                trim(request.getTenDangNhap());

        String soDienThoai =
                trim(request.getSoDienThoai());

        String maSoThue =
                trimNullable(
                        request.getMaSoThue()
                );

        if (
                doiTacRepository
                        .existsByTenDangNhap(
                                tenDangNhap
                        )
                        && !tenDangNhap.equals(
                        doiTac.getTenDangNhap()
                )
        ) {
            throw new RuntimeException(
                    "Tên đăng nhập đã tồn tại trong hệ thống. "
                            + "Vui lòng chọn tên khác."
            );
        }

        /*
         * Dùng method có loại trừ chính đối tác đang cập nhật
         * để tránh báo trùng dữ liệu của chính tài khoản đó.
         */
        if (
                doiTacRepository
                        .existsBySoDienThoaiAndMaDoiTacNot(
                                soDienThoai,
                                doiTac.getMaDoiTac()
                        )
        ) {
            throw new RuntimeException(
                    "Số điện thoại đã được sử dụng bởi một tài khoản khác."
            );
        }

        if (
                maSoThue != null
                        && doiTacRepository
                        .existsByMaSoThueAndMaDoiTacNot(
                                maSoThue,
                                doiTac.getMaDoiTac()
                        )
        ) {
            throw new RuntimeException(
                    "Mã số thuế đã tồn tại trong hệ thống."
            );
        }

        doiTac.setTenDangNhap(
                tenDangNhap
        );

        doiTac.setMatKhau(
                passwordEncoder.encode(
                        request.getMatKhau()
                )
        );

        doiTac.setTenDoiTac(
                trim(request.getTenDoiTac())
        );

        doiTac.setTenDoanhNghiep(
                trimNullable(
                        request.getTenDoanhNghiep()
                )
        );

        doiTac.setMaSoThue(
                maSoThue
        );

        doiTac.setSoDienThoai(
                soDienThoai
        );

        doiTac.setDiaChi(
                trimNullable(
                        request.getDiaChi()
                )
        );

        /*
         * Hoàn tất ký hợp đồng thì chuyển
         * sang trạng thái đang hợp tác.
         */
        doiTac.setTrangThai(
                DoiTac.TT_DANG_HOAT_DONG
        );

        doiTac.setConfirmationToken(null);

        doiTacRepository.save(doiTac);
    }

    /**
     * Kiểm tra dữ liệu trùng khi cập nhật đối tác.
     */
    private void validateTrungKhiSua(
            Integer maDoiTac,
            QuanLyDoiTacRequest request
    ) {
        String email =
                trim(request.getEmail());

        String soDienThoai =
                trim(request.getSoDienThoai());

        String maSoThue =
                trimNullable(
                        request.getMaSoThue()
                );

        if (
                email.isEmpty()
        ) {
            throw new RuntimeException(
                    "Email không được để trống"
            );
        }

        if (
                soDienThoai.isEmpty()
        ) {
            throw new RuntimeException(
                    "Số điện thoại không được để trống"
            );
        }

        if (
                doiTacRepository
                        .existsByEmailAndMaDoiTacNot(
                                email,
                                maDoiTac
                        )
        ) {
            throw new RuntimeException(
                    "Email đã được sử dụng bởi đối tác khác"
            );
        }

        if (
                doiTacRepository
                        .existsBySoDienThoaiAndMaDoiTacNot(
                                soDienThoai,
                                maDoiTac
                        )
        ) {
            throw new RuntimeException(
                    "Số điện thoại đã được sử dụng bởi đối tác khác"
            );
        }

        if (
                maSoThue != null
                        && doiTacRepository
                        .existsByMaSoThueAndMaDoiTacNot(
                                maSoThue,
                                maDoiTac
                        )
        ) {
            throw new RuntimeException(
                    "Mã số thuế đã được sử dụng bởi đối tác khác"
            );
        }
    }

    /**
     * Gửi email xác nhận đối tác.
     */
    private void guiEmailXacNhan(
            DoiTac doiTac,
            String token
    ) {
        try {
            String confirmUrl =
                    "http://localhost:5173/doitac/register?token="
                            + token;

            Context context = new Context();

            context.setVariable(
                    "tenDoiTac",
                    doiTac.getTenDoiTac()
            );

            context.setVariable(
                    "tenDoanhNghiep",
                    doiTac.getTenDoanhNghiep()
            );

            context.setVariable(
                    "email",
                    doiTac.getEmail()
            );

            context.setVariable(
                    "soDienThoai",
                    doiTac.getSoDienThoai()
            );

            context.setVariable(
                    "tenDangNhap",
                    doiTac.getTenDangNhap()
            );

            context.setVariable(
                    "confirmUrl",
                    confirmUrl
            );

            context.setVariable(
                    "websiteUrl",
                    "http://localhost:5173/"
            );

            context.setVariable(
                    "websiteDisplay",
                    "www.anyen.vn"
            );

            context.setVariable(
                    "logoUrl",
                    "https://res.cloudinary.com/duongtan/"
                            + "image/upload/v1721030000/logoAnYen.png"
            );

            context.setVariable(
                    "footerLogoUrl",
                    "https://res.cloudinary.com/duongtan/"
                            + "image/upload/v1721030000/logoAnYen.png"
            );

            String htmlContent =
                    templateEngine.process(
                            "xac-nhan-doi-tac",
                            context
                    );

            MimeMessage mimeMessage =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(doiTac.getEmail());

            helper.setSubject(
                    "🤝 An Yên — Lời Mời Hợp Tác Chính Thức"
            );

            helper.setText(
                    htmlContent,
                    true
            );

            javaMailSender.send(
                    mimeMessage
            );

        } catch (MessagingException e) {
            throw new RuntimeException(
                    "Lỗi khi gửi email xác nhận đến đối tác: "
                            + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Chuyển entity sang response.
     */
    private QuanLyDoiTacResponse mapToResponse(
            DoiTac doiTac
    ) {
        return QuanLyDoiTacResponse.builder()
                .maDoiTac(
                        doiTac.getMaDoiTac()
                )
                .tenDoiTac(
                        doiTac.getTenDoiTac()
                )
                .tenDoanhNghiep(
                        doiTac.getTenDoanhNghiep()
                )
                .maSoThue(
                        doiTac.getMaSoThue()
                )
                .tenDangNhap(
                        doiTac.getTenDangNhap()
                )
                .email(
                        doiTac.getEmail()
                )
                .soDienThoai(
                        doiTac.getSoDienThoai()
                )
                .diaChi(
                        doiTac.getDiaChi()
                )
                .quanHuyen(
                        doiTac.getQuanHuyen()
                )
                .tinhThanh(
                        doiTac.getTinhThanh()
                )
                .trangThai(
                        doiTac.getTrangThai()
                )
                .createdAt(
                        doiTac.getCreatedAt()
                )
                .coTheXoa(
                        kiemTraCoTheXoa(
                                doiTac.getMaDoiTac()
                        )
                )
                .build();
    }

    /**
     * Chỉ được xóa nếu sản phẩm của đối tác
     * chưa phát sinh chi tiết đơn hàng.
     */
    private boolean kiemTraCoTheXoa(
            Integer maDoiTac
    ) {
        long soChiTietDonHang =
                doiTacRepository
                        .countChiTietDonHangByMaDoiTac(
                                maDoiTac
                        );

        return soChiTietDonHang == 0;
    }

    /**
     * Tìm đối tác chưa bị xóa mềm.
     */
    private DoiTac getDoiTacDangTonTai(
            Integer maDoiTac
    ) {
        if (
                maDoiTac == null
                        || maDoiTac <= 0
        ) {
            throw new RuntimeException(
                    "Mã đối tác không hợp lệ"
            );
        }

        DoiTac doiTac =
                doiTacRepository
                        .findById(maDoiTac)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Không tìm thấy đối tác"
                                )
                        );

        if (
                DoiTac.TT_DA_XOA.equals(
                        doiTac.getTrangThai()
                )
        ) {
            throw new RuntimeException(
                    "Đối tác đã bị xóa"
            );
        }

        return doiTac;
    }

    private String trim(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }

    private String trimNullable(
            String value
    ) {
        if (
                value == null
                        || value.trim().isEmpty()
        ) {
            return null;
        }

        return value.trim();
    }
}