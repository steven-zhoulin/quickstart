
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

/**
 * @author Steven
 * @date 2021-01-04
 */
public class Main {

    public static void main(String[] args) throws Exception {
        List<String> lines = FileUtils.readLines(new File("C:/Users/Steven/Downloads/hn_xt_tns_1988-DNS.ora"));
        StringBuilder buff = new StringBuilder();
        for (String line : lines) {
            if ("".equals(line.trim())) {
                sql(buff.toString());
                buff = new StringBuilder();
            } else {
                buff.append(line);
                buff.append("\n");
            }
        }

    }

    private static void sql(String content) {
        String[] split = StringUtils.split(content, '\n');
        String urlCode = StringUtils.replace(split[0], "=", "");
        urlCode = StringUtils.replace(urlCode, " ", "");
        urlCode = StringUtils.replace(urlCode, "zz_", "xt_");
        int i = content.indexOf("(description");
        String url = "jdbc:oracle:thin:@" + content.substring(i);
        System.out.printf("INSERT INTO WD_DATASOURCE_URL(URL_CODE, URL, STATE) VALUES('%s', '%s', 'U');\n", urlCode, url);
    }
}
