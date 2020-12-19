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
                //.expireAfterAccess(10, TimeUnit.SECONDS)
                .maximumSize(200000)
                .softValues()
                .build();

        long start = System.currentTimeMillis();
        for (int i = 1; i <= 100000; i++) {
            String key = "k" + i;
            String val = "v" + i;
            localCache.put(key, val);
        }
        System.out.println("存入10W个KV, 耗时: " + (System.currentTimeMillis() - start) + "ms");

        while (true) {
            start = System.currentTimeMillis();
            for (int i = 1; i <= 100000; i++) {
                String key = "k" + i;
                Object value = localCache.getIfPresent(key);
                if (i % 20000 == 0) {
                    System.out.println(LocalDateTime.now() + ": " + value);
                }
            }
            System.out.println("访问10W个KV, 耗时: " + (System.currentTimeMillis() - start) + "ms");
            Thread.sleep(1000);
        }
    }
}
