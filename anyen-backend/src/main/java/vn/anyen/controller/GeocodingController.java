package vn.anyen.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.anyen.dto.request.DiaChiRequest;
import vn.anyen.dto.response.DiaChiRespone;
import vn.anyen.service.GeocodingService;

@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")

    public class GeocodingController {

        private final GeocodingService geocodingService;

        @PostMapping
        public ResponseEntity<DiaChiRespone> geocode(
                @RequestBody DiaChiRequest request
        ) {

            DiaChiRespone response =
                    geocodingService.getLocation(request.getDiaChi());

            return ResponseEntity.ok(response);
        }
    }
