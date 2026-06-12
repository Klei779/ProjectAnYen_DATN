package vn.anyen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.LienHeRequest;
import vn.anyen.service.LienHeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/lien-he")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LienHeController {

    private final LienHeService lienHeService;

    @PostMapping("/")
    public ResponseEntity<?> guiLienHe(@Valid @RequestBody LienHeRequest request) {

        lienHeService.guiLienHe(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Gửi liên hệ thành công");
        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}