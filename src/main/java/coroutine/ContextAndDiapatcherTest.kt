package coroutine

import kotlinx.coroutines.*

fun main() {
    contextTest7()
}

//协程上下文包含一个 协程调度器 （参见 CoroutineDispatcher）它确定了哪些线程或与线程相对应的协程执行。
// 协程调度器可以将协程限制在一个特定的线程执行，或将它分派到一个线程池，亦或是让它不受限地运行。
//
//所有的协程构建器诸如 launch 和 async 接收一个可选的 CoroutineContext 参数，它可以被用来显式的为一个新协程或其它上下文元素指定一个调度器。
fun contextTest1() = runBlocking {

    // 运行在父协程的上下文中，即runBlocking 主协程
    launch {
        println("main runBlocking  : I'm working in thread ${Thread.currentThread().name}")
    }

    // 不受限的 —— 将工作在主线程中，但是实际上，它是一种不同的机制
    launch(Dispatchers.Unconfined) {
        println("Unconfined  : I'm working in thread ${Thread.currentThread().name}")
    }

    // 将会获取默认调度器，当协程在GlobalScope 中启动的时候使用，它代表Dispatchers.Default 使用了共享的后台线程池
    launch(Dispatchers.Default) {
        println("Default  : I'm working in thread ${Thread.currentThread().name}")
    }

    // 为协程的运行启动了新的一个线程，在真实的应用程序中两者都必须被释放，当不再需要的时候，使用close 函数，
    // 或存储在一个顶层变量中使它在整个应用程序中被重用
    launch(newSingleThreadContext("MyOwnThread")) {
        println("newSingleThreadContext  : I'm working in thread ${Thread.currentThread().name}")
    }
}

// Dispatchers.Unconfined 协程调度器在调用它的线程启动了一个协程，但它仅仅只是运行到第一个挂起点。
// 挂起后，它恢复线程中的协程，而这完全由被调用的挂起函数来决定。非受限的调度器非常适用于执行不消耗 CPU 时间的任务，以及不更新局限于特定线程的任何共享数据（如UI）的协程。
//
// 另一方面，该调度器默认继承了外部的 CoroutineScope。 runBlocking 协程的默认调度器，
// 特别是， 当它被限制在了调用者线程时，继承自它将会有效地限制协程在该线程运行并且具有可预测的 FIFO 调度
fun contextTest2() = runBlocking {

    // 该协程的上下文继承自 runBlocking {...} 协程并在 main 线程中运行，当 delay 函数调用的时候，非受限的那个协程在默认的执行者线程中恢复执行。
    launch(Dispatchers.Unconfined) {
        println("Unconfined  : I'm working in thread ${Thread.currentThread().name}")
        delay(500)
        println("Unconfined  : After delay in thread ${Thread.currentThread().name}")
    }

    launch {
        println("main runBlocking  : I'm working in thread ${Thread.currentThread().name}")
        delay(1000)
        println("main runBlocking  : After delay in thread ${Thread.currentThread().name}")
    }
}

// 协程可以在一个线程上挂起并在其它线程上恢复。 甚至一个单线程的调度器也是难以弄清楚协程在何时何地正在做什么事情。
// 使用通常调试应用程序的方法是让线程在每一个日志文件的日志声明中打印线程的名字。这种特性在日志框架中是普遍受支持的。
// 但是在使用协程时，单独的线程名称不会给出很多协程上下文信息，所以 kotlinx.coroutines 包含了调试工具来让它更简单。
//
//使用 -Dkotlinx.coroutines.debug JVM 参数运行下面的代码
fun debugTest1() = runBlocking {
    val a = async {
        log("I'm computing a piece of the answer")
        6
    }
    val b = async {
        log("I'm computing another piece of the answer")
        6
    }
    log("the answer is ${a.await() * b.await()}")
}

fun debugTest2() {
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            // 使用runBlocking 来显式指定一个上下文
            runBlocking(ctx1) {
                log("Started in ctx1")
                // 使用withContext 来改变协程的上下文，但是仍然驻留在相同的协程中
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }
}

// 协程的 Job 是上下文的一部分，并且可以使用 coroutineContext[Job] 表达式在上下文中检索它
fun contextTest3() = runBlocking {
    println("My Job is ${coroutineContext[Job]}")
}

