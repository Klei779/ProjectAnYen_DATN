package vn.anyen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.anyen.dto.request.LoginRequest;
import vn.anyen.dto.response.LoginResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.entity.NhanVien;
import vn.anyen.repository.DoiTacRepository;
import vn.anyen.repository.NhanVienRepository;
import vn.anyen.service.AuthService;
import vn.anyen.service.JwtService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private DoiTacRepository doiTacRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private NhanVien nhanVien;
    private DoiTac doiTac;

    @BeforeEach
    void setUp() {
        nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(1);
        nhanVien.setHoTen("Nguyen Van A");
        nhanVien.setTenDangNhap("admin");
        nhanVien.setMatKhau("encoded_password");

        doiTac = new DoiTac();
        doiTac.setMaDoiTac(10);
        doiTac.setTenDoiTac("Cong ty An Phuc");
        doiTac.setTenDangNhap("anphuc");
        doiTac.setMatKhau("encoded_password");
    }

    @Test
    void login_NhanVien_DungTaiKhoanMatKhau_TraVeSuccessVaToken() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("admin");
        request.setMatKhau("123456");
        request.setLoaiTaiKhoan("NHAN_VIEN");

        when(nhanVienRepository.findByTenDangNhap("admin"))
                .thenReturn(Optional.of(nhanVien));

        when(passwordEncoder.matches("123456", "encoded_password"))
                .thenReturn(true);

        when(jwtService.generateToken(1, "admin", "NHAN_VIEN"))
                .thenReturn("jwt_token_nhan_vien");

        LoginResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getId());
        assertEquals("Nguyen Van A", response.getHoTen());
        assertEquals("admin", response.getTenDangNhap());
        assertEquals("NHAN_VIEN", response.getLoaiTaiKhoan());
        assertEquals("jwt_token_nhan_vien", response.getToken());

        verify(nhanVienRepository, times(1)).findByTenDangNhap("admin");
        verify(passwordEncoder, times(1)).matches("123456", "encoded_password");
        verify(jwtService, times(1)).generateToken(1, "admin", "NHAN_VIEN");
    }

    @Test
    void login_DoiTac_DungTaiKhoanMatKhau_TraVeSuccessVaToken() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("anphuc");
        request.setMatKhau("123456");
        request.setLoaiTaiKhoan("DOI_TAC");

        when(doiTacRepository.findByTenDangNhap("anphuc"))
                .thenReturn(Optional.of(doiTac));

        when(passwordEncoder.matches("123456", "encoded_password"))
                .thenReturn(true);

        when(jwtService.generateToken(10, "anphuc", "DOI_TAC"))
                .thenReturn("jwt_token_doi_tac");

        LoginResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals(10, response.getId());
        assertEquals("Cong ty An Phuc", response.getHoTen());
        assertEquals("anphuc", response.getTenDangNhap());
        assertEquals("DOI_TAC", response.getLoaiTaiKhoan());
        assertEquals("jwt_token_doi_tac", response.getToken());

        verify(doiTacRepository, times(1)).findByTenDangNhap("anphuc");
        verify(passwordEncoder, times(1)).matches("123456", "encoded_password");
        verify(jwtService, times(1)).generateToken(10, "anphuc", "DOI_TAC");
    }

    @Test
    void login_NhanVien_SaiMatKhau_TraVeFail() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("admin");
        request.setMatKhau("wrong_password");
        request.setLoaiTaiKhoan("NHAN_VIEN");

        when(nhanVienRepository.findByTenDangNhap("admin"))
                .thenReturn(Optional.of(nhanVien));

        when(passwordEncoder.matches("wrong_password", "encoded_password"))
                .thenReturn(false);

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertNull(response.getToken());
        assertNull(response.getTenDangNhap());
        assertNull(response.getLoaiTaiKhoan());

        verify(nhanVienRepository, times(1)).findByTenDangNhap("admin");
        verify(passwordEncoder, times(1)).matches("wrong_password", "encoded_password");
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void login_NhanVien_TaiKhoanKhongTonTai_TraVeFail() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("khongtontai");
        request.setMatKhau("123456");
        request.setLoaiTaiKhoan("NHAN_VIEN");

        when(nhanVienRepository.findByTenDangNhap("khongtontai"))
                .thenReturn(Optional.empty());

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertNull(response.getToken());

        verify(nhanVienRepository, times(1)).findByTenDangNhap("khongtontai");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void login_DoiTac_SaiMatKhau_TraVeFail() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("anphuc");
        request.setMatKhau("wrong_password");
        request.setLoaiTaiKhoan("DOI_TAC");

        when(doiTacRepository.findByTenDangNhap("anphuc"))
                .thenReturn(Optional.of(doiTac));

        when(passwordEncoder.matches("wrong_password", "encoded_password"))
                .thenReturn(false);

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertNull(response.getToken());

        verify(doiTacRepository, times(1)).findByTenDangNhap("anphuc");
        verify(passwordEncoder, times(1)).matches("wrong_password", "encoded_password");
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void login_DoiTac_TaiKhoanKhongTonTai_TraVeFail() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("khongtontai");
        request.setMatKhau("123456");
        request.setLoaiTaiKhoan("DOI_TAC");

        when(doiTacRepository.findByTenDangNhap("khongtontai"))
                .thenReturn(Optional.empty());

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertNull(response.getToken());

        verify(doiTacRepository, times(1)).findByTenDangNhap("khongtontai");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void login_LoaiTaiKhoanKhongHopLe_TraVeFail() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("admin");
        request.setMatKhau("123456");
        request.setLoaiTaiKhoan("ADMIN");

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertNull(response.getToken());

        verify(nhanVienRepository, never()).findByTenDangNhap(anyString());
        verify(doiTacRepository, never()).findByTenDangNhap(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }

    @Test
    void login_LoaiTaiKhoanNull_TraVeFail() {
        LoginRequest request = new LoginRequest();
        request.setTenDangNhap("admin");
        request.setMatKhau("123456");
        request.setLoaiTaiKhoan(null);

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertNull(response.getToken());

        verify(nhanVienRepository, never()).findByTenDangNhap(anyString());
        verify(doiTacRepository, never()).findByTenDangNhap(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyInt(), anyString(), anyString());
    }
}