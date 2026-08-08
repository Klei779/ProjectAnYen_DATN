package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.request.SoTienRequest;
import vn.anyen.dto.response.CongNoResponse;
import vn.anyen.dto.response.PayooMockResponse;

import vn.anyen.service.CongNoService;
import vn.anyen.service.PayooMockService;

@RestController
@RequestMapping("/api/admin/congno")
@RequiredArgsConstructor
public class CongNoController {

    private final CongNoService
            congNoService;

    private final PayooMockService
            payooMockService;


    // =================================================
    // DANH SÁCH CÔNG NỢ
    // =================================================

    @GetMapping
    public ResponseEntity<Page<CongNoResponse>>
    getAllCongNo(

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    required = false
            )
            Integer trangThai
    ) {

        return ResponseEntity.ok(

                congNoService
                        .getDanhSach(
                                page,
                                size,
                                trangThai
                        )
        );
    }


    // =================================================
    // THANH TOÁN CÔNG NỢ PAYOO
    // =================================================

    @PostMapping(
            "/{maCongNo}/payoo"
    )
    public PayooMockResponse thanhToanPayoo(

            @PathVariable
            Integer maCongNo,

            @Valid
            @RequestBody
            SoTienRequest request
    ) {

        return payooMockService
                .taoThanhToanCongNo(
                        maCongNo,
                        request.getSoTien()
                );
    }
}