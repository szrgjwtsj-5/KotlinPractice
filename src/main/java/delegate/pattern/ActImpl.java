package delegate.pattern;

public class ActImpl implements IFunctions {

    public ActImpl() {
    }

    @Override
    public String fuckRong(String name) {
        return name + " is fucking rong...";
    }
}
