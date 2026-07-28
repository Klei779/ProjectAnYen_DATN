package vn.anyen.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.anyen.dto.request.TinTucRequest;
import vn.anyen.dto.response.TinTucPageResponse;
import vn.anyen.dto.response.TinTucResponse;
import vn.anyen.entity.TinTuc;
import vn.anyen.repository.TinTucRepository;
import vn.anyen.service.TinTucService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TinTucServiceImpl implements TinTucService {

    private static final int TRANG_THAI_HIEN_THI = 1;

    private final TinTucRepository tinTucRepository;

    /**
     * API công khai cho website.
     * Chỉ lấy bài viết đang hiển thị.
     */
    @Override
    public List<TinTucResponse> getAllPublic() {
        return tinTucRepository
                .findAllByTrangThaiOrderByNgayDangDesc(
                        TRANG_THAI_HIEN_THI
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Người dùng không thể xem bài đang bị ẩn.
     */
    @Override
    public TinTucResponse findPublicById(Integer id) {

        TinTuc tinTuc = tinTucRepository
                .findByMaTinTucAndTrangThai(
                        id,
                        TRANG_THAI_HIEN_THI
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Bài viết không tồn tại hoặc đang bị ẩn"
                        )
                );

        return convertToResponse(tinTuc);
    }

    /**
     * Danh sách quản lý dành cho Admin.
     */
    @Override
    public TinTucPageResponse searchAdmin(
            String keyword,
            Integer loaiTin,
            Integer trangThai,
            int page,
            int pageSize
    ) {
        validateLoaiTinNullable(loaiTin);
        validateTrangThaiNullable(trangThai);

        int safePage = Math.max(page, 1);

        int safePageSize = Math.min(
                Math.max(pageSize, 1),
                100
        );

        String normalizedKeyword =
                normalizeNullable(keyword);

        Page<TinTuc> result =
                tinTucRepository.search(
                        normalizedKeyword,
                        loaiTin,
                        trangThai,
                        PageRequest.of(
                                safePage - 1,
                                safePageSize
                        )
                );

        return TinTucPageResponse.builder()
                .items(
                        result.getContent()
                                .stream()
                                .map(this::convertToResponse)
                                .toList()
                )
                .total(result.getTotalElements())
                .page(safePage)
                .pageSize(safePageSize)
                .totalPages(result.getTotalPages())
                .build();
    }

    /**
     * Admin được xem cả bài đang ẩn.
     */
    @Override
    public TinTucResponse findAdminById(Integer id) {
        return convertToResponse(findEntity(id));
    }

    @Override
    @Transactional
    public TinTucResponse create(
            TinTucRequest request
    ) {
        TinTuc tinTuc = new TinTuc();

        applyRequest(
                tinTuc,
                request,
                true
        );

        TinTuc saved =
                tinTucRepository.save(tinTuc);

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public TinTucResponse update(
            Integer id,
            TinTucRequest request
    ) {
        TinTuc tinTuc = findEntity(id);

        applyRequest(
                tinTuc,
                request,
                false
        );

        TinTuc updated =
                tinTucRepository.save(tinTuc);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public TinTucResponse changeStatus(
            Integer id,
            Integer trangThai
    ) {
        validateTrangThai(trangThai);

        TinTuc tinTuc = findEntity(id);

        tinTuc.setTrangThai(trangThai);

        TinTuc updated =
                tinTucRepository.save(tinTuc);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Integer id) {

        TinTuc tinTuc = findEntity(id);

        tinTucRepository.delete(tinTuc);
    }

    private TinTuc findEntity(Integer id) {
        return tinTucRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy tin tức có mã " + id
                        )
                );
    }

    /**
     * Gán dữ liệu Request vào Entity.
     */
    private void applyRequest(
            TinTuc tinTuc,
            TinTucRequest request,
            boolean creating
    ) {
        validateLoaiTin(
                request.getLoaiTin()
        );

        validateTrangThaiNullable(
                request.getTrangThai()
        );

        tinTuc.setTieuDe(
                request.getTieuDe().trim()
        );

        tinTuc.setTomTat(
                request.getTomTat().trim()
        );

        tinTuc.setNoiDung(
                request.getNoiDung().trim()
        );

        tinTuc.setAnhDaiDien(
                normalizeNullable(
                        request.getAnhDaiDien()
                )
        );

        tinTuc.setLoaiTin(
                request.getLoaiTin()
        );

        if (request.getTrangThai() != null) {
            tinTuc.setTrangThai(
                    request.getTrangThai()
            );
        } else if (creating) {
            tinTuc.setTrangThai(
                    TRANG_THAI_HIEN_THI
            );
        }
    }

    private void validateLoaiTin(Integer loaiTin) {
        if (
                loaiTin == null
                        || loaiTin < 1
                        || loaiTin > 4
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Loại tin phải nằm trong khoảng từ 1 đến 4"
            );
        }
    }

    private void validateLoaiTinNullable(
            Integer loaiTin
    ) {
        if (loaiTin != null) {
            validateLoaiTin(loaiTin);
        }
    }

    private void validateTrangThai(
            Integer trangThai
    ) {
        if (
                trangThai == null
                        || (
                        trangThai != 0
                                && trangThai != 1
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái chỉ nhận giá trị 0 hoặc 1"
            );
        }
    }

    private void validateTrangThaiNullable(
            Integer trangThai
    ) {
        if (trangThai != null) {
            validateTrangThai(trangThai);
        }
    }

    private String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private TinTucResponse convertToResponse(
            TinTuc tinTuc
    ) {
        return TinTucResponse.builder()
                .maTinTuc(
                        tinTuc.getMaTinTuc()
                )
                .tieuDe(
                        tinTuc.getTieuDe()
                )
                .tomTat(
                        tinTuc.getTomTat()
                )
                .noiDung(
                        tinTuc.getNoiDung()
                )
                .anhDaiDien(
                        tinTuc.getAnhDaiDien()
                )
                .loaiTin(
                        tinTuc.getLoaiTin()
                )
                .trangThai(
                        tinTuc.getTrangThai()
                )
                .ngayDang(
                        tinTuc.getNgayDang()
                )
                .createdAt(
                        tinTuc.getCreatedAt()
                )
                .updatedAt(
                        tinTuc.getUpdatedAt()
                )
                .build();
    }
}