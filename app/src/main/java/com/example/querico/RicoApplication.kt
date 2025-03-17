package com.example.querico

import android.app.Application
import android.content.Context
import com.example.querico.Model.ModelRoom.AppDB
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class RicoApplication : Application() {
    companion object {
        private const val THREAD_AMOUNT = 4
        private val executorService: ExecutorService = Executors.newFixedThreadPool(THREAD_AMOUNT)
        private lateinit var instance: RicoApplication;

        fun getExecutorService(): ExecutorService {
            return this.executorService;
        }

        fun getInstance(): Context {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }
}