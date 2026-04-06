package com.example.tgcardistributedmltracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.media.Image
import android.renderscript.* // For Android 12+ this won't work. Will need to perform manual plane conversion.
import android.util.Log

class YuvToRgbConverter(context: Context) {
    private val rs = RenderScript.create(context)
    private val yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
    // Yo, moved these here to the initialization block so I can DESTROY THEM AHAHAHAH OPE jk but
    // really no we needed to call destroy() because we weren't doing that before because renderscript
    // uses internal C++ Native drivers and like yeah since this function runs about 50-60x's a minute
    // it is causing massive overhead which might have been causing our frame rate issues but still those
    // could be just because of all the processing occurring on the CPU.
    private var inAllocation: Allocation? = null
    private var outAllocation: Allocation? = null
    private var yuvBuffer: ByteArray? = null // Needed to create this because ARCore uses Direct ByteBuffers
    // (Native Memory) for speed which cause Android to throw UnsupportedOperationException since it doesn't
    // have a Java array to point to.

    fun yuvToRgb(image: Image, bitmap: Bitmap) {
        val width = image.width
        val height = image.height
        // Only create new allocations at the start or if the size changes. Since our images sizes will
        // be consistent then we onlly ever be creating them once saving us a lot of overhead.
        if (inAllocation == null || inAllocation!!.type.x != image.width) {
            inAllocation?.destroy() // Clean up old one if size changed
            outAllocation?.destroy()

            val yuvType = Type.Builder(rs, Element.U8(rs))
                .setX(width)
                .setY(height + height/2)
                .setYuvFormat(ImageFormat.NV21).create()
//        val yuvType = Type.Builder(rs, Element.U8(rs)).setX(image.width).setY(image.height)
//            .setYuvFormat(ImageFormat.NV21).create()
            inAllocation = Allocation.createTyped(rs, yuvType, Allocation.USAGE_SCRIPT)

            val rgbaType =
                Type.Builder(rs, Element.RGBA_8888(rs)).setX(image.width).setY(image.height).create()
            outAllocation = Allocation.createTyped(rs, rgbaType, Allocation.USAGE_SCRIPT)

            // Re-allocate the temporary byte array
            yuvBuffer = ByteArray(image.width * (image.height+height/2))
        }
        // Alright here we are now done with the creation of the in's and out's variable so they will
        // be reused.
        // NV21 is the standard format for ARCore camera images
        // 2. SAFE DATA EXTRACTION
        // We cannot use .array(). We must manually pull the bytes from the direct buffer.
        try {
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            // Fill our ByteArray safely
            yuvBuffer?.let {
                // 1. Copy the Y plane (Luminance)
                yBuffer.get(it, 0, ySize)

                // 2. Interleave V and U planes (Chroma)
                // NV21 format is YYYYYYYY VUVU...
                // We start copying color data right after the Y data
                vBuffer.get(it, ySize, vSize)

                // If the device's camera uses a pixel stride of 2 (common in ARCore),
                // the V buffer actually already contains the U data interleaved.
                // If not, we'd manually interleave them here using uSize.

                inAllocation?.copyFrom(it)
            }

            // 3. RUN RENDERSCRIPT
            yuvToRgbIntrinsic.setInput(inAllocation)
            yuvToRgbIntrinsic.forEach(outAllocation)
            outAllocation?.copyTo(bitmap)

        } catch (e: Exception) {
            Log.e("YuvConverter", "Failed to copy buffers safely", e)
            throw e
        }



//        inAllocation?.copyFrom(image.planes[0].buffer.array()) // This is simplified
//
//        yuvToRgbIntrinsic.setInput(inAllocation)
//        yuvToRgbIntrinsic.forEach(outAllocation)
//        outAllocation?.copyTo(bitmap)
    }
    // New function destroy() to get rid of those memory leaks!!!
    // Saving us tons of memory and improving performance!!
    // This will be called when the activity is destroyed!
    fun destroy() {
        inAllocation?.destroy()
        outAllocation?.destroy()
        yuvToRgbIntrinsic.destroy()
        rs.destroy()
    }
}