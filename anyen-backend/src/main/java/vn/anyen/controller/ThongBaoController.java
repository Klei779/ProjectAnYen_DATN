package vn.anyen.controller;



import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.request.GiaoCongViecRequest;

import vn.anyen.dto.request.TuChoiRequest;

import vn.anyen.dto.response.ThongBaoResponse;

import vn.anyen.service.JwtService;

import vn.anyen.service.ThongBaoService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;

import vn.anyen.dto.request.TuChoiHoaDonRequest;

import vn.anyen.service.HoaDonService;



import java.util.HashMap;

import java.util.List;

import java.util.Map;



@RestController

@RequestMapping("/api/nhan-vien/thong-bao")

@RequiredArgsConstructor

public class ThongBaoController {



    private final ThongBaoService thongBaoService;

    private final JwtService jwtService;

    private final HoaDonService hoaDonService;



    /**

     * Lấy userId từ JWT token trong header Authorization

     */

    private Integer getUserIdFromHeader(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            throw new RuntimeException("Không có token xác thực");

        }



        String token = authHeader.substring(7);

        return jwtService.getUserIdFromToken(token);

    }



    /**

     * Lấy danh sách thông báo của nhân viên đang đăng nhập

     */

    @GetMapping

    public List<ThongBaoResponse> getAll(

            @RequestHeader("Authorization") String authHeader) {



        Integer userId = getUserIdFromHeader(authHeader);

        return thongBaoService.getThongBaoByNguoiNhan(userId);

    }



    /**

     * Đếm thông báo chưa đọc

     */

    @GetMapping("/chua-doc")

    public Map<String, Long> countChuaDoc(

            @RequestHeader("Authorization") String authHeader) {



        Integer userId = getUserIdFromHeader(authHeader);

        Map<String, Long> result = new HashMap<>();

        result.put("count", thongBaoService.countChuaDoc(userId));

        return result;

    }



    /**

     * Chấp nhận thông báo công việc

     */

    @PutMapping("/{id}/chap-nhan")

    public ResponseEntity<?> chapNhan(

            @PathVariable Integer id,

            @RequestHeader("Authorization") String authHeader) {



        Integer userId = getUserIdFromHeader(authHeader);

        thongBaoService.chapNhan(id, userId);



        Map<String, Object> response = new HashMap<>();

        response.put("success", true);

        response.put("message", "Đã tiếp nhận khách hàng thành công");

        return ResponseEntity.ok(response);

    }



    /**

     * Từ chối thông báo + lý do

     */

    @PutMapping("/{id}/tu-choi")

    public ResponseEntity<?> tuChoi(

            @PathVariable Integer id,

            @RequestBody TuChoiRequest request,

            @RequestHeader("Authorization") String authHeader) {



        Integer userId = getUserIdFromHeader(authHeader);

        thongBaoService.tuChoi(id, userId, request.getLyDoTuChoi());



        Map<String, Object> response = new HashMap<>();

        response.put("success", true);

        response.put("message", "Đã từ chối và gửi phản hồi về hotline");

        return ResponseEntity.ok(response);

    }



    /**

     * Đánh dấu đã đọc

     */

    @PutMapping("/{id}/da-doc")

    public ResponseEntity<?> danhDauDaDoc(@PathVariable Integer id) {



        thongBaoService.danhDauDaDoc(id);



        Map<String, Object> response = new HashMap<>();

        response.put("success", true);

        return ResponseEntity.ok(response);

    }



    /**

     * Đánh dấu tất cả đã đọc

     */

    @PutMapping("/da-doc-tat-ca")

    public ResponseEntity<?> danhDauTatCaDaDoc(

            @RequestHeader("Authorization") String authHeader) {

        

        Integer userId = getUserIdFromHeader(authHeader);

        thongBaoService.danhDauTatCaDaDoc(userId);



        Map<String, Object> response = new HashMap<>();

        response.put("success", true);

        return ResponseEntity.ok(response);

    }



    @PutMapping("/{maThongBao}/chap-nhan-huy-hoa-don")

    public Map<String, Object> chapNhanHuyHoaDon(

            @PathVariable Integer maThongBao,

            Authentication authentication

    ) {

        return hoaDonService.chapNhanHuy(

                maThongBao,

                authentication.getName()

        );

    }



    @PutMapping("/{maThongBao}/tu-choi-huy-hoa-don")

    public Map<String, Object> tuChoiHuyHoaDon(

            @PathVariable Integer maThongBao,

            @Valid @RequestBody TuChoiHoaDonRequest request,

            Authentication authentication

    ) {

        return hoaDonService.tuChoiHuy(

                maThongBao,

                authentication.getName(),

                request

        );

    }



    /**

     * Giao công việc cho nhân viên (Hotline gửi)

     */

    @PostMapping("/giao-cong-viec")

    public ResponseEntity<?> giaoCongViec(

            @RequestBody GiaoCongViecRequest request,

            @RequestHeader("Authorization") String authHeader) {



        System.out.println("=== GIAO CONG VIEC CALLED ===");

        System.out.println("Request: " + request);



        Integer nguoiGuiId = getUserIdFromHeader(authHeader);



        thongBaoService.giaoCongViec(

                nguoiGuiId,

                request.getMaNhanVien(),

                request.getHoTenKhachHang(),

                request.getSoDienThoaiKhachHang(),

                request.getDiaChiKhachHang(),

                request.getAudioUrl(),

                request.getLatitude(),

                request.getLongitude()

        );



        Map<String, Object> response = new HashMap<>();

        response.put("success", true);

        response.put("message", "Đã giao công việc cho nhân viên thành công");

        return ResponseEntity.ok(response);

    }



    /**

     * Lấy danh sách thông báo của hotline (chỉ các thông báo gửi đến hotline)

     */

    @GetMapping("/hotline")

    public List<ThongBaoResponse> getHotlineNotifications(

            @RequestHeader("Authorization") String authHeader) {



        Integer userId = getUserIdFromHeader(authHeader);

        return thongBaoService.getThongBaoByNguoiNhan(userId);

    }



    /**

     * Đánh dấu tất cả đã đọc cho hotline

     */

    @PutMapping("/hotline/da-doc-tat-ca")

    public ResponseEntity<?> markAllAsReadHotline(

            @RequestHeader("Authorization") String authHeader) {



        Integer userId = getUserIdFromHeader(authHeader);

        thongBaoService.danhDauTatCaDaDoc(userId);



        Map<String, Object> response = new HashMap<>();

        response.put("success", true);

        return ResponseEntity.ok(response);

    }

}

