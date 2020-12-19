import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 * @date 2020-12-19
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        Cache<String, Object> localCache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .maximumSize(10000)
                .softValues()
                .build();

        for (int i = 1; i < 1000000000; i++) {
            String key = "k" + i;
            String val = "v" + i;
            localCache.put(key, val);
            LocalDateTime now = LocalDateTime.now();
            System.out.println(now + ": " + localCache.asMap());
            Thread.sleep(1000);
        }
    }
}
