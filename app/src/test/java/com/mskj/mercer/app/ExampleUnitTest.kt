package com.mskj.mercer.app

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @FlowPreview
    @Test
    fun addition_isCorrect() = runBlocking {
        flowOf(1)
            .flatMapConcat {
                flow {
                    for (i in 0..5) {
                        delay(100)
                        emit(i)
                    }
                }
            }
            .collect {
                println("it : $it")
            }
        // assertEquals(4, 2 + 2)
        Unit
    }
}