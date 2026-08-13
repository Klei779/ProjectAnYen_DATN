package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.anyen.dto.request.PushSubscriptionRequest;
import vn.anyen.dto.request.PushUnsubscribeRequest;
import vn.anyen.service.PushNotificationService;

import java.util.Map;

@RestController
@RequestMapping("/api/doi-tac/push")
@RequiredArgsConstructor
public class DoiTacPushController {

    private final PushNotificationService
            pushNotificationService;

    @GetMapping("/public-key")
    public ResponseEntity<?> getPublicKey() {

        return ResponseEntity.ok(
                Map.of(
                        "publicKey",
                        pushNotificationService
                                .getPublicKey()
                )
        );
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @Valid
            @RequestBody
            PushSubscriptionRequest request,

            Authentication authentication
    ) {

        pushNotificationService.subscribe(
                authentication,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message",
                        "Đã bật thông báo trên thiết bị"
                )
        );
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(
            @Valid
            @RequestBody
            PushUnsubscribeRequest request,

            Authentication authentication
    ) {

        pushNotificationService.unsubscribe(
                authentication,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "success", true
                )
        );
    }
}