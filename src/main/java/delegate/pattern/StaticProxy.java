package delegate.pattern;

public class StaticProxy implements IFunctions {

    private ActImpl jj;

    public StaticProxy(ActImpl jj) {
        this.jj = jj;
    }

    @Override
    public String fuckRong(String name) {
        System.out.println("pre function");
        String res = jj.fuckRong(name);
        System.out.println("after function");
        return res;
    }
}
