package test;

/**
 * 静态成员和构造函数的初始化顺序，父类静态 -> 子类静态 -> 父类构造函数 -> 子类构造函数
 */
public class InitTest {

    public static void main(String[] args) {
        Child c = new Child();
    }

    public static class Tmp {
        static int getInt(String author) {
            System.out.println(author + " make int");
            return 233;
        }
    }
    public static class Parent {
        public static int aa = Tmp.getInt("Parent");
        public Parent() {
            System.out.println("parent construct");
        }
        public static void func1() {
            System.out.println("parent func1");
        }
    }
    public static class Child extends Parent {
        public static int bb = Tmp.getInt("Child");

        public Child() {
            System.out.println("child construct");
        }

        public static void func1() {
            System.out.println("child func2");
        }
    }
}
