package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.SoTienRequest;
import vn.anyen.dto.response.DoiTacTaiChinhResponse;
import vn.anyen.dto.response.PayooMockResponse;
import vn.anyen.entity.DoiTac;
import vn.anyen.service.PayooMockService;
import vn.anyen.service.TaiChinhDoiTacService;

@RestController
@RequestMapping(
        "/api/doi-tac/tai-chinh"
)
@RequiredArgsConstructor
public class DoiTacTaiChinhController {

    private final TaiChinhDoiTacService
            taiChinhDoiTacService;

    private final PayooMockService
            payooMockService;


    @GetMapping
    public DoiTacTaiChinhResponse getThongTin(
            Authentication authentication
    ) {

        return taiChinhDoiTacService
                .getThongTin(
                        authentication
                );
    }


    @PostMapping("/mo-quy")
    public DoiTacTaiChinhResponse moQuy(
            Authentication authentication
    ) {

        return taiChinhDoiTacService
                .moQuy(
                        authentication
                );
    }


    // =============================
    // NẠP QUỸ QUA PAYOO
    // =============================

    @PostMapping(
            "/payoo/nap-quy"
    )
    public PayooMockResponse napQuy(
            Authentication authentication,

            @Valid
            @RequestBody
            SoTienRequest request
    ) {

        DoiTac doiTac =
                taiChinhDoiTacService
                        .getDoiTacDangNhap(
                                authentication
                        );


        return payooMockService
                .taoNapQuy(
                        doiTac.getMaDoiTac(),
                        request.getSoTien()
                );
    }


    // =============================
    // RÚT QUỸ
    // =============================

    @PostMapping(
            "/payoo/rut-quy"
    )
    public PayooMockResponse rutQuy(
            Authentication authentication,

            @Valid
            @RequestBody
            SoTienRequest request
    ) {

        DoiTac doiTac =
                taiChinhDoiTacService
                        .getDoiTacDangNhap(
                                authentication
                        );


        return payooMockService
                .taoRutQuy(
                        doiTac.getMaDoiTac(),
                        request.getSoTien()
                );
    }


    // =============================
    // RÚT VÍ
    // =============================

    @PostMapping(
            "/payoo/rut-vi"
    )
    public PayooMockResponse rutVi(
            Authentication authentication,

            @Valid
            @RequestBody
            SoTienRequest request
    ) {

        DoiTac doiTac =
                taiChinhDoiTacService
                        .getDoiTacDangNhap(
                                authentication
                        );


        return payooMockService
                .taoRutVi(
                        doiTac.getMaDoiTac(),
                        request.getSoTien()
                );
    }


    // =============================
    // VÍ -> QUỸ
    // =============================

    @PostMapping(
            "/chuyen-vi-vao-quy"
    )
    public DoiTacTaiChinhResponse chuyenViVaoQuy(
            Authentication authentication,

            @Valid
            @RequestBody
            SoTienRequest request
    ) {

        return taiChinhDoiTacService
                .chuyenViVaoQuy(
                        authentication,
                        request.getSoTien()
                );
    }
}