// 当一个协程被其它协程在 CoroutineScope 中启动的时候， 它将通过 CoroutineScope.coroutineContext 来承袭上下文，
// 并且这个新协程的 Job 将会成为父协程作业的 子 作业。当一个父协程被取消的时候，所有它的子协程也会被递归的取消。
//
// 然而，当使用 GlobalScope 来启动一个协程时，则新协程的作业没有父作业。 因此它与这个启动的作用域无关且独立运作。
fun contextTest4() = runBlocking {
    // 启动一个协程来处理某种传入请求
    val request = launch {
        // 孵化了两个子作业，其中一个通过GlobalScope 启动
        GlobalScope.launch {
            println("job1: I run in GlobalScope and execute independently!")
            delay(1000)
            println("job1: I am not affected by cancellation of the request")                   // 1
        }
        // 另一个则承袭了父协程的上下文
        launch {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancelled")     // 2
        }
    }
    delay(500)
    request.cancel()            // 它内部的子协程也会被取消，所以 2 处的代码不会被执行，但是1 处代码不受影响
    delay(1000)
    println("main: Who has survived request cancellation?")
}

// 一个父协程总是等待所有的子协程执行结束。父协程并不显式的跟踪所有子协程的启动，并且不必使用 Job.join 在最后的时候等待它们
fun contextTest5() = runBlocking {
    val request = launch {
        repeat(3) { i ->            // 启动少量的子作业
            launch {
                delay((i + 1) * 200L)
                println("Coroutine #$i is done")
            }
        }
        println("request: I'm done and I don't explicitly join my children that are still active")
    }
    request.join()      // 等待请求的完成，包括其所有子协程
    println("Now processing of the request is complete")
}

// 当协程经常打印日志并且你只需要关联来自同一个协程的日志记录时， 则自动分配的 id 是非常好的。
// 然而，当一个协程与特定请求的处理相关联时或做一些特定的后台任务，最好将其明确命名以用于调试目的。
// CoroutineName 上下文元素与线程名具有相同的目的。当调试模式开启时，它被包含在正在执行此协程的线程名中。
fun debugTest3() = runBlocking {
    log("Started main coroutine")
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        233
    }

    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        666
    }
    log("The answer for v1 / v2 = ${v1.await() / v2.await()}")
}

// 有时我们需要在协程上下文中定义多个元素。我们可以使用 + 操作符来实现。
// 比如说，我们可以显式指定一个调度器来启动协程并且同时显式指定一个命名：
fun contextTest6() = runBlocking {
    launch(Dispatchers.Default + CoroutineName("test")) {
        log("I'm working")
    }
}

// 假设我们的应用程序拥有一个具有生命周期的对象，但这个对象并不是一个协程。
// 举例来说，我们编写了一个 Android 应用程序并在 Android 的 activity 上下文中启动了一组协程来使用异步操作拉取并更新数据以及执行动画等等。
// 所有这些协程必须在这个 activity 销毁的时候取消以避免内存泄漏。当然，我们也可以手动操作上下文与作业，以结合 activity 的生命周期与它的协程，
// 但是 kotlinx.coroutines 提供了一个封装：CoroutineScope 的抽象。 你应该已经熟悉了协程作用域，因为所有的协程构建器都声明为在它之上的扩展。
fun contextTest7() = runBlocking {
    val activity = Activity()
    activity.doSomething()
    println("Launched coroutines")
    delay(500)
    println("activity destroy")
    activity.onDestroy()
    delay(1000)
}

// 我们通过创建一个 CoroutineScope 实例来管理协程的生命周期，并使它与 activit 的生命周期相关联。
// CoroutineScope 可以通过 CoroutineScope() 创建或者通过MainScope() 工厂函数。前者创建了一个通用作用域，
// 而后者为使用 Dispatchers.Main 作为默认调度器的 UI 应用程序 创建作用域：
class Activity0 {
    private val mainScope = MainScope()

    fun onDestroy() {
        mainScope.cancel()
    }
}
// 我们可以在这个 Activity 类中实现 CoroutineScope 接口。最好的方法是使用具有默认工厂函数的委托。
// 我们也可以将所需的调度器与作用域合并（我们在这个示例中使用 Dispatchers.Default）
class Activity : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    fun doSomething() {
        repeat(10) { i ->
            launch {
                delay((i + 1) * 200L)
                println("coroutine #$i is done")
            }
        }
    }

    fun onDestroy() {
        cancel()
    }
}

// 在这个例子中我们使用 Dispatchers.Default 在后台线程池中启动了一个新的协程，所以它工作在线程池中的不同线程中，但它仍然具有线程局部变量的值，
// 我们指定使用 threadLocal.asContextElement(value = "launch")， 无论协程执行在什么线程中都是没有问题的
val threadLocal = ThreadLocal<String?>()
fun contextTest8() = runBlocking {
    threadLocal.set("main")
    println("pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        println("launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        yield()
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }
    job.join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
}