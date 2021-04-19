package test

fun main() {
    val a = 233; val b = 666

    /*println(max(a, b) {aa, bb-> aa > bb })
    println(compute(a, b, object : ComputeListener {
        override fun compute(a: Int, b: Int): Int {
            return a + b
        }
    }))*/

    val aa: A? = A(null)

    when(aa?.value) {
        1 -> println(1)
        2 -> println(2)
        null -> println(null)
        else -> println("else")
    }
}

fun max(a: Int, b: Int, com: (Int, Int) -> Boolean): Int {
    return if (com(a, b)) {
        a
    } else {
        b
    }
}

fun compute(a: Int, b: Int, listener: ComputeListener): Int {
    return listener.compute(a, b)
}

interface ComputeListener {
    fun compute(a: Int, b: Int): Int
}

class A(val value: Int? = null)