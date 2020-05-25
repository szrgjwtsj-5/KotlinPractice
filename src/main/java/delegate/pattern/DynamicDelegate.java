package delegate.pattern;

public class DynamicDelegate {

    public static void main(String[] args) {
//        StaticProxy qiang = new StaticProxy(new ActImpl());
//        System.out.println(qiang.fuckRong("jj"));
//
//        IFunctions qiang1 = (IFunctions) new DynamicProxy().newProxy(qiang);
//        System.out.println(qiang1.fuckRong("jj"));

        IFunctions f = new DynamicProxy().createProxy(IFunctions.class);
        System.out.println(f.fuckRong("jj"));
    }
}
