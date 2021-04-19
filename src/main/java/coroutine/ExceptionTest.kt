package coroutine

import kotlinx.coroutines.*
import java.lang.AssertionError
import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException

fun main() {
//    exceptionTest1()
//    exceptionTest2()
//    exceptionTest3()
//    exceptionTest4()
//    exceptionTest5()
//    exceptionTest6()
//    exceptionTest7()
//    exceptionTest8()
    exceptionTest9()
}

fun exceptionTest1() = runBlocking {
    val job = GlobalScope.launch {
        println("throwing exception from launch")
        throw IndexOutOfBoundsException()
    }
    job.join()
    println("joined failed job")

    val deferred = GlobalScope.async {
        println("throwing exception from async")
        throw ArithmeticException()
    }

    try {
        deferred.await()
        println("unreached")
    } catch (e: Exception) {
        println("caught ArithmeticException")
    }
}

fun exceptionTest2() = runBlocking {
    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println("CoroutineExceptionHandler got $throwable")
    }
    val job = GlobalScope.launch(handler) {
        throw AssertionError()
    }
    val deferred = GlobalScope.async(handler) {
        throw ArithmeticException()
    }
    joinAll(job, deferred)
}

fun exceptionTest3() = runBlocking {
    val job = launch {
        val child = launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("child is cancelled")
            }
        }
        yield()
        println("cancelling child")
        child.cancel()
        child.join()
        yield()
        println("parent is not cancelled")
    }
    job.join()
}

fun exceptionTest4() = runBlocking {
    // CoroutineExceptionHandler 总是被设置在由 GlobalScope 启动的协程中。
    // 将异常处理者设置在 runBlocking 主作用域内启动的协程中是没有意义的，尽管子协程已经设置了异常处理者， 但是主协程也总是会被取消的
    val handler = CoroutineExceptionHandler { _, throwable ->
        println("CoroutineExceptionHandler got $throwable")
    }
    val job = GlobalScope.launch(handler) {
        launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                withContext(NonCancellable) {
                    println("Children are cancelled, but exception is not handled until all children terminate")
                    delay(100)
                    println("The first child finished its non cancellable block")
                }
            }
        }
        // 如果一个协程遇到了 CancellationException 以外的异常，它将使用该异常取消它的父协程。
        // 这个行为无法被覆盖，并且用于为结构化的并发（structured concurrency） 提供稳定的协程层级结构
        launch {
            delay(10)
            println("Second child throws an exception")
            throw ArithmeticException()
        }
    }
    job.join()
}

// 当协程的多个子协程因异常而失败时， 一般规则是“取第一个异常”，因此将处理第一个异常。
// 在第一个异常之后发生的所有其他异常都作为被抑制的异常绑定至第一个异常。
fun exceptionTest5() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
    }

    val job = GlobalScope.launch(handler) {
        launch {
            try {
                delay(Long.MAX_VALUE)       // 当另一个同级的协程因 IllegalArgumentException  失败时，它将被取消
            } finally {
                throw ArithmeticException()             // 第二个异常
            }
        }

        launch {
            delay(100)
            throw IllegalArgumentException()            // 首个异常
        }

        delay(Long.MAX_VALUE)
    }
    job.join()
}

// 取消异常是透明的，默认情况下是未包装的
fun exceptionTest6() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    val job = GlobalScope.launch(handler) {
        val inner = launch {
            launch {
                launch {
                    throw ArithmeticException()         // 原始异常
                }
            }
        }
        try {
            inner.join()
        } catch (e: CancellationException) {
            println("Rethrowing CancellationException with original cause")
            throw e                 // 取消异常被重新抛出，但原始 IOException 得到了处理
        }
    }
    job.join()
}

fun exceptionTest7() = runBlocking {
    val supervisor = SupervisorJob()

    with(CoroutineScope(coroutineContext + supervisor)) {
        // 启动第一个子作业——这个示例将会忽略它的异常（不要在实践中这么做！）
        val firstChild = launch(CoroutineExceptionHandler { _, _ ->  }) {
            println("The first child is failing")
            throw AssertionError("The first child is cancelled")
        }
        // 启动第二个子作业
        val secondChild = launch {
            firstChild.join()
            // 取消了第一个子作业且没有传播给第二个子作业
            println("The first child is cancelled: ${firstChild.isCancelled}, but the second one is still active")
            try {
                delay(Long.MAX_VALUE)
            } finally {
                // 但是取消了监督的传播
                println("The second child is cancelled because the supervisor was cancelled")
            }
        }

        // 等待直到第一个子作业失败且执行完成
        firstChild.join()
        println("Cancelling the supervisor")
        supervisor.cancel()
        secondChild.join()
    }
}

fun exceptionTest8() = runBlocking {
    try {
        supervisorScope {
            val child = launch {
                try {
                    println("child is sleeping")
                    delay(Long.MAX_VALUE)
                } finally {
                    println("child is cancelled")
                }
            }

            yield()
            println("Throwing an exception from the scope")
            throw AssertionError()
        }
    } catch (e: AssertionError) {
        println("Caught an assertion error")
    }
}

// 常规的作业和监督作业之间的另一个重要区别是异常处理。
// 监督协程中的每一个子作业应该通过异常处理机制处理自身的异常。
// 这种差异来自于子作业的执行失败不会传播给它的父作业的事实。
// 这意味着在 supervisorScope 内部直接启动的协程确实使用了设置在它们作用域内的 CoroutineExceptionHandler，与父协程的方式相同
fun exceptionTest9() = runBlocking {
    val handler = CoroutineExceptionHandler { _, e ->
        println("CoroutineExceptionHandler got $e")
    }
    supervisorScope {
        val child = launch(handler) {
            println("The child throws an exception")
            throw AssertionError()
        }
//        child.join()
        println("The scope is completing")
    }
    println("The scope is completed")
}