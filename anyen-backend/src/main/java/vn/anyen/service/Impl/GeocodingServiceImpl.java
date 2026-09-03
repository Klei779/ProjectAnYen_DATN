package vn.anyen.service.Impl;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import vn.anyen.dto.response.DiaChiRespone;
import vn.anyen.service.GeocodingService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
@Service
@RequiredArgsConstructor

    public class GeocodingServiceImpl implements GeocodingService {

        private final RestTemplate restTemplate;

        @Override
        public DiaChiRespone getLocation(String address) {
            if (address == null || address.trim().isEmpty()) {
                return null;
            }

            try {
                String url =
                        "https://nominatim.openstreetmap.org/search?q="
                                + UriUtils.encode(address.trim(), StandardCharsets.UTF_8)
                                + "&countrycodes=vn"
                                + "&format=json"
                                + "&limit=1";
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "AnYen-DATN/1.0 (contact: admin@anyen.vn)");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                if (response.getBody() == null) {
                    return null;
                }

                JSONArray array = new JSONArray(response.getBody());

                if (array.length() == 0) {
                    return null;
                }

                JSONObject object = array.getJSONObject(0);

                System.out.println("LAT = " + object.getString("lat"));
                System.out.println("LON = " + object.getString("lon"));
                System.out.println("DISPLAY = " + object.getString("display_name"));

                return new DiaChiRespone(
                        BigDecimal.valueOf(object.getDouble("lat")),
                        BigDecimal.valueOf(object.getDouble("lon")),
                        object.getString("display_name")
                );
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi Nominatim geocode: " + e.getMessage());
                return null;
            }
        }
}
