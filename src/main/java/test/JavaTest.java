package test;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class JavaTest {

    public static void main(String[] args) {
        float num = 12.521F;

        int in = (int) num;

        /*
        DecimalFormat df = new DecimalFormat("#");
        String idf = df.format(num);

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(0);
        String inf = nf.format(num);

        BigDecimal bd = new BigDecimal(num);
        int ibd = bd.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

        System.out.println(ibd);
        System.out.println(inf);
        System.out.println(idf);
        System.out.println(String.format("%.0f", num));
        System.out.println(in);

        System.out.println(URLDecoder.decode("https://graph.facebook.com/v3.2/2665413863779534/picture?height=500&width=500&migration_overrides=%7Boctober_2012%3Atrue%7D"));
        */

        System.out.println(tableSizeFor(3));
        System.out.println(tableSizeFor(6));
        System.out.println(tableSizeFor(8));
        System.out.println(tableSizeFor(9));
        System.out.println(tableSizeFor(12));
    }

    static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : n + 1;
    }
}
