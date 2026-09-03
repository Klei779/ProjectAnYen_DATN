package vn.anyen.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GuestChatAuthService {

    private final JwtService jwtService;
    private final ChatRedisService chatRedisService;

    public GuestChatAuthService(
            JwtService jwtService,
            ChatRedisService chatRedisService
    ) {

        this.jwtService = jwtService;
        this.chatRedisService = chatRedisService;
    }

    public void requireSession(
            String authorizationHeader,
            String tokenPhien
    ) {
        String jwt = extractBearerToken(authorizationHeader);

        if (!jwtService.isTokenValid(jwt)
                || !jwtService.isGuestChatToken(jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Phiên đăng nhập khách đã hết hạn"
            );
        }

        String sessionFromJwt = jwtService.getGuestSessionToken(jwt);
        if (sessionFromJwt == null || !sessionFromJwt.equals(tokenPhien)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "JWT khách hàng không thuộc phiên tư vấn này"
            );
        }

        String tokenId = jwtService.getTokenId(jwt);
        if (!chatRedisService.isGuestSessionAllowed(tokenId, tokenPhien)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Phiên đăng nhập khách không còn hiệu lực"
            );
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Thiếu JWT của khách hàng"
            );
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "JWT của khách hàng không hợp lệ"
            );
        }

        return token;
    }
}
