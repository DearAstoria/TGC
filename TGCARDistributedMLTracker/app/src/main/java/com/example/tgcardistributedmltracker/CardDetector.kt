package com.example.tgcardistributedmltracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import java.nio.ByteBuffer
import java.nio.ByteOrder
//import com.google.ai.edge.litert.Interpreter
//import com.google.ai.edge.litert.support.common.FileUtil
// GPU for later maybe
// import com.google.ai.edge.litert.gpu.GpuDelegate
//vvTHESE ARE OUTDATED AS GOOGLE IS REBRANDING TENSORFLOWLITE TO LITERT (LITE RUNTIME)
//VVTHEY ARE DOING THIS TO BETTER REFLECT ITS ROLE AS HIGH-PERFORMANCE RUNTIME FOR EDGE AI.
// Core TFLite Interpreter imports
import org.tensorflow.lite.Interpreter
//import org.tensorflow.lite.GpuDelegate This will take a few steps
// Support Library import for loading the model file
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage

data class DetectionResult(
    val classIndex: Int,       // Which card it is (0=Bulb_Wiz, etc.)
    val className: String,    // Card class names from data.yaml
    val confidence: Float,    // How sure the AI is (0.0 to 1.0)
    val boundingBox: RectF    // The [left, top, right, bottom] coordinates on screen
)

private fun newConvertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
    // 640 * 640 * 3 channels * 4 bytes per Float = 4,915,200 bytes
    val imgData = ByteBuffer.allocateDirect(1 * 640 * 640 * 3 * 4)
    imgData.order(ByteOrder.nativeOrder())

    val intValues = IntArray(640 * 640)
    bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    imgData.rewind()
    val floatBuffer = imgData.asFloatBuffer()

    for (pixelValue in intValues) {
        // Extract RGB and normalize to [0, 1]
        // We divide by 255.0f because Float32 models don't want 0-255 bytes,
        // they want normalized percentages.
        floatBuffer.put(((pixelValue shr 16) and 0xFF) / 255.0f) // Red
        floatBuffer.put(((pixelValue shr 8) and 0xFF) / 255.0f)  // Green
        floatBuffer.put((pixelValue and 0xFF) / 255.0f)         // Blue
    }
    return imgData
}

private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
    // 640 * 640 * 3 channels (R, G, B).
    // We use 1 byte per channel because this is an INT8 model.
    val imgData = ByteBuffer.allocateDirect(1 * 640 * 640 * 3)
    imgData.order(ByteOrder.nativeOrder())

    val intValues = IntArray(640 * 640)
    bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    imgData.rewind()
    for (pixelValue in intValues) {
        // Extract RGB and pack them into the buffer
        // In INT8 models, we usually don't need to normalize to 0.0-1.0
        imgData.put(((pixelValue shr 16) and 0xFF).toByte()) // Red
        imgData.put(((pixelValue shr 8) and 0xFF).toByte())  // Green
        imgData.put((pixelValue and 0xFF).toByte())         // Blue
    }
    return imgData
}

private fun processOutput(output: Array<Array<FloatArray>>): List<DetectionResult> {
    val results = mutableListOf<DetectionResult>()
    val classNames = listOf("Bulb_Wiz", "Char", "Grim", "Mage")

    for (i in 0 until 8400) {
        // Find the class with the highest confidence score for this point
        var maxConfidence = 0.0f
        var detectedClassIndex = -1

        // Classes start at index 4 in the '9' dimension
        for (c in 0 until 5) {
            val confidence = output[0][c + 4][i]
            if (confidence > maxConfidence) {
                maxConfidence = confidence
                detectedClassIndex = c
            }
        }

        // Only keep detections we are sure about (Threshold = 50%)
        if (maxConfidence > 0.5f) {
            val x = output[0][0][i]
            val y = output[0][1][i]
            val w = output[0][2][i]
            val h = output[0][3][i]

            results.add(DetectionResult(
                classIndex = detectedClassIndex,
                className = classNames[detectedClassIndex],
                confidence = maxConfidence,
                boundingBox = RectF(x - w/2, y - h/2, x + w/2, y + h/2)
            ))
        }
    }
    return results
}

// Function used in order for there not to be multiple bounding boxes placed for the same card,
// that the model may re-spot at different angles.
private fun nms(detections: List<DetectionResult>): List<DetectionResult> {
    if (detections.isEmpty()) return emptyList()

    // 1. Sort detections by confidence (highest first)
    val sortedDetections = detections.sortedByDescending { it.confidence }.toMutableList()
    val selectedDetections = mutableListOf<DetectionResult>()

    while (sortedDetections.isNotEmpty()) {
        val current = sortedDetections.removeAt(0)
        selectedDetections.add(current)

        // 2. Remove any other boxes that overlap significantly with the current one
        val iterator = sortedDetections.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (calculateIoU(current.boundingBox, next.boundingBox) > 0.45f) { // 0.45 is a standard threshold
                iterator.remove()
            }
        }
    }
    return selectedDetections
}

// Helper to calculate overlap percentage
private fun calculateIoU(rect1: RectF, rect2: RectF): Float {
    val intersection = RectF()
    if (!intersection.setIntersect(rect1, rect2)) return 0f

    val intersectionArea = intersection.width() * intersection.height()
    val unionArea = (rect1.width() * rect1.height()) +
            (rect2.width() * rect2.height()) - intersectionArea

    return intersectionArea / unionArea
}

class CardDetector(context: Context) {
    private val interpreter: Interpreter
    private val modelInputSize = 640 // Based off training images.

    init {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            // GPU acceleration:
//          addDelegate(GpuDelegate())
        }
        val tfliteModel = FileUtil.loadMappedFile(context, "best_int8.tflite")
        interpreter = Interpreter(tfliteModel, options)
    }

    fun detectCard(bitmap: Bitmap): List<DetectionResult> {

        // 1. Pre-process: Resize Bitmap to 640x640
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, modelInputSize, modelInputSize, true)

        // Converting the 1.2MB Bitmap into the 4.9MB Float32 buffer that the model needs.
        val inputDataType = interpreter.getInputTensor(0).dataType() // Check if it's FLOAT32
        val inputShape = interpreter.getInputTensor(0).shape() // Should be [1, 480, 640, 4] or [1, 640, 480, 4]
        Log.i("INPUTDATATYPE: ", "${inputDataType}\n")
        Log.i("INPUTSHAPE: ", "${inputShape}\n")
        // Using the TFLite Support Library to auto-convert
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(resizedBitmap)

        // 2. Convert to ByteBuffer
        // Normalizing the pixel values into a range of 0.0f - 1.0f that the Float32 model requires.
        val inputBuffer = newConvertBitmapToBuffer(resizedBitmap)

        // 3. Define Output: YOLOv8n output is [1, 9, 8400]
        // (4 box coords + 5 class scores)
        val output = Array(1) { Array(9) { FloatArray(8400) } }

        interpreter.run(inputBuffer, output)

        // 4. Post-process (NMS and scaling) logic
        val rawResults = processOutput(output)
        return nms(rawResults) // Polished list (no multiple boxes, this also stops extra
        // pings from being sent to the server, I.E. redundant pings about detecting the same card
        // from a different angle.
    }


}