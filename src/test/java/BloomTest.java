import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Steven
 * @date 2021-01-20
 */
public class BloomTest {

    /**
     * 黑名单用户
     */
    private static final BloomFilter<String> BLACK_USERS = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 500000, 0.01);

    /**
     * 白名单服务
     */
    private static final BloomFilter<String> WHITE_SERVICE_NAME = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 10000, 0.01);

    public static void main(String[] args) {

        System.out.println("=======================");

        System.out.println(BLACK_USERS.mightContain("SUPERUSR1"));
        System.out.println(BLACK_USERS.mightContain("SUPERUSR2"));
        BLACK_USERS.put("SUPERUSR1");
        BLACK_USERS.put("SUPERUSR2");

        isRed("SUPERUSR1");
        isRed("SUPERUSR2");
        isRed("SUPERUSR3");
    }

    /**
     * 判断是否红名单用户
     *
     * @param opId
     * @return
     */
    private static final boolean isRed(String opId) {
        boolean b = BLACK_USERS.mightContain(opId);
        if (b) {
            System.out.printf("opId: %s, %s\n", opId, "黑名单");
        } else {
            System.out.printf("opId: %s, %s\n", opId, "白名单");
        }
        return !b;
    }
}
