package delegate.pattern;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

public class DynamicProxy {

    public Object newProxy(Object target) {
        Class<?> clazz = target.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object res;
                long t1 = System.currentTimeMillis();
                System.out.println("pre function");
                res = method.invoke(target, args);
                long t2 = System.currentTimeMillis();
                System.out.println("after function and cost "+ (t2 - t1) + "ms");

                return res;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> targetInterface) {
        return (T) Proxy.newProxyInstance(targetInterface.getClassLoader(), new Class[]{targetInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("pre function");
                if (method.getReturnType() == String.class) {
                    String param = "";
                    if (args.length > 0 && args[0].getClass() == String.class) {
                        param = (String) args[0];
                    }
                    return param + " is fucking rong";
                } else {
                    return null;
                }
            }
        });
    }
}
