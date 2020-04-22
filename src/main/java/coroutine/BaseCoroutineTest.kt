package coroutine

import kotlinx.coroutines.*
import kotlin.concurrent.thread

fun main() {
    simpleCoroutines7()
}

//fun main() = runBlocking {
//    GlobalScope.launch {
//        delay(1000)
//        println("world")
//    }
//    print("hello,")
//    delay(2000)
//}

fun simpleCoroutines0() {
    GlobalScope.launch {
        delay(1000)
        println("hello world")
    }
    Thread.sleep(2000)             // 阻塞线程的sleep
}

fun simpleCoroutines1() {
    GlobalScope.launch {                // 在后台启动一个新的协程并继续
        delay(1000)
        println("world")
    }
    println("hello,")                   // 主线程中的代码会立即执行
    runBlocking {                       // 但是这个表达式会阻塞主线程
        delay(2000)            // 非阻塞的，挂起协程
    }
}

fun simpleCoroutines2() = runBlocking {     // 开始执行主协程
    val job = GlobalScope.launch {          // 启动一个新协程并保持对这个作业的引用
        delay(2000)
        println("world")
    }
    print("hello,")
    job.join()                              // 等待直到子协程执行结束
}

fun simpleCoroutines3() = runBlocking {
    launch {                        // 在 runBlocking 作用域中启动一个新协程
        delay(2000)
        println("world")
    }
    launch {
        delay(1000)
        print("the fuck ")
    }
    print("hello,")
}

fun simpleCoroutines4() = runBlocking {
    launch {
        delay(1000)
        println("task from runBlocking")
    }

    // runBlocking 与 coroutineScope 可能看起来很类似，因为它们都会等待其协程体以及所有子协程结束。
    // 这两者的主要区别在于，runBlocking 方法会阻塞当前线程来等待， 而 coroutineScope 只是挂起，会释放底层线程用于其他用途。
    // 由于存在这点差异，runBlocking 是常规函数，而 coroutineScope 是挂起函数。
    coroutineScope {        // 创建一个协程作用域
        // 内嵌launch
        launch {
            delay(2000)
            println("task from nested launch")
        }
        delay(500)
        println("task from nested scope")
    }

    println("coroutine scope is over")      // 这一行在内嵌launch 执行完毕之后执行
}

fun simpleCoroutines5() = runBlocking {
    launch {
        doWorld()
    }
    println("hello, ")
}

suspend fun doWorld() {         // 挂起函数
    delay(1000)
    println("world")
}

fun simpleCoroutines6() {
    // 创建10W 个线程，会报OOM
    /* repeat(100_000) {
        thread {
            Thread.sleep(1000)
            println(".")
        }
    } */

    runBlocking {
        // 但是创建10W 个协程却跟没事人儿似的，说明协程比线程轻量
        repeat(10_0000) {
            launch {
                delay(1000)
                println("hhh")
            }
        }
    }
}

fun simpleCoroutines7() = runBlocking {
    // 在 GlobalScope 中启动的活动协程并不会使进程保活。它们就像守护线程。
    GlobalScope.launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500)
        }
    }
//    launch {
//        repeat(1000) { i ->
//            println("I'm sleeping $i ...")
//            delay(500)
//        }
//    }
    delay(1300)
}