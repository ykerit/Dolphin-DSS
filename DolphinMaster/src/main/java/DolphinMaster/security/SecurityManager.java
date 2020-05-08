package DolphinMaster.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecurityManager {
    private static final Logger log = LogManager.getLogger(SecurityManager.class.getName());
    public boolean checkExpire(String token) {
        boolean isExpire = false;
        try {
            Claims claims = TokenUtil.parseToken(token);
            log.info("subject: {}", claims.getSubject());
        } catch (ExpiredJwtException e) {
            log.info("token expire: {}", e.getMessage());
            isExpire = true;
        } finally {
            return isExpire;
        }
    }

    public String genToken(String useId, String serviceName) {
        return TokenUtil.createToken(useId, serviceName, 60 * 1000);
    }
}
