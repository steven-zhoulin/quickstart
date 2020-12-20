import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven
 * @date 2020-12-20
 */
public class GenerateKeyTest {

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 10000; i++) {
            test();
            Thread.sleep(1000);
        }

    }

    private static final void test() {
        long start = System.currentTimeMillis();
        List<String> list = new ArrayList<>(10000);
        for (int i = 0; i < 10000; i++) {

            StringBuilder buff = new StringBuilder(128);
            for (int j = 0; j < 6; j++) {
                buff.append(RandomStringUtils.randomAlphabetic(8, 15).toLowerCase()).append(".");
            }
            buff.append(RandomStringUtils.randomAlphabetic(8, 15).toLowerCase());
            list.add(buff.toString());
        }
        //System.out.println("生产1W个包路径耗时: " + (System.currentTimeMillis() - start) + "ms");

        List<String> list2 = new ArrayList<>(10000);
        start = System.currentTimeMillis();
        for (String packageName : list) {
            StringBuilder buff = new StringBuilder(packageName.length() * 2);
            char[] chars = packageName.toCharArray();

            buff.append("@").append(chars[0]);
            for (int i = 1; i < chars.length; i++) {
                if ('.' == chars[i]) {
                    char c = chars[i + 1];
                    buff.append('.').append(c);
                    i++;
                }
            }
            list2.add(buff.toString());
        }
        System.out.println("简化1W个包路径耗时: " + (System.currentTimeMillis() - start) + "ms");
    }

}
