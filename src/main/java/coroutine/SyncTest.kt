package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

fun main() {
//    test1()
//    test2()
//    test3()
//    test4()
    test5()
}

suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100
    val k = 1000
    val time = measureTimeMillis {
        coroutineScope {
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("completed ${n * k} actions in $time ms")
}

fun test1() = runBlocking {
    // @Volatile  volatile 并不能解决协程间的并发问题
    val counter = AtomicInteger()       // 原子类，可以保证线程间

    withContext(Dispatchers.Default) {
        massiveRun {
            counter.incrementAndGet()
        }
    }
    println("counter = $counter")
}


// 限制线程 是解决共享可变状态问题的一种方案：对特定共享状态的所有访问权都限制在单个线程中。
// 它通常应用于 UI 程序中：所有 UI 状态都局限于单个事件分发线程或应用主线程中。
// 这在协程中很容易实现，通过使用一个单线程上下文
fun test2() = runBlocking {
    val counterContext = newSingleThreadContext("CounterContext")
    var counter = 0
    // 使用这种方式会导致方法运行效率极差，因为每次计数操作都需要切换上下文
    withContext(Dispatchers.Default) {
        massiveRun {
            withContext(counterContext) {
                counter++
            }
        }
    }
    println("counter = $counter")
}

// 粗粒度限制线程
fun test3() = runBlocking {
    val counterContext = newSingleThreadContext("CounterContext")
    var counter = 0

    // 将所有协程都限制在单线程上下文中
    withContext(counterContext) {
        massiveRun {
            counter++
        }
    }
    println("counter = $counter")
}

// 互斥解决方案：使用永远不会同时执行的 关键代码块 来保护共享状态的所有修改。
// 在阻塞的世界中，你通常会为此目的使用 synchronized 或者 ReentrantLock。
// 在协程中的替代品叫做 Mutex 。它具有 lock 和 unlock 方法， 可以隔离关键的部分。
// 关键的区别在于 Mutex.lock() 是一个挂起函数，它不会阻塞线程
fun test4() = runBlocking {
    val mutex = Mutex()
    var counter = 0

    withContext(Dispatchers.Default) {
        massiveRun {
            // 此示例中锁是细粒度的，因此会付出一些代价。但是对于某些必须定期修改共享状态的场景，它是一个不错的选择，
            // 但是没有自然线程可以限制此状态。
            mutex.withLock {
                counter ++
            }
        }
    }
    println("counter = $counter")
}

sealed class CounterMsg
object IncCounter : CounterMsg()
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg()

fun test5() = runBlocking {
    val counter = counterActor()
    withContext(Dispatchers.Default) {
        massiveRun {
            counter.send(IncCounter)
        }
    }
    // 发送一条消息以用来从一个 actor 中获取计数值
    val response = CompletableDeferred<Int>()
    counter.send(GetCounter(response))
    println("Counter = ${response.await()}")
    counter.close()
}
// 一个 actor 是由协程、 被限制并封装到该协程中的状态以及一个与其它协程通信的 通道 组合而成的一个实体。
// 一个简单的 actor 可以简单的写成一个函数， 但是一个拥有复杂状态的 actor 更适合由类来表示。
//
// 有一个 actor 协程构建器，它可以方便地将 actor 的邮箱通道组合到其作用域中（用来接收消息）、
// 组合发送 channel 与结果集对象，这样对 actor 的单个引用就可以作为其句柄持有

// 使用 actor 协程构建器来启动一个 actor
fun CoroutineScope.counterActor() = actor<CounterMsg> {
    var counter = 0             // actor 状态
    for (msg in channel) {      // 即将到来消息的迭代器
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}

