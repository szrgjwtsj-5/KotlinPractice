package coroutine

import kotlinx.coroutines.*
import java.lang.Exception

fun main() {
    timeoutTest2()
}

fun cancelTest1() = runBlocking {
    val job = launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500)
        }
    }
    delay(1300)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
//    job.join()
    println("main: Now I can quit")
}

// 协程的取消是 协作 的，一段协程代码必须协作才能取消，所有 kotlinx.coroutines 中的挂起函数都是可被取消的，
// 它们检查协程的取消，并在取消时抛出 CancellationException。然而，如果协程在执行计算任务，并且没有检查取消的话，那么它是不能被取消的
fun cancelTest2() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextTime = startTime
        var i = 0
        while (i < 5) {
            if (System.currentTimeMillis() >= nextTime) {
                println("job: I'm sleeping ${i++} ...")
                nextTime += 500
            }
        }
    }
    delay(1300)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit")
}

fun cancelTest3() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextTime = startTime
        var i = 0
        while (isActive) {          // isActive 是一个可以被使用在 CoroutineScope 中的扩展属性
            if (System.currentTimeMillis() >= nextTime) {
                println("job: I'm sleeping ${i++} ...")
                nextTime += 500
            }
        }
    }
    delay(1300)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit")
}

// 我们有两种方法来使执行计算的代码可以被取消。
// 第一种方法是定期调用挂起函数来检查取消。对于这种目的 yield 是一个好的选择。
// 另一种方法是显式的检查取消状态，下面的就是这种方法
fun cancelTest4() = runBlocking {
    val job = launch {
        // 我们通常使用如下的方法处理在被取消时抛出 CancellationException 的可被取消的挂起函数
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500)
            }
        } finally {
            println("job: I'm running finally")
        }
    }
    delay(1300)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit")
}

fun cancelTest5() = runBlocking {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500)
            }
        } finally {
            // 任何尝试在 finally 块中调用挂起函数的行为都会抛出 CancellationException，
            // 因为这里持续运行的代码是可以被取消的。
            /*delay(1000)
            println("job: I'm running finally")*/

            // 通常，这并不是一个问题，所有良好的关闭操作（关闭一个文件、取消一个作业、或是关闭任何一种通信通道）通常都是非阻塞的，并且不会调用任何挂起函数。
            // 然而，在真实的案例中，当你需要挂起一个被取消的协程，你可以将相应的代码包装在 withContext(NonCancellable) {……} 中，
            // 并使用 withContext 函数以及 NonCancellable 上下文
            withContext(NonCancellable) {
                println("job: I'm running finally")
                delay(1000)
                println("job: And I've just delayed for 1 sec because I'm non-cancellable")
            }
        }
    }
    delay(1300)
    println("main: I'm tired of waiting")
    job.cancelAndJoin()
    println("main: Now I can quit")
}

// 在实践中绝大多数取消一个协程的理由是它有可能超时。
// 当你手动追踪一个相关 Job 的引用并启动了一个单独的协程在延迟后取消追踪，这里已经准备好使用 withTimeout 函数来做这件事
fun timeoutTest1() = runBlocking {
    withTimeout(1300) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
}

// withTimeout 抛出了 TimeoutCancellationException，它是 CancellationException 的子类。
// 我们之前没有在控制台上看到堆栈跟踪信息的打印。这是因为在被取消的协程中 CancellationException 被认为是协程执行结束的正常原因。
// 然而，在这个示例中我们在 main 函数中正确地使用了 withTimeout。
// 由于取消只是一个例外，所有的资源都使用常用的方法来关闭。 如果你需要做一些各类使用超时的特别的额外操作，
// 可以使用类似 withTimeout 的 withTimeoutOrNull 函数，
// 并把这些会超时的代码包装在 try {...} catch (e: TimeoutCancellationException) {...} 代码块中，
// 而 withTimeoutOrNull 通过返回 null 来进行超时操作，从而替代抛出一个异常
fun timeoutTest2() = runBlocking {
    val result = withTimeoutOrNull(1300) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "done"
    }
    println("result is $result")
}