package vn.anyen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.PushSubscriptionRequest;
import vn.anyen.dto.request.PushUnsubscribeRequest;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.PushSubscriptionDoiTac;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.PushSubscriptionDoiTacRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {

    private final PushSubscriptionDoiTacRepository
            pushSubscriptionRepository;

    private final DoiTacRepository doiTacRepository;

    private final ObjectMapper objectMapper;

    private final PublicKey vapidPublicKeyObject;

    private final PrivateKey vapidPrivateKeyObject;

    private final String vapidPublicKey;

    private final String vapidSubject;

    public PushNotificationService(
            PushSubscriptionDoiTacRepository
                    pushSubscriptionRepository,

            DoiTacRepository doiTacRepository,

            ObjectMapper objectMapper,

            @Value("${webpush.vapid.public-key:}")
            String vapidPublicKey,

            @Value("${webpush.vapid.private-key:}")
            String vapidPrivateKey,

            @Value("${webpush.vapid.subject:mailto:admin@anyen.vn}")
            String vapidSubject

    ) throws Exception {

        this.pushSubscriptionRepository =
                pushSubscriptionRepository;

        this.doiTacRepository =
                doiTacRepository;

        this.objectMapper =
                objectMapper;

        this.vapidPublicKey =
                vapidPublicKey;

        this.vapidSubject =
                vapidSubject;

        /*
         * BouncyCastle được web-push sử dụng
         * để xử lý EC/VAPID.
         */
        if (Security.getProvider(
                BouncyCastleProvider.PROVIDER_NAME
        ) == null) {

            Security.addProvider(
                    new BouncyCastleProvider()
            );
        }

        if (vapidPublicKey == null
                || vapidPublicKey.isBlank()
                || vapidPrivateKey == null
                || vapidPrivateKey.isBlank()) {

            log.warn(
                    "Chưa cấu hình VAPID key. Web Push chưa hoạt động."
            );

            this.vapidPublicKeyObject = null;
            this.vapidPrivateKeyObject = null;

            return;
        }

        this.vapidPublicKeyObject =
                Utils.loadPublicKey(
                        vapidPublicKey
                );

        this.vapidPrivateKeyObject =
                Utils.loadPrivateKey(
                        vapidPrivateKey
                );
    }

    public String getPublicKey() {
        return vapidPublicKey;
    }

    /*
     * Đối tác đồng ý nhận notification trên browser.
     */
    @Transactional
    public void subscribe(
            Authentication authentication,
            PushSubscriptionRequest request
    ) {

        DoiTac doiTac =
                getDoiTacDangNhap(authentication);

        String endpoint =
                request.getEndpoint().trim();

        String endpointHash =
                hashEndpoint(endpoint);

        PushSubscriptionDoiTac subscription =
                pushSubscriptionRepository
                        .findByEndpointHash(endpointHash)
                        .orElseGet(
                                PushSubscriptionDoiTac::new
                        );

        /*
         * Nếu cùng browser trước đó đăng nhập
         * tài khoản đối tác khác thì chuyển ownership
         * sang tài khoản hiện tại.
         */
        subscription.setDoiTac(doiTac);

        subscription.setEndpoint(
                endpoint
        );

        subscription.setEndpointHash(
                endpointHash
        );

        subscription.setP256dh(
                request.getKeys()
                        .getP256dh()
        );

        subscription.setAuth(
                request.getKeys()
                        .getAuth()
        );

        pushSubscriptionRepository.save(
                subscription
        );
    }

    /*
     * Dùng khi đối tác LOGOUT.
     *
     * Đóng browser KHÔNG gọi hàm này,
     * vì đóng browser vẫn phải nhận push.
     */
    @Transactional
    public void unsubscribe(
            Authentication authentication,
            PushUnsubscribeRequest request
    ) {

        DoiTac doiTac =
                getDoiTacDangNhap(authentication);

        String hash =
                hashEndpoint(
                        request.getEndpoint()
                );

        pushSubscriptionRepository
                .deleteByEndpointHashAndDoiTac_MaDoiTac(
                        hash,
                        doiTac.getMaDoiTac()
                );
    }

    /*
     * Gửi notification ra ngoài browser.
     *
     * Được chạy background nên lỗi Push
     * KHÔNG được làm lỗi nghiệp vụ đơn hàng.
     */
    @Async("pushExecutor")
    public void guiThongBaoDoiTac(
            Integer maDoiTac,
            String title,
            String body,
            String url,
            String tag,
            Integer maThongBao,
            Integer maChiTietDonHang,
            Integer maSanPhamGoiY
    ) {

        log.info(
                "========== WEB PUSH START maDoiTac={} ==========",
                maDoiTac
        );

        if (maDoiTac == null) {

            log.warn(
                    "[WEB-PUSH] maDoiTac null"
            );

            return;
        }

        if (
                vapidPublicKeyObject == null
                        || vapidPrivateKeyObject == null
        ) {

            log.warn(
                    "[WEB-PUSH] KHÔNG CÓ VAPID KEY"
            );

            return;
        }

        List<PushSubscriptionDoiTac> subscriptions =
                pushSubscriptionRepository
                        .findByDoiTac_MaDoiTac(
                                maDoiTac
                        );

        log.info(
                "[WEB-PUSH] maDoiTac={} có {} subscription",
                maDoiTac,
                subscriptions.size()
        );

        if (subscriptions.isEmpty()) {

            log.warn(
                    "[WEB-PUSH] Không có subscription cho maDoiTac={}",
                    maDoiTac
            );

            return;
        }

        String safeTitle =
                title == null || title.isBlank()
                        ? "An Yên"
                        : title;

        String safeBody =
                rutGon(
                        body == null
                                ? "Bạn có thông báo mới."
                                : body,
                        450
                );

        String safeUrl =
                url == null || url.isBlank()
                        ? "/doi-tac/thong-bao"
                        : url;

        String safeTag =
                tag == null || tag.isBlank()
                        ? "anyen-" + System.currentTimeMillis()
                        : tag;

        String payload;

        try {

            Map<String, Object> data =
                    new LinkedHashMap<>();

            DoiTac doiTac =
                    doiTacRepository
                            .findById(maDoiTac)
                            .orElse(null);

            String tenDoiTac =
                    "Đối tác #" + maDoiTac;

            if (doiTac != null) {

                // Ưu tiên tên đối tác
                if (
                        doiTac.getTenDoiTac() != null
                                && !doiTac.getTenDoiTac().isBlank()
                ) {

                    tenDoiTac =
                            doiTac.getTenDoiTac();

                } else if (
                        doiTac.getTenDoanhNghiep() != null
                                && !doiTac.getTenDoanhNghiep().isBlank()
                ) {

                    // Không có tên đối tác mới fallback sang doanh nghiệp
                    tenDoiTac =
                            doiTac.getTenDoanhNghiep();
                }
            }


// ===============================
// CHỈ LỜI MỜI THAY THẾ MỚI TRUE
// ===============================
            boolean isReplacement =
                    maChiTietDonHang != null
                            && maSanPhamGoiY != null;


            data.put(
                    "title",
                    safeTitle
            );

            data.put(
                    "body",
                    safeBody
            );

            data.put(
                    "url",
                    safeUrl
            );

            data.put(
                    "tag",
                    safeTag
            );

            data.put(
                    "maDoiTac",
                    maDoiTac
            );

            data.put(
                    "tenDoiTac",
                    tenDoiTac
            );

            data.put(
                    "maThongBao",
                    maThongBao
            );

            data.put(
                    "maChiTietDonHang",
                    maChiTietDonHang
            );

            data.put(
                    "maSanPhamGoiY",
                    maSanPhamGoiY
            );


// QUAN TRỌNG
            data.put(
                    "isReplacement",
                    isReplacement
            );

            log.info(
                    "[WEB-PUSH-CHECK] doiTac={}, ten={}, replacement={}, CTDH={}, SP={}",
                    maDoiTac,
                    tenDoiTac,
                    isReplacement,
                    maChiTietDonHang,
                    maSanPhamGoiY
            );
            payload =
                    objectMapper.writeValueAsString(
                            data
                    );

            log.info(
                    "[WEB-PUSH] Payload={}",
                    payload
            );

        } catch (Exception e) {

            log.error(
                    "[WEB-PUSH] Không tạo được payload",
                    e
            );

            return;
        }

        for (
                PushSubscriptionDoiTac subscription
                : subscriptions
        ) {

            log.info(
                    "[WEB-PUSH] Chuẩn bị gửi maPush={}",
                    subscription.getMaPush()
            );

            guiMotSubscription(
                    subscription,
                    payload
            );
        }
    }

    private void guiMotSubscription(
            PushSubscriptionDoiTac subscription,
            String payload
    ) {

        try {

            log.info(
                    "[WEB-PUSH] Sending maPush={}, endpoint={}",
                    subscription.getMaPush(),
                    subscription.getEndpoint()
            );

            Notification notification =
                    new Notification(
                            subscription.getEndpoint(),
                            subscription.getP256dh(),
                            subscription.getAuth(),
                            payload
                    );

            PushService pushService =
                    new PushService();

            pushService.setPublicKey(
                    vapidPublicKeyObject
            );

            pushService.setPrivateKey(
                    vapidPrivateKeyObject
            );

            pushService.setSubject(
                    vapidSubject
            );

            // TẠO request trước, chưa gửi
            HttpPost httpPost =
                    pushService.preparePost(
                            notification,
                            Encoding.AES128GCM
                    );

            // web-push 5.1.2 thêm Crypto-Key,
            // nhưng FCM hiện đang từ chối header này
            httpPost.removeHeaders("Crypto-Key");

            log.info(
                    "[WEB-PUSH] Crypto-Key sau khi xóa = {}",
                    httpPost.getFirstHeader("Crypto-Key")
            );

            try (
                    CloseableHttpClient httpClient =
                            HttpClients.createSystem();

                    CloseableHttpResponse response =
                            httpClient.execute(httpPost)
            ) {

                int statusCode =
                        response
                                .getStatusLine()
                                .getStatusCode();

                String responseBody = "";

                if (response.getEntity() != null) {

                    responseBody =
                            EntityUtils.toString(
                                    response.getEntity(),
                                    StandardCharsets.UTF_8
                            );
                }

                log.info(
                        "[WEB-PUSH] RESPONSE BODY maPush={} => {}",
                        subscription.getMaPush(),
                        responseBody
                );

                if (
                        response.getFirstHeader("WWW-Authenticate")
                                != null
                ) {

                    log.info(
                            "[WEB-PUSH] WWW-AUTHENTICATE maPush={} => {}",
                            subscription.getMaPush(),
                            response
                                    .getFirstHeader("WWW-Authenticate")
                                    .getValue()
                    );
                }

                log.info(
                        "[WEB-PUSH] KẾT QUẢ maPush={} HTTP={}",
                        subscription.getMaPush(),
                        statusCode
                );

                if (
                        statusCode == 404
                                || statusCode == 410
                ) {

                    pushSubscriptionRepository
                            .deleteById(
                                    subscription.getMaPush()
                            );

                    log.warn(
                            "[WEB-PUSH] Subscription hết hạn. Đã xóa maPush={}",
                            subscription.getMaPush()
                    );

                    return;
                }

                if (
                        statusCode < 200
                                || statusCode >= 300
                ) {

                    log.warn(
                            "[WEB-PUSH] Gửi thất bại maPush={}, HTTP={}",
                            subscription.getMaPush(),
                            statusCode
                    );

                    return;
                }

                log.info(
                        "[WEB-PUSH] GỬI THÀNH CÔNG maPush={}",
                        subscription.getMaPush()
                );
            }

        } catch (Exception e) {

            log.error(
                    "[WEB-PUSH] EXCEPTION maPush={}",
                    subscription.getMaPush(),
                    e
            );
        }
    }

    private DoiTac getDoiTacDangNhap(
            Authentication authentication
    ) {

        if (authentication == null
                || authentication.getName() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bạn chưa đăng nhập"
            );
        }

        return doiTacRepository
                .findByTenDangNhap(
                        authentication.getName()
                )
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Không tìm thấy đối tác"
                                )
                );
    }

    private String hashEndpoint(
            String endpoint
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            endpoint
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Không hash được endpoint",
                    e
            );
        }
    }

    private String rutGon(
            String text,
            int max
    ) {

        if (text == null) {
            return "";
        }

        String value =
                text.trim();

        if (value.length() <= max) {
            return value;
        }

        return value.substring(
                0,
                max
        ) + "...";
    }
}
