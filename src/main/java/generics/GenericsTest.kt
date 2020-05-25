package generics

interface Source<out T> {
    fun nextT(): T
}

fun demo(src: Source<String>) {
    val obj: Source<Any> = src
    println(obj.nextT())
}

interface Comparable<in T> {
    operator fun compareTo(other: T): Int
}

fun demo2(x: Comparable<Number>) {
    x.compareTo(2.33)
    val y: Comparable<Double> = x
    println(y)
}

fun main() {
//    demo(object : Source<String> {
//        override fun nextT(): String {
//            return "hhhhhhhhhh"
//        }
//    })

    demo2(object : Comparable<Number> {
        override fun compareTo(other: Number): Int {
            return other.toInt() - 0
        }
    })
}