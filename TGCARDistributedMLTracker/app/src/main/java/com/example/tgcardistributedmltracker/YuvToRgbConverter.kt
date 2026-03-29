package com.example.tgcardistributedmltracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.media.Image
import android.renderscript.* // For Android 12+ this won't work. Will need to perform manual plane conversion.

class YuvToRgbConverter(context: Context) {
    private val rs = RenderScript.create(context)
    private val yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

    fun yuvToRgb(image: Image, bitmap: Bitmap) {
        val yuvType = Type.Builder(rs, Element.U8(rs)).setX(image.width).setY(image.height)
            .setYuvFormat(ImageFormat.NV21).create()
        val inAllocation = Allocation.createTyped(rs, yuvType, Allocation.USAGE_SCRIPT)

        val rgbaType =
            Type.Builder(rs, Element.RGBA_8888(rs)).setX(image.width).setY(image.height).create()
        val outAllocation = Allocation.createTyped(rs, rgbaType, Allocation.USAGE_SCRIPT)

        // NV21 is the standard format for ARCore camera images
        inAllocation.copyFrom(image.planes[0].buffer.array()) // This is simplified

        yuvToRgbIntrinsic.setInput(inAllocation)
        yuvToRgbIntrinsic.forEach(outAllocation)
        outAllocation.copyTo(bitmap)
    }
}