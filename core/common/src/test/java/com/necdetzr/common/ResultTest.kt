package com.necdetzr.common

import app.cash.turbine.test
import com.necdetzr.common.result.Result
import com.necdetzr.common.result.asResult
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ResultTest {
    @Test
    fun asResult_emits_Loading_then_Success() = runTest {
        val flow = flowOf("Macbook Pro BLE", "Apple Watch 9")
        flow.asResult().test {
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Success("Macbook Pro BLE"), awaitItem())
            assertEquals(Result.Success("Apple Watch 9"), awaitItem())

            awaitComplete()
        }
    }

    @Test
    fun  asResult_catches_exception_and_throws_error() = runTest {
        val testException = Exception("Test Exception")
        val flow = flow<String> {
            throw testException
        }
        flow.asResult().test {
            assertEquals(Result.Loading, awaitItem())
            val errorItem = awaitItem()
            assertTrue(errorItem is Result.Error)
            assertEquals(testException.message, errorItem.exception.message)
            awaitComplete()
        }
    }
}
