package com.example.tgcardistributedmltracker

import android.app.Application

class ARDistributedMLTrackerApplication : Application() {
    override fun onCreate() {
        // THIS RUNS BEFORE ACTIVITIES
        System.setProperty("io.github.sceneview.render_backend", "opengl")
        System.setProperty("filament_backend", "opengl")
        super.onCreate()
    }
}