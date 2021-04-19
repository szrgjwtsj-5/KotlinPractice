package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun main() {
//    channelTest1()
//    closeChTest()
//    channelTest2()
//    channelTest3()
//    channelTest4()
//    channelTest5()
//    channelTest6()
//    capacityTest()
//    channelTest7()
    channelTest8()
}

fun channelTest1() = runBlocking {
    val channel = Channel<Int>()

    launch {
        (1..5).forEach {
            channel.send(it * it)
        }
    }
    repeat(5) {
        println(channel.receive())
    }
    println("done")
}

fun closeChTest() = runBlocking {
    val channel = Channel<Int>()
    launch {
        for (x in (1..5)) {
            if (channel.isClosedForSend) {
                break
            }
            channel.send(x * x)
            if (x == 3) {
                channel.close()
            }
        }
//        channel.close()
    }
    for (i in channel) {
        println(i)
    }
    println("done")
}

// 构建通道生产者
// 协程生成一系列元素的模式很常见。 这是 生产者——消费者 模式的一部分，并且经常能在并发的代码中看到它。
// 你可以将生产者抽象成一个函数，并且使通道作为它的参数
fun CoroutineScope.produceSquare(): ReceiveChannel<Int> = produce {
    for (x in (1..5)) send(x * x)
}
fun channelTest2() = runBlocking {
    val squares = produceSquare()
    squares.consumeEach {
        println(it)
    }
    println("done")
}

// 管道是一种一个协程在流中开始生产可能无穷多个元素的模式
fun CoroutineScope.produceNumbers() = produce {
    var x = 1
    while (true) {
        send(x++)
    }
}
// 并且另一个或多个协程开始消费这些流，做一些操作，并生产了一些额外的结果
fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (x in numbers) send(x * x)
}
fun channelTest3() = runBlocking {
    val numbers = produceNumbers()
    val squares = square(numbers)

    repeat(5) {                 // 输出前五个
        println(squares.receive())
    }
    println("done")
    coroutineContext.cancelChildren()       // 取消子协程
}

fun CoroutineScope.numberFrom(start: Int): ReceiveChannel<Int> = produce {
    var x = start
    while (true) {
        send(x++)
    }
}
fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) {
        if (x % prime != 0) {
            send(x)
        }
    }
}
fun channelTest4() = runBlocking {
    var cur = numberFrom(2)
    while (true) {
        val prime = cur.receive()
        println(prime)
        cur = filter(cur, prime)
        if (prime > 100) {
            break
        }
    }
    println("done")
    coroutineContext.cancelChildren()
}

fun CoroutineScope.produceNumbersDelay() = produce {
    var x = 1
    while (true) {
        send(x++)
        delay(100)
    }
}
fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    // 注意我们如何使用 for 循环显式迭代通道以在 launchProcessor 代码中执行扇出。
    // 与 consumeEach 不同，这个 for 循环是安全完美地使用多个协程的。
    // 如果其中一个处理器协程执行失败，其它的处理器协程仍然会继续处理通道，
    // 而通过 consumeEach 编写的处理器始终在正常或非正常完成时消耗（取消）底层通道。
    for (msg in channel) {
        println("id: $id receive ${channel.receive()}")
    }
}
fun channelTest5() = runBlocking {
    val producer = produceNumbersDelay()
    repeat(5) {
        launchProcessor(it, producer)
    }
    delay(1000)
    producer.cancel()              // 取消生产者协程将关闭它的通道，从而最终终止处理器协程正在执行的此通道上的迭代
}

// 多个协程可以发送到同一个通道。
// 比如说，让我们创建一个字符串的通道，和一个在这个通道中以指定的延迟反复发送一个指定字符串的挂起函数
suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}
fun channelTest6() = runBlocking {
    val channel = Channel<String>()
    launch { sendString(channel, "foo", 200) }
    launch { sendString(channel, "abb", 500) }

    repeat(6) {
        println(channel.receive())
    }
    coroutineContext.cancelChildren()
}

// 上面展示的通道都是没有缓冲区的。无缓冲的通道在发送者和接收者相遇时传输元素（也称“对接”）。
// 如果发送先被调用，则它将被挂起直到接收被调用， 如果接收先被调用，它将被挂起直到发送被调用。
fun capacityTest() = runBlocking {
    // Channel() 工厂函数与 produce 建造器通过一个可选的参数 capacity 来指定 缓冲区大小 。缓冲允许发送者在被挂起前发送多个元素
    val channel = Channel<Int>(4)
    val sender = launch {
        repeat(10) {
            println("sending $it")
            channel.send(it)    // 将在缓冲区被占满时挂起
        }
    }
    // 没有接收到东西……只是等待……
    delay(1000)
    sender.cancel()
}

// 发送和接收操作是 公平的 并且尊重调用它们的多个协程。它们遵守先进先出原则，可以看到第一个协程调用 receive 并得到了元素
data class Ball(var hits: Int)
suspend fun player(name: String, table: Channel<Ball>) {
    for (ball in table) {
        ball.hits++
        println("name: $name, hits: $ball")
        delay(300)
        table.send(ball)
        // “乒”协程首先被启动，所以它首先接收到了球。
        // 甚至虽然“乒” 协程在将球发送会桌子以后立即开始接收，但是球还是被“乓” 协程接收了，因为它一直在等待着接收球
    }
}
fun channelTest7() = runBlocking {
    val table = Channel<Ball>()         // 一个共享的 channel
    launch { player("ping", table) }
    launch { player("pong", table) }
    table.send(Ball(0))
    delay(1000)
    coroutineContext.cancelChildren()

    // 有时候通道执行时由于执行者的性质而看起来不那么公平, https://github.com/Kotlin/kotlinx.coroutines/issues/111
}

// 计时器通道是一种特别的会合通道，每次经过特定的延迟都会从该通道进行消费并产生 Unit。
// 虽然它看起来似乎没用，它被用来构建分段来创建复杂的基于时间的 produce 管道和进行窗口化操作以及其它时间相关的处理。
// 可以在 select 中使用计时器通道来进行“打勾”操作。
fun channelTest8() = runBlocking {
    val tickerChannel = ticker(100, 0)
    var nextElement = withTimeoutOrNull(1) {
        tickerChannel.receive()
    }
    println("Initial element is available immediately: $nextElement")

    nextElement = withTimeoutOrNull(50) {
        tickerChannel.receive()
    }
    println("Next element is not ready in 50 ms: $nextElement")

    nextElement = withTimeoutOrNull(60) {
        tickerChannel.receive()
    }
    println("Next element is ready in 100 ms: $nextElement")

    // 模拟大量消费延迟
    println("Consumer pauses for 150ms")
    delay(150)

    // 下一个元素立即可用
    nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Next element is available immediately after large consumer delay: $nextElement")

    // 请注意，`receive` 调用之间的暂停被考虑在内，下一个元素的到达速度更快
    nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
    println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

    tickerChannel.cancel()
}
// 请注意，ticker 知道可能的消费者暂停，并且默认情况下会调整下一个生成的元素如果发生暂停则延迟，试图保持固定的生成元素率。
// 给可选的 mode 参数传入 TickerMode.FIXED_DELAY 可以保持固定元素之间的延迟。