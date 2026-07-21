package vn.anyen.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String TOKEN_TYPE_GUEST_CHAT = "GUEST_CHAT";

    private final String secretKey;
    private final long guestTtlHours;

    public JwtService(
            @Value("${jwt.secret:AnyenSecretKey2026LongDo123456789}") String secretKey,
            @Value("${jwt.guest-ttl-hours:48}") long guestTtlHours
    ) {
        this.secretKey = secretKey;
        this.guestTtlHours = guestTtlHours;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(
            Integer userId,
            String username,
            String role
    ) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + Duration.ofDays(7).toMillis()
                ))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateGuestChatToken(String tokenPhien) {
        Date now = new Date();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .claim("username", "guest:" + tokenPhien)
                .claim("role", "GUEST")
                .claim("tokenType", TOKEN_TYPE_GUEST_CHAT)
                .claim("sessionToken", tokenPhien)
                .issuedAt(now)
                .expiration(new Date(
                        now.getTime()
                                + Duration.ofHours(guestTtlHours).toMillis()
                ))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer getUserIdFromToken(String token) {
        return extractAllClaims(token)
                .get("userId", Integer.class);
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token)
                .get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        return extractAllClaims(token)
                .get("role", String.class);
    }

    public String getGuestSessionToken(String token) {
        return extractAllClaims(token)
                .get("sessionToken", String.class);
    }

    public String getTokenId(String token) {
        return extractAllClaims(token).getId();
    }

    public boolean isGuestChatToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return TOKEN_TYPE_GUEST_CHAT.equals(
                    claims.get("tokenType", String.class)
            );
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
