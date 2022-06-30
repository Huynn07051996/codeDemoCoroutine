package com.example.myapplication

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    var a = 0
    var b = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main3()
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
        job.cancel() // cancels the job
        job.join() // waits for job's completion
        Log.e(TAG, "main: Now I can quit.")
    }




    fun main2() = runBlocking {
        val time = measureTimeMillis {
            Log.e(TAG, "thoi gian hien tai 1: ${System.currentTimeMillis()}")
            val one = async {
                Log.e(TAG, "thoi gian hien tai 3: ${System.currentTimeMillis()}")
                printOne()
                Log.e(TAG, "thoi gian hien tai 5: ${System.currentTimeMillis()}")
            }
            val two = async {
                Log.e(TAG, "thoi gian hien tai 4: ${System.currentTimeMillis()}")
                printTwo()
                Log.e(TAG, "thoi gian hien tai 6: ${System.currentTimeMillis()}")
            }
            Log.e(TAG, "thoi gian hien tai 2: ${System.currentTimeMillis()}")
            Log.e(TAG, "The answer is ${one.await() + two.await()}")
            Log.e(TAG, "thoi gian hien tai 7: ${System.currentTimeMillis()}")
            val three = async {
                Log.e(TAG, "thoi gian hien tai 9: ${System.currentTimeMillis()}")
                countNumber()
                Log.e(TAG, "thoi gian hien tai 10: ${System.currentTimeMillis()}")
            }
            Log.e(TAG, "thoi gian hien tai 8: ${System.currentTimeMillis()}")
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
            newSingleThreadContext("Ctx2").use { ctx2 ->
                runBlocking(ctx1) {
                    Log.e(TAG,"[${Thread.currentThread().name}] - Started in ctx1")
                    withContext(ctx2) {
                        Log.e(TAG,"[${Thread.currentThread().name}] - Working in ctx2")
                    }
                    Log.e(TAG,"[${Thread.currentThread().name}] - Back to ctx1")
                }
            }
        }
    }

    // Đồng thời có cấu trúc
    fun main4() = runBlocking {
        doWorld()
        println("Done")
    }

    // Concurrently executes both sections
    suspend fun doWorld() = coroutineScope { // this: CoroutineScope
        launch {
            delay(2000L)
            println("World 2")
        }
        launch {
            delay(1000L)
            println("World 1")
        }
        println("Hello")
    }
}