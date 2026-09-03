package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.anyen.dto.request.LienHeRequest;

@Service
@RequiredArgsConstructor
public class LienHeService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void guiLienHe(LienHeRequest request) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);

        // Email nhận thông tin liên hệ
        message.setTo(fromEmail);

        message.setSubject("[AN YÊN] Liên hệ mới: " + request.getTieude());

        String content = """
                Bạn vừa nhận được một liên hệ mới từ website An Yên.

                THÔNG TIN KHÁCH HÀNG
                -------------------------
                Họ tên: %s
                Email: %s
                Số điện thoại: %s

                TIÊU ĐỀ
                -------------------------
                %s

                NỘI DUNG
                -------------------------
                %s
                """.formatted(
                request.getUsername(),
                request.getEmail(),
                request.getSdt(),
                request.getTieude(),
                request.getNoidung()
        );

        message.setText(content);

        javaMailSender.send(message);
    }
}