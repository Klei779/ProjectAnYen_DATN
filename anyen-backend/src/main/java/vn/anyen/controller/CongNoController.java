package vn.anyen.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.entity.CongNo;
import vn.anyen.repository.CongNoRepository;

@RestController
@RequestMapping("/api/admin/congno")
@RequiredArgsConstructor
public class CongNoController {

    private final CongNoRepository congNoRepository;

    @GetMapping
    public ResponseEntity<Page<CongNo>> getAllCongNo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CongNo> dsCongNo = congNoRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(dsCongNo);
    }
}
