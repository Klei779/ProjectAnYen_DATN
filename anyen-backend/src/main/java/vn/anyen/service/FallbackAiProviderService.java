package vn.anyen.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.AiTrichXuatKhachHangResult;
import vn.anyen.entity.YeuCauTuVanAi;

@Slf4j
@Service
@Primary
public class FallbackAiProviderService implements AiProviderService {

    private final AiProviderService geminiProvider;
    private final AiProviderService ollamaProvider;

    public FallbackAiProviderService(
            @Qualifier("geminiProvider")
            AiProviderService geminiProvider,

            @Qualifier("ollamaProvider")
            AiProviderService ollamaProvider
    ) {
        this.geminiProvider = geminiProvider;
        this.ollamaProvider = ollamaProvider;
    }

    @Override
    public AiTrichXuatKhachHangResult trichXuatThongTinKhachHang(
            String tenKhachHang,
            YeuCauTuVanAi thongTinHienTai,
            String tinNhanMoi
    ) {
        long geminiStart = System.currentTimeMillis();

        try {
            log.info("========== BẮT ĐẦU GEMINI ==========");

            AiTrichXuatKhachHangResult result =
                    geminiProvider.trichXuatThongTinKhachHang(
                            tenKhachHang,
                            thongTinHienTai,
                            tinNhanMoi
                    );

            long elapsed =
                    System.currentTimeMillis() - geminiStart;

            log.info(
                    "Gemini thành công sau {} ms.",
                    elapsed
            );

            return result;

        } catch (Exception geminiException) {
            long elapsed =
                    System.currentTimeMillis() - geminiStart;

            log.warn(
                    "Gemini thất bại sau {} ms. Nguyên nhân: {}",
                    elapsed,
                    geminiException.getMessage(),
                    geminiException
            );
        }

        long ollamaStart = System.currentTimeMillis();

        try {
            log.info("========== CHUYỂN SANG OLLAMA ==========");

            AiTrichXuatKhachHangResult result =
                    ollamaProvider.trichXuatThongTinKhachHang(
                            tenKhachHang,
                            thongTinHienTai,
                            tinNhanMoi
                    );

            long elapsed =
                    System.currentTimeMillis() - ollamaStart;

            log.info(
                    "Ollama thành công sau {} ms.",
                    elapsed
            );

            return result;

        } catch (Exception ollamaException) {
            long elapsed =
                    System.currentTimeMillis() - ollamaStart;

            log.error(
                    "Ollama thất bại sau {} ms. Nguyên nhân: {}",
                    elapsed,
                    ollamaException.getMessage(),
                    ollamaException
            );

            throw new RuntimeException(
                    "Cả Gemini và Ollama hiện không thể xử lý yêu cầu. "
                            + "Phiên tư vấn sẽ được chuyển đến Hotline.",
                    ollamaException
            );
        }
    }

    @Override
    public String getProviderName() {
        return "GEMINI_WITH_OLLAMA_FALLBACK";
    }
}