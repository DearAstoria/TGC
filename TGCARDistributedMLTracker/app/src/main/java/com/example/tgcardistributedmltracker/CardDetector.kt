package com.example.tgcardistributedmltracker

import android.content.Context
// Core TFLite Interpreter imports
import org.tensorflow.lite.Interpreter
//import org.tensorflow.lite.GpuDelegate This will take a few steps

// Support Library import for loading the model file
import org.tensorflow.lite.support.common.FileUtil


class CardDetector(context: Context) {
    val options = Interpreter.Options().apply {
        setNumThreads(4)
        // GPU acceleration:
//         addDelegate(GpuDelegate())
    }
    private val tfliteModel = FileUtil.loadMappedFile(context, "best_int8.tflite")

    val interpreter = Interpreter(tfliteModel, options)

}