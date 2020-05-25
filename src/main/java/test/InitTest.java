package test;

/**
 * 静态成员和构造函数的初始化顺序，父类静态 -> 子类静态 -> 父类构造函数 -> 子类构造函数
 */
public class InitTest {

    public static void main(String[] args) {
//        Child c = new Child();
        Child.func1();
    }

    public static class Tmp {
        static int getInt(String author) {
            System.out.println(author + " make int");
            return 233;
        }
    }
    // 初始化顺序如下注释顺序
    public static class Parent {
        public static int aa = Tmp.getInt("Parent");                    // 1
        private int aaa = Tmp.getInt("parent field");                   // 3

        public Parent() {
            System.out.println("parent construct");
        }            // 4
        public static void func1() {
            System.out.println("parent func1");
        }
    }
    public static class Child extends Parent {
        public static int bb = Tmp.getInt("Child");                     // 2
        private int bbb = Tmp.getInt("child field");                    // 5

        public Child() {
            System.out.println("child construct");
        }              // 6

        public static void func1() {
            System.out.println("child func2");
        }
    }
}
