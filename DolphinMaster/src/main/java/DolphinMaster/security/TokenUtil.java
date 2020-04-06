package DolphinMaster.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenUtil {
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String createToken(String useId, String serviceName, long expire) {
        Map<String, Object> claims = new HashMap<>();
        long now_s = System.currentTimeMillis();
        claims.put("id", useId);
        JwtBuilder builder = Jwts.builder()
                .setIssuer(serviceName)
                .setClaims(claims)
                .setIssuedAt(new Date(now_s))
                .setSubject(useId)
                .signWith(key);
        if (expire >= 0) {
            long expire_time = now_s + expire;
            builder.setExpiration(new Date(expire_time));
        }
        return builder.compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
