package com.example.tmdbmovies.app

import android.app.Application
import com.example.tmdbmovies.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TmdbMoviesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TmdbMoviesApplication)
            modules(appModule)
        }
    }
}
