package cloud.term.redisqueue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final StringRedisTemplate redisTemplate;

    private static final String ID_COOKIE_HASH = "login:id-cookie";
    private static final String COOKIE_ID_HASH = "login:cookie-id";
    private static final String VALID_USER_SET = "login:valid-users-id";

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

    public boolean isValid(String id, String cookie) {
        Object storedCookie = redisTemplate.opsForHash().get(ID_COOKIE_HASH, id);
        return storedCookie != null && storedCookie.equals(cookie);
    }

    public boolean isValidCookie(String cookie) {
        Object storedId = redisTemplate.opsForHash().get(COOKIE_ID_HASH, cookie);
        if (storedId == null) { return false; }
        Object storedCookie = redisTemplate.opsForHash().get(ID_COOKIE_HASH, storedId);

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
    public boolean idExists(String id) {
        return redisTemplate.opsForHash().hasKey(ID_COOKIE_HASH, id);
    }
    public boolean isUserValid(String id) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(VALID_USER_SET, id));
    }

    public void addValidUser(String id) {
        redisTemplate.opsForSet().add(VALID_USER_SET, id);
    }

    public void removeValidUser(String id) {
        redisTemplate.opsForSet().remove(VALID_USER_SET, id);
    }
}