package coroutine

import kotlinx.coroutines.*
import java.text.DecimalFormat
import kotlin.system.measureTimeMillis

fun main() {
    composeTest6()
}

fun composeTest1() = runBlocking {
    // 如果使用普通的顺序来进行调用，因为这些代码是运行在协程中的，只要像常规的代码一样，顺序都是默认的
    val time = measureTimeMillis {
        // 下面这两个函数是顺序调用的，如果我们需要根据第一个函数的结果来决定下一个函数的调用时，可以用这种方式
        val one = doSomethingOne()
        val two = doSomethingTwo()
        println("the answer is ${one + two}")
    }
    println("time is $time")
}

fun composeTest2() = runBlocking {
    val time = measureTimeMillis {
        // 如果两个要执行的函数没有依赖关系，我们可以让它们 并发 运行，可以使用async 函数，
        // 在概念上async 和 launch 是类似的，它启动了一个单独的协程，这是一个轻量级的线程并与其他所有的协程一起并发工作。
        // 两者不同之处在于，launch 返回一个 Job 并且不带任何结果值，而 async 返回一个 Deferred —— 一个轻量级的非阻塞 future，
        // 这代表了一个将会在稍后提供结果的 promise，可以使用 .await() 方法获得async 的执行结果，同时 Deferred 也是一个 Job，
        // 所以需要的话，也可以取消它

        // 注意：使用协程进行并发总是显式的
        val one = async { doSomethingOne() }
        val two = async { doSomethingTwo() }
        println("the answer is ${one.await() + two.await()}")
    }
    println("time is $time")
}

fun composeTest3() = runBlocking {
    val time = measureTimeMillis {
        // async 可以通过将 start 参数设置为 CoroutineStart.LAZY 而变为惰性的，
        // 在这个模式下，只有结果通过 await 获取时，或者调用 Job 的 start 函数时，协程才会启动
        val one = async(start = CoroutineStart.LAZY) { doSomethingOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingTwo() }

        one.start()
        two.start()
        println("the answer is ${one.await() + two.await()}")   // 如果只是在这里调用 await，而没有在单独的协程中调用 start，那么这两个函数的执行就会变成顺序执行，而不是并发
    }
    println("time is $time")
}

fun composeTest4() {
    val time = measureTimeMillis {
        val one = doSomethingOneAsync()
        val two = doSomethingTwoAsync()

        runBlocking {
            println("the answer is ${one.await() + two.await()}")
        }
    }
    println("time is $time")
}
// 这种带有异步函数的编程风格仅供参考，不推荐使用，原因如下
// 如果 val one = somethingUsefulOneAsync() 这一行和 one.await() 表达式这里在代码中有逻辑错误， 并且程序抛出了异常以及程序在操作的过程中中止，将会发生什么。
// 通常情况下，一个全局的异常处理者会捕获这个异常，将异常打印成日记并报告给开发者，但是反之该程序将会继续执行其它操作。
// 但是这里我们的 somethingUsefulOneAsync 仍然在后台执行， 尽管如此，启动它的那次操作也会被终止。这个程序将不会进行结构化并发
fun doSomethingOneAsync() = GlobalScope.async {
    doSomethingOne()
}
fun doSomethingTwoAsync() = GlobalScope.async {
    doSomethingTwo()
}

fun composeTest5() = runBlocking {
    val time = measureTimeMillis {
        println("the answer is ${concurrentSum()}")
    }
    println("time is $time")
}
// 提取出一个函数并发的调用doSomethingOne这两个函数，并返回它们两个结果之和，
// 由于 async 被定义为 CoroutineScope 上的扩展函数，所以我们需要将它写在作用域内
suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingOne() }
    val two = async { doSomethingTwo() }
    one.await() + two.await()
}

suspend fun doSomethingOne(): Int {
    delay(1000)
    return 233
}

suspend fun doSomethingTwo(): Int {
    delay(2000)
    return 666
}

fun composeTest6() = runBlocking {
    try {
        failedConcurrentSum()
    } catch (e: ArithmeticException) {
        println("Computation failed with ArithmeticException")
    }
}

// 如果有一个协程中抛出了异常，所有在作用域中启动的协程都会被取消
suspend fun failedConcurrentSum(): Int = coroutineScope {
    val one = async {
        try {
            delay(Long.MAX_VALUE)
            233
        } finally {
            println("First child was cancelled")
        }
    }
    val two = async<Int> {
        println("Second child throws an exception")
        throw ArithmeticException()
    }
    one.await() + two.await()
}