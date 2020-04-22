package test;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class JavaTest {

    public static void main(String[] args) {
        float num = 12.521F;

        int in = (int) num;

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
    }
}
