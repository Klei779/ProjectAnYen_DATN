package vn.anyen.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body when a guest (not logged in) sends their geolocation.
 */
@Data
public class GuestLocationRequest {
    private String sessionId;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
