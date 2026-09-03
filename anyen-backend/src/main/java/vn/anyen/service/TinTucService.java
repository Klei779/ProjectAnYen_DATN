package vn.anyen.service;

import vn.anyen.dto.request.TinTucRequest;
import vn.anyen.dto.response.TinTucPageResponse;
import vn.anyen.dto.response.TinTucResponse;

import java.util.List;

public interface TinTucService {

    /**
     * Danh sách tin tức công khai.
     * Chỉ trả về bài viết có trạng thái hiển thị.
     */
    List<TinTucResponse> getAllPublic();

    /**
     * Chi tiết tin tức công khai.
     */
    TinTucResponse findPublicById(Integer id);

    /**
     * Danh sách quản lý dành cho Admin.
     */
    TinTucPageResponse searchAdmin(
            String keyword,
            Integer loaiTin,
            Integer trangThai,
            int page,
            int pageSize
    );

    /**
     * Admin xem bài viết, kể cả bài viết đang ẩn.
     */
    TinTucResponse findAdminById(Integer id);

    TinTucResponse create(TinTucRequest request);

    TinTucResponse update(
            Integer id,
            TinTucRequest request
    );

    TinTucResponse changeStatus(
            Integer id,
            Integer trangThai
    );

    void delete(Integer id);
}