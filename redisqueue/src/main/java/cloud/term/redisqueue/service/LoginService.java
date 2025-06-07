package cloud.term.redisqueue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final StringRedisTemplate redisTemplate;

    private static final String ID_COOKIE_HASH = "login:id-cookie";
    private static final String COOKIE_ID_HASH = "login:cookie-id";

    // 항상 true 반환 (Stub)
    public boolean authenticate(String id, String password) {
        return true;
    }

    public void registerSession(String id, String cookie) {

        Object oldCookie = redisTemplate.opsForHash().get(ID_COOKIE_HASH, id);
        if (oldCookie != null) {
            redisTemplate.opsForHash().delete(COOKIE_ID_HASH, oldCookie.toString());
        }

        redisTemplate.opsForHash().put(ID_COOKIE_HASH, id, cookie);
        redisTemplate.opsForHash().put(COOKIE_ID_HASH, cookie, id);
    }

    public boolean isValidCookie(String cookie) {
        String storedId = getIdFromCookie(cookie);
        if (storedId == null) { return false; }
        String storedCookie = getCookieFromId(storedId);
        return storedCookie != null && storedCookie.equals(cookie);
    }

    public String getIdFromCookie(String cookie) {
        Object id = redisTemplate.opsForHash().get(COOKIE_ID_HASH, cookie);
        return id != null ? id.toString() : null;
    }

    public String getCookieFromId(String id) {
        Object cookie = redisTemplate.opsForHash().get(ID_COOKIE_HASH, id);
        return cookie != null ? cookie.toString() : null;
    }
}