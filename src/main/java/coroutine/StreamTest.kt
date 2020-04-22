package coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
//    seqTest().forEach { println(it) }

//    runBlocking {
//        suList().forEach { println(it) }
//    }

    runBlocking {
        launch {
            for (k in 1..3) {
                println("I'm not blocked $k")
                delay(100)
            }
        }
        flowTest().collect { println(it) }
    }
}

fun seqTest() = sequence<Int> {
    for (i in 1..3) {
        Thread.sleep(100)
        yield(i)
    }
}

suspend fun suList(): List<Int> {
    delay(1000)
    return listOf(1, 2, 3)
}

fun flowTest() = flow<Int> {
    for (i in 1..3) {
        delay(200)
        emit(i)
    }
}