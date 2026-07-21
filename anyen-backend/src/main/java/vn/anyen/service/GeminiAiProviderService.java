package vn.anyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.AiTrichXuatKhachHangResult;
import vn.anyen.entity.YeuCauTuVanAi;

@Service
@Qualifier("geminiProvider")
@RequiredArgsConstructor
public class GeminiAiProviderService implements AiProviderService {

    private final GeminiService geminiService;

    @Override
    public AiTrichXuatKhachHangResult trichXuatThongTinKhachHang(
            String tenKhachHang,
            YeuCauTuVanAi thongTinHienTai,
            String tinNhanMoi
    ) {
        return geminiService.trichXuatThongTinKhachHang(
                tenKhachHang,
                thongTinHienTai,
                tinNhanMoi
        );
    }

    @Override
    public String getProviderName() {
        return "GEMINI";
    }
}