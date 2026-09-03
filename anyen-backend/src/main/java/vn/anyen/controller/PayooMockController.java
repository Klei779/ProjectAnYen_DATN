package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.response.PayooMockResponse;
import vn.anyen.service.PayooMockService;

@RestController
@RequestMapping(
        "/api/payoo-mock"
)
@RequiredArgsConstructor
public class PayooMockController {

    private final PayooMockService
            payooMockService;


    @GetMapping(
            "/{maGiaoDich}"
    )
    public PayooMockResponse getGiaoDich(
            @PathVariable
            String maGiaoDich
    ) {

        return payooMockService
                .getById(
                        maGiaoDich
                );
    }


    /*
     * Giả lập callback Payoo.
     */
    @PostMapping(
            "/{maGiaoDich}/xac-nhan"
    )
    public PayooMockResponse xacNhan(
            @PathVariable
            String maGiaoDich
    ) {

        return payooMockService
                .xacNhanThanhCong(
                        maGiaoDich
                );
    }
}