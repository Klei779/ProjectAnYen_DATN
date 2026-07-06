package vn.anyen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.InviteDoiTacRequest;
import vn.anyen.dto.request.RegisterDoiTacRequest;
import vn.anyen.entity.DoiTac;
import vn.anyen.service.QuanLyDoiTacService;

import java.util.List;

@RestController
@RequestMapping("/api/quanly-doitac")
@CrossOrigin("*")
public class QuanLyDoiTacController {

    @Autowired
    private QuanLyDoiTacService quanLyDoiTacService;

    @GetMapping("/list")
    public ResponseEntity<List<DoiTac>> getAllDoiTac() {
        return ResponseEntity.ok(quanLyDoiTacService.getAllDoiTac());
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteDoiTac(@RequestBody InviteDoiTacRequest request) {
        try {
            quanLyDoiTacService.inviteDoiTac(request.getEmail());
            return ResponseEntity.ok("Đã gửi lời mời hợp tác thành công đến " + request.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerDoiTac(@RequestBody RegisterDoiTacRequest request) {
        try {
            quanLyDoiTacService.registerDoiTac(
                    request.getToken(),
                    request.getTenDoiTac(),
                    request.getTenDoanhNghiep(),
                    request.getMaSoThue(),
                    request.getTenDangNhap(),
                    request.getMatKhau(),
                    request.getSoDienThoai(),
                    request.getDiaChi()
            );
            return ResponseEntity.ok("Đăng ký thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
