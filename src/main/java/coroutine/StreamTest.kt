package coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Exception
import kotlin.system.measureTimeMillis

fun main() {
//    seqTest().forEach { println(it) }

//    runBlocking {
//        suList().forEach { println(it) }
//    }

//    runBlocking {
//        launch {
//            for (k in 1..3) {
//                println("I'm not blocked $k")
//                delay(100)
//            }
//        }
//        flowTest().collect { println(it) }
//    }

//    timeoutFlow()

//    flowTest2()
//    flowTest3()

//    flowTest4()
//    flowTest5()
//    flowTest6()
//    flowTest7()
//    flowTest8()
//    flowTest9()

//    flowTest15()
//    flowTest16()
//    flowTest17()
//    flowTest18()
    flowTest19()
}

fun seqTest() = sequence {
    for (i in 1..3) {
        Thread.sleep(100)
        yield(i)
    }
}

suspend fun suList(): List<Int> {
    delay(1000)
    return listOf(1, 2, 3)
}

fun flowTest() = flow {
    log("Started simple flow")
    for (i in 1..3) {
        delay(200)
        emit(i)
    }
}

fun timeoutFlow() = runBlocking {
    withTimeoutOrNull(500) {
        flowTest().collect { println(it) }
    }
    println("done")
}

fun flowTest2() = runBlocking {
    (1..10).asFlow()
            .map { it * 2 }
            .filter { it <= 16 }
            .transform {
                emit("hhh")
                emit(it)
            }
            .take(3)
            .collect { println(it) }
}

fun flowTest3() = runBlocking {
    flowTest().collect { log("collected value: $it") }
}

fun errorFlow() = flow {
    withContext(Dispatchers.Default) {          // 在流构建器中更改消耗 CPU 代码的上下文的错误方式，会抛异常
        (1..5).forEach {
            emit(it)
        }
    }
}

fun contextFlow() = flow {
    (1..5).forEach {
        Thread.sleep(200)
        log("emitting $it")
        emit(it)
    }
}.flowOn(Dispatchers.Default)       // 在流构建器中改变消耗 CPU 代码上下文的正确方式

fun flowTest4() = runBlocking {
    contextFlow().collect { log("value: $it") }
}

fun costFlow() = flow {
    (1..5).forEach {
        log("emit $it")
        delay(200)
        emit(it)
    }
}

// 从收集流所花费的时间来看，将流的不同部分运行在不同的协程中将会很有帮助，特别是当涉及到长时间运行的异步操作时。
fun flowTest5() = runBlocking {
    val time = measureTimeMillis {
        costFlow()
                .buffer()           // 可以在流上使用 buffer 操作符来并发运行这个 simple 流中发射元素的代码以及收集的代码， 而不是顺序运行它们
                .collect {
                    delay(300)
                    println(it)
                }
    }
    log("cost time: $time")
}

fun flowTest6() = runBlocking {
    val time = measureTimeMillis {
        costFlow().conflate()           // 合并发射项，不对每个值进行处理
                .collect {
                    delay(500)
                    println(it)
                }
    }
    log("cost time $time")
}

fun flowTest7() = runBlocking {
    val time = measureTimeMillis {
        costFlow().collectLatest {
            println("collect start")
            delay(300)
            println(it)
            println("collect end")
        }
    }
    println("cost time $time")
}

fun flowTest8() = runBlocking {
    val nums = (1..5).asFlow()
    val strs = flowOf("one", "two", "three", "four", "five")

    nums.zip(strs) { n, s -> "$n -> $s" }
            .collect { println(it) }
}

fun flowTest9() = runBlocking {
    val nums = (1..5).asFlow().onEach { delay(300) }
    val strs = flowOf("one", "two", "three", "four", "five").onEach { delay(400) }

    val startTime = System.currentTimeMillis()
//    nums.zip(strs) { n, s -> "$n -> $s" }
//            .collect { println("value at ${System.currentTimeMillis() - startTime} is: $it") }

    nums.combine(strs) { a, b -> "$a -> $b" }
            .collect { println("value at ${System.currentTimeMillis() - startTime} is: $it") }
}

fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: first")
    delay(500)
    emit("$i: second")
}

fun flowTest10() = runBlocking {
    val start = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100) }
            .flatMapConcat { requestFlow(it) }
            .collect {
                println("$it at ${System.currentTimeMillis() - start} ms from start")
            }
}

fun flowTest11() = runBlocking {
    val start = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100) }
            .flatMapMerge { requestFlow(it) }       // flatMapMerge 会顺序调用代码块（本示例中的 { requestFlow(it) }），但是并发收集结果流
            .collect {
                println("$it at ${System.currentTimeMillis() - start} ms from start")
            }
}

fun flowTest12() = runBlocking {
    val start = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100) }
            .flatMapLatest { requestFlow(it) }
            .collect {
                println("$it at ${System.currentTimeMillis() - start} ms from start")
            }
}

fun sampleFlow() = flow {
    (1..3).forEach {
        println("emitting $it")
        emit(it)
    }
}

fun flowTest13() = runBlocking {
    try {
        sampleFlow().collect {
            println(it)
            check(it <= 1)
        }
    } catch (e: Exception) {
        println(e)
    }
}

fun sampleFlow1() = flow {
    (1..3).forEach {
        println("emitting $it")
        emit(it)
    }
}.map {
    check(it <= 1) { "crash on $it" }
    "string $it"
}

fun flowTest14() = runBlocking {
    try {
        sampleFlow1().collect {
            println(it)
        }
    } catch (e: Exception) {
        println(e)
    }
}

fun flowTest15() = runBlocking {
    sampleFlow1().catch { e -> emit("catch $e") }
            .collect {
                println(it)
            }
}

fun flowTest16() = runBlocking {
    (1..3).asFlow()
            .onEach {
                check(it <= 1)
                println("value $it")
            }
            .catch { e -> println("catch $e") }         // 捕获上游异常
            .collect()
}

fun flowTest17() = runBlocking {
    try {
        sampleFlow()
                .collect { println(it) }
    } finally {             // 命令式完成
        println("end")
    }

    sampleFlow().onCompletion {     // 声明式完成
        println("end")
    }.collect {
        println(it)
    }

    sampleFlow1()
            .onCompletion { cause ->        //  onCompletion 能观察到所有异常并且仅在上游流成功完成（没有取消或失败）的情况下接收一个 null 异常。
                if (cause != null)
                    println("Flow completed exceptionally")
            }
            .catch { println("catch $it") }
            .collect { println(it) }
}

fun events() = (1..5).asFlow().onEach { delay(100) }

fun flowTest18() = runBlocking {
    /*events().onEach { println("event $it") }
            .collect()
    println("done")*/

    events().onEach { println("event $it") }
            .launchIn(this)     // <--- 在单独的协程中执行流
    println("done")
}

fun flowTest19() = runBlocking {
    /*events().collect {
        if (it == 3) {
            cancel()        // 取消流
        }
        println(it)
    }*/

    /*(1..5).asFlow().collect {       // 繁忙循环， 并且没有在任何地方暂停，那么就没有取消的检测
        if (it == 3) {
            cancel()
        }
        println(it)
    }*/

    /*(1..5).asFlow().onEach {
        ensureActive()      // 繁忙循环，必须明确检测是否取消，添加ensureActive()
    }.collect {
        if (it == 3) {
            cancel()
        }
        println(it)
    }*/

    (1..5).asFlow().cancellable().collect {
        if (it == 3) {
            cancel()
        }
        println(it)
    }
}