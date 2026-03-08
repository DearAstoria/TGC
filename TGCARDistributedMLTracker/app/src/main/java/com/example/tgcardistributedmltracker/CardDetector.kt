package com.example.tgcardistributedmltracker

import android.content.Context
import android.graphics.Bitmap
// Core TFLite Interpreter imports
import org.tensorflow.lite.Interpreter
//import org.tensorflow.lite.GpuDelegate This will take a few steps

// Support Library import for loading the model file
import org.tensorflow.lite.support.common.FileUtil


class CardDetector(context: Context) {
    private val interpreter: Interpreter
    private val modelInputSize = 640 // Based off training imgsz

    init {
    val options = Interpreter.Options().apply {
        setNumThreads(4)
        // GPU acceleration:
//         addDelegate(GpuDelegate())
    }
    val tfliteModel = FileUtil.loadMappedFile(context, "best_int8.tflite")
        interpreter = Interpreter(tfliteModel, options)
    }

    fun detectCard(bitmap: Bitmap): List<DetectionResult> {
        // 1. Pre-process: Resize Bitmap to 640x640
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true)

        // 2. Convert to ByteBuffer (Standardize pixel values 0-255 for INT8)
        val inputBuffer = convertBitmapToBuffer(resizedBitmap)

        // 3. Define Output: YOLOv8n output is [1, 9, 8400]
        // (4 box coords + 5 class scores)
        val output = Array(1) { Array(9) { FloatArray(8400) } }

        interpreter.run(inputBuffer, output)

        // 4. Post-process (NMS and scaling) logic goes here
        return processOutput(output)
    }


}