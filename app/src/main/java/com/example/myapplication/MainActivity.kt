package com.example.myapplication

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    var a = 0
    var b = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        main11()
//        exceptionConcurrent()
//        fixBugException()
        main17()
    }

    private fun main() = runBlocking {
        val job = launch {
            repeat(1000) { i ->
                Log.e(TAG, "job: I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1300L) // delay a bit
        Log.e(TAG, "main: I'm tired of waiting!")
        Log.e(TAG, "check isActive: ${job.isActive} - isCompleted: ${job.isCompleted} - isCancelled: ${job.isCancelled}")
        job.cancel() // cancels the job
        Log.e(TAG, "check isActive: ${job.isActive} - isCompleted: ${job.isCompleted} - isCancelled: ${job.isCancelled}")
        job.join() // waits for job's completion
        Log.e(TAG, "main: Now I can quit.")
    }

    fun main2() = runBlocking {
        val time = measureTimeMillis {
            Log.e(TAG, "thoi gian hien tai 1: ${System.currentTimeMillis()}")
            val one = async {
                Log.e(TAG, "thoi gian hien tai 2: ${System.currentTimeMillis()}")
                printOne()
                Log.e(TAG, "thoi gian hien tai 3: ${System.currentTimeMillis()}")
            }
            val two = async {
                Log.e(TAG, "thoi gian hien tai 4: ${System.currentTimeMillis()}")
                printTwo()
                Log.e(TAG, "thoi gian hien tai 5: ${System.currentTimeMillis()}")
            }
            Log.e(TAG, "thoi gian hien tai 6: ${System.currentTimeMillis()}")
            Log.e(TAG, "The answer is ${one.await() + two.await()}")
            Log.e(TAG, "thoi gian hien tai 7: ${System.currentTimeMillis()}")
            val three = async {
                Log.e(TAG, "thoi gian hien tai 8: ${System.currentTimeMillis()}")
                countNumber()
                Log.e(TAG, "thoi gian hien tai 9: ${System.currentTimeMillis()}")
            }
            Log.e(TAG, "thoi gian hien tai 10: ${System.currentTimeMillis()}")
            Log.e(TAG, "The answer 2 is ${three.await()}")
            Log.e(TAG, "thoi gian hien tai 11: ${System.currentTimeMillis()}")
        }
        Log.e(TAG, "Completed in $time ms")
    }

    suspend fun printOne(): Int {
        delay(1000L)
        a = 10
        return 10
    }

    suspend fun printTwo(): Int {
        delay(1000L)
        b = 20
        return 20
    }

    suspend fun countNumber(): Int {
        delay(1000L)
        return (a + b)
    }

    // Điều phối và bối cảnh Coroutine
    fun main3() {
        newSingleThreadContext("Ctx1").use { ctx1 ->
            Log.e(TAG, "[${Thread.currentThread().name}] - 1")
            newSingleThreadContext("Ctx2").use { ctx2 ->
                Log.e(TAG, "[${Thread.currentThread().name}] - 2")
                runBlocking(ctx1) {
                    Log.e(TAG, "[${Thread.currentThread().name}] - 3")
                    withContext(ctx2) {
                        Log.e(TAG, "[${Thread.currentThread().name}] - 4")
                    }
                    Log.e(TAG, "[${Thread.currentThread().name}] - 5")
                }
            }
            Log.e(TAG, "[${Thread.currentThread().name}] - 6")
        }
    }

    // Đồng thời có cấu trúc
    fun main4() = runBlocking {
        doWorld()
        Log.e(TAG,"Done")
    }

    // Concurrently executes both sections
    suspend fun doWorld() = coroutineScope { // this: CoroutineScope
        launch {
            delay(2000L)
            Log.e(TAG,"World 2")
        }
        launch {
            delay(1000L)
            Log.e(TAG,"World 1")
        }
        Log.e(TAG,"Hello")
    }

    // Đồng thời có cấu trúc
    fun main5() = runBlocking {
        doWorld2()
        Log.e(TAG, "Done")
    }

    suspend fun doWorld2() = coroutineScope { // this: CoroutineScope
        val job = launch {
            delay(2000L)
            Log.e(TAG, "World 2")
            val job2 = launch {
                delay(1000L)
                Log.e(TAG, "World 1")
            }
            val job3 = launch {
                delay(3000L)
                Log.e(TAG, "World 3")
            }.invokeOnCompletion {  }
        }
        Log.e(TAG, "Hello")
    }

    fun main6() = runBlocking {
        val result = withTimeoutOrNull(1300L) {
            repeat(3) { i ->
                Log.e(TAG, "I'm sleeping $i ...")
                delay(500L)
            }
            Log.e(TAG, "Done") // will get cancelled before it produces this result
        }
        Log.e(TAG, "Result is $result")
    }

    fun main7() = runBlocking {
        GlobalScope.launch { // root coroutine with launch
            try {
                Log.e(TAG, "Throwing exception from launch")
                throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
            } catch (e: Exception) {
                Log.e(TAG, "Caught IndexOutOfBoundsException")
            }
        }
        val deferred = GlobalScope.async { // root coroutine with async
            Log.e(TAG, "Throwing exception from async")
            throw ArithmeticException() // Nothing is printed, relying on user to call await
        }
        try {
            deferred.await()
            Log.e(TAG, "Unreached")
        } catch (e: ArithmeticException) {
            Log.e(TAG, "Caught ArithmeticException")
        }
    }

    fun main8() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG,"CoroutineExceptionHandler got $exception")
        }
        val job = GlobalScope.launch(handler) { // root coroutine, running in GlobalScope
            throw AssertionError()
        }
        val deferred = GlobalScope.async(handler) { // also root, but async instead of launch
            throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
        }
        joinAll(job, deferred)
    }

    fun main9() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG,"CoroutineExceptionHandler got $exception")
        }
        val job = GlobalScope.launch(handler) {
            launch { // the first child
                try {
                    delay(1000L)
                    Log.e(TAG,"first child is running")
                } finally {
                    Log.e(TAG,"first child throws an exception")
                    throw IOException()
                }
            }
            launch { // the second child
                delay(10)
                Log.e(TAG,"Second child throws an exception")
                throw ArithmeticException()
            }
        }
        job.join()
        Log.e(TAG,"Done")
    }

    fun main10() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG,"Caught $exception")
        }
        supervisorScope {
            val first = launch(handler) {
                Log.e(TAG,"Child throws an exception")
                throw AssertionError()
            }
            val second = launch {
                delay(100)
                Log.e(TAG,"Scope is completing")
            }
        }
        Log.e(TAG,"Scope is completed")
    }

    fun main11() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG,"Caught $exception")
        }
        val supervisor = SupervisorJob()
        val parent = CoroutineScope(handler).launch {
            val first = launch(supervisor) {
                Log.e(TAG,"Child throws an exception")
                throw AssertionError()
            }
            val second = launch {
                delay(100)
                Log.e(TAG,"Scope is completing")
            }
        }
        parent.join()
        Log.e(TAG,"Scope is completed")
    }

    // Exception ConcurrentModificationException
    var instanceArrayList = ArrayList<String>()
    val synclist = CopyOnWriteArrayList(instanceArrayList)

    fun exceptionConcurrent() {

        var arrayList = ArrayList<String>()

        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "Caught $exception")
        }

        val supervisor = SupervisorJob()

        CoroutineScope(Dispatchers.Default).launch {
            launch(handler + supervisor) {
                arrayList.add("so 1")
            }
            launch(handler + supervisor) {
                arrayList.add("so 2")
            }
            launch {
                repeat(100) {
                    arrayList.add("so $it")
                }
            }
            launch {
                for (item in arrayList) {
                    Log.e(TAG, item)
                }
            }
            launch {
                arrayList.removeAt(2)
            }
        }
    }

    // Fix bug Exception ConcurrentModificationException
    fun add(string: String) {
        synchronized(synclist) {
            synclist.add(string)
        }
    }

    fun remove(index: Int) {
        synchronized(synclist) {
            synclist.removeAt(index)
        }
    }

    fun logArray() {
        synchronized(synclist) {
            for (item in synclist) {
                Log.e(TAG, item)
            }
        }
    }

    fun fixBugException() {
        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "Caught $exception")
        }

        val supervisor = SupervisorJob()

        CoroutineScope(Dispatchers.Default).launch {
            launch(handler + supervisor) {
                add("so 1")
            }
            launch(handler + supervisor) {
                add("so 2")
            }
            launch {
                repeat(100) {
                    add("so $it")
                }
            }
            launch {
                delay(100L)
                logArray()
            }
            launch {
                remove(2)
            }
        }
    }

    // flow example
    // take
    fun numbers(): Flow<Int> = flow {
        try {
            emit(1)
            emit(2)
            Log.e(TAG,"This line will not execute")
            emit(3)
        } catch (e: CancellationException) {
            Log.e(TAG,"exception")
        } finally {
            Log.e(TAG,"close resource here")
        }
    }

    fun main14() = runBlocking {
        numbers()
            .take(3) // take only the first two
            .collect { value ->
                Log.e(TAG,"collect: $value")
            }
    }

    // transform
    fun main15() = runBlocking {
        (1..9).asFlow() // a flow of requests
            .transform { value ->
                if (value % 2 == 0) { // Emit only even values, but twice
                    emit(value * value)
                    emit(value * value * value)
                } // Do nothing if odd
            }
            .collect { response -> Log.e(TAG,"$response") }
    }

    fun simple(): Flow<Int> = flow {
        for (i in 1..3) {
            delay(100) // pretend we are asynchronously waiting 100 ms
            emit(i) // emit next value
            Log.e(TAG,"emit: $i")
        }
    }

    fun main16() = runBlocking<Unit> {
        val time = measureTimeMillis {
            simple()
                .conflate()
                .collectLatest{ value ->
                delay(300) // pretend we are processing it for 300 ms
                println(value)
                Log.e(TAG,"collect: $value")
            }
        }
        Log.e(TAG,"Collected in $time ms")
    }

    fun main17() = runBlocking<Unit> {
        val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
        val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
        val startTime = System.currentTimeMillis() // remember the start time
        nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
            .collect { value -> // collect and print
                Log.e(TAG,"$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }

}