package vn.anyen.service;

import vn.anyen.dto.response.AiTrichXuatKhachHangResult;
import vn.anyen.entity.YeuCauTuVanAi;

public interface AiProviderService {

    AiTrichXuatKhachHangResult trichXuatThongTinKhachHang(
            String tenKhachHang,
            YeuCauTuVanAi thongTinHienTai,
            String tinNhanMoi
    );

    String getProviderName();
}