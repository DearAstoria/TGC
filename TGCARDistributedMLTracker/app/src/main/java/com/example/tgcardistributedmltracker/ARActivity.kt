package com.example.tgcardistributedmltracker

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text

//ADAM ADDED THESE/////////////////////////////////////////////////////////////////////////////////
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
import io.github.sceneview.ar.ARScene
//import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.model.ModelInstance
//  import io.github.sceneview.ar.node.ArModelNode DEPRECATED
//  import io.github.sceneview.ar.node.PlacementMode DEPRECATED
//import io.github.sceneview.ar.arcore.getBestAnchor // Also deprecated
//import io.github.sceneview.rememberARSceneView // DEPRECATED
import io.github.sceneview.node.ModelNode
import io.github.sceneview.ar.node.AnchorNode
import android.view.GestureDetector // <--vv Needed for modern implementations
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.google.ar.core.Config
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.google.ar.core.Plane
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.loaders.MaterialLoader
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.google.ar.core.Anchor
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotYetAvailableException
//import io.github.sceneview.SceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.rememberEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.EnumSet
import io.github.sceneview.ar.rememberARCameraStream
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import io.github.sceneview.rememberEngine
import kotlin.div
import kotlin.text.toFloat
import kotlin.text.toInt
import kotlin.times


class ARActivity : ComponentActivity() {
    private lateinit var converter: YuvToRgbConverter
    override fun onCreate(savedInstanceState: Bundle?) {
        System.setProperty("io.github.sceneview.render_backend", "opengl")
        System.setProperty("filament.backend", "opengl")
        super.onCreate(savedInstanceState)
        setContent {
            TGCARDistributedMLTrackerTheme {
                var anchor by remember { mutableStateOf<Anchor?>(null) }
                var isProcessing by remember { mutableStateOf(false) }
                // before the ARScene ensure it's ready for use when the ARScene is set up.
                var lastInferenceTime: Long = 0
                val bitmap = remember { Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888) }
                val context = LocalContext.current
                converter = remember { YuvToRgbConverter(context) }
                val scope = rememberCoroutineScope()
                val cardDetector = remember { CardDetector(context) }
                var currentDetections: List<DetectionResult> by remember { mutableStateOf<List<DetectionResult>>(emptyList())}
                var damageAmount by remember { mutableStateOf(0) }
                var showDamage by remember { mutableStateOf(false) }
                var damageOffset by remember { mutableStateOf(0f) }
                // SPARK PARTICLE STATE
                // ADAM ADDED THESE //
                data class Spark (
                    var x: Float,
                    var y: Float,
                    var vx: Float,
                    var vy: Float,
                    var life: Float,
                )
                var sparks by remember { mutableStateOf(listOf<Spark>()) }
                // ============================================
                // TURN TIMER: COUNTS DOWN HOW LONG YOU HAVE TO ATTACK
                // ============================================
                var turnTime by remember { mutableStateOf(10f) }
                val animatedTurnTime by animateFloatAsState(targetValue = turnTime)
                Box(Modifier.fillMaxSize()) {
                ARScene(
                    planeRenderer = true,
                    onSessionUpdated = { _, frame ->
                        if (anchor == null) {
                            anchor = frame.getUpdatedPlanes()
                                .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                                ?.let { it.createAnchorOrNull(it.centerPose) }
                        }
                        if (frame.camera.trackingState == TrackingState.TRACKING && !isProcessing) {
                                    try {
                                        val currentTime = System.currentTimeMillis()

                                        // Only run inference every 150ms (15 FPS) to save battery and stability purposes.
                                        if (currentTime - lastInferenceTime > 150) {
                                            lastInferenceTime = currentTime
                                            // Acquire the camera image/frame
                                            val cameraImage = try { frame.acquireCameraImage() } catch (e: Exception) { null }
                                            cameraImage?.let { image ->
                                                isProcessing = true
                                                // Launching this on a background thread in order to keep the UI running smoothly.
                                                scope.launch(Dispatchers.Default) {
                                                    try {
                                                        cameraImage.let {
                                                            Log.i("Hi", "BEFORE converter\n")
                                                            if (cameraImage.planes.size >= 3) {
                                                                Log.d("ARCORE", "Camera Image: ${cameraImage.width}x${cameraImage.height} | Bitmap: ${bitmap.width}x${bitmap.height}")
                                                                converter.yuvToRgb(
                                                                    cameraImage,
                                                                    bitmap
                                                                )
                                                            } else {
                                                                Log.e(
                                                                    "ARCORE",
                                                                    "Image planes invalid: ${cameraImage.planes.size}"
                                                                )
                                                            }
                                                            Log.i("Hi", "After converter\n")
                                                            // Feed the bitmap
                                                            val detections = cardDetector.detectCard(bitmap)
        //                                                    // Updating currentDetections,
                                                            currentDetections = detections
        //                                                    // Handling Detections code:
        //                                                    // vv might update the UI here and/or
        //                                                    // vv creating Anchor 3D models                               vv
                                                            if (!detections.isEmpty()) {
                                                                Log.i("DETECTIONS: detections not empty", "$detections\n")
                                                                for (detection in detections) {
                                                                // 1. Getting the center of the card's bounding box
                                                                val centerX = detection.boundingBox.centerX()
                                                                val centerY = detection.boundingBox.centerY()
        //
        //                                                        // 2. Performing a Hit-Test to see where that point hits the
        //                                                        // physical floor/table
                                                                val hitResults = frame.hitTest(centerX, centerY)
                                                            } // End of detections-loop
                                                            } // End of if (!detections.isEmpty())
                                                                } // End of cameraImage.let
                                                    } catch (e: Exception) {
                                                        Log.e(
                                                            "ARCORE",
                                                            "Processing error: ${e}"
                                                        )
                                                    } finally {
                                                        cameraImage.close()
                                                        isProcessing =
                                                            false // Using the finally block for state-management
                                                        // the use. statement still closes the image.
                                                        // I might re-arrange the code so it looks cleaner later
                                                        // and this difference in uses are more explicit.
                                                    }
                                                } // End of launch
                                            } // End of let
                                        } // End of if(inferenceTime-..)
                                    } catch (e: NotYetAvailableException) {
                                        // This is normal during the first 1-2 seconds of startup
                                        Log.d("ARCORE", "Frame not yet available")
                                    } catch (e: Exception) {
                                        Log.e("ARCORE", "Detection error: ${e.message}")
                                    } finally {
                                    }
                                } // End of if (frame.camera.tracking..)
                    }
                ) // {

                    // The Debug Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        currentDetections.forEach { detection ->
//                            // YOLOv8 returns 0-640 coordinates; we need to scale them to the screen size
                            val scaleX = size.width / 640f
                            val scaleY = size.height / 640f

                            val left = detection.boundingBox.left * scaleX
                            val top = detection.boundingBox.top * scaleY
                            val right = detection.boundingBox.right * scaleX
                            val bottom = detection.boundingBox.bottom * scaleY

                            // Draw the bounding box
                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top),
                                style = Stroke(width = 5f)
                            )
//
//                            // Draw the Label (I.E., "Bulb_Wiz 99%")
                            drawContext.canvas.nativeCanvas.drawText(
                                "${detection.className} ${(detection.confidence * 100).toInt()}%",
                                left,
                                top - 10f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GREEN
                                    textSize = 40f
                                    isFakeBoldText = true
                                }
                            )

                        drawRect(
                            color = Color.Gray,
                            topLeft = Offset(20f, 20f),
                            size = Size(600f, 20f)
                        )
                        drawRect(
                            color = Color.Cyan,
                            topLeft = Offset(20f, 20f),
                            size = Size(600f * animatedTurnTime / 10f, 20f)
                        )
                        currentDetections.forEach { detection ->
                            // YOLOv8 returns 0-640 coordinates; we need to scale them to the screen size
                            val scaleX = size.width / 640f
                            val scaleY = size.height / 640f

                            val left = detection.boundingBox.left * scaleX
                            val top = detection.boundingBox.top * scaleY
                            val right = detection.boundingBox.right * scaleX
                            val bottom = detection.boundingBox.bottom * scaleY

                            // CARD GLOW //
                            drawRoundRect(
                                color = Color.Cyan.copy(alpha = 0.3f),
                                topLeft = Offset(left -10f, top - 10f),
                                size = Size(right - left + 20f, bottom - top + 20f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                            )

                            // Draw the bounding box
                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top),
                                style = Stroke(width = 5f)
                            )

                            // Draw the Label (I.E., "Bulb_Wiz 99%")
                            // ADAM DID SOME RESEARCH AND UPDATED THIS CODE A LITTLE BIT TO SHOW MORE ABOUT THE CARD LABELS //////////////////////////////////////
                            // BOUNDING BOX ITSELF
                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top),
                                style = Stroke(width = 5f)
                            )

                            // LABEL BACKGROUND PANEL ITSELF
                            drawRect(
                                color = Color.Black.copy(alpha = 0.7f),
                                topLeft = Offset(left, top - 80f),
                                size = Size(right - left, 60f)
                            )

                            // CARD NAME DISPLAY ITSELF
                            drawContext.canvas.nativeCanvas.drawText(
                                detection.className,
                                left + 10f,
                                top - 45f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 38f
                                    isFakeBoldText = true
                                }
                            )

                            // AI CONFIDENCE SCORE ITSELF
                            drawContext.canvas.nativeCanvas.drawText(
                                "Confidence: ${(detection.confidence * 100).toInt()}%",
                                left + 10f,
                                top - 15f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.LTGRAY
                                    textSize = 30f
                                }
                            )

                            // ADAM ADDED THESE /////////////////////////////////////////////////////////
                            // ==============================
                            // HEALTH BAR BACKGROUND
                            drawRect(
                                color = Color.DarkGray,
                                topLeft = Offset(left, top - 110f),
                                size = Size(right - left, 12f)
                            )

                            // HEALTH BAR CURRENT VALUE ITSELF
                            // This is going to be a green bar that shows current health remaining
                            // Will need to be replaced with real game health later
                            val healthPercent = detection.confidence.toFloat()
                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(left, top - 100f),
                                size = Size((right - left) * healthPercent, 12f)
                            )

                            // HEALTH BAR TEXT DISPLAY
                            // This shows the numerical health above health bar
                            drawContext.canvas.nativeCanvas.drawText(
                                "HP: ${(healthPercent * 100).toInt()}",
                                left + 5f,
                                top - 120f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 28f
                                    isFakeBoldText = true
                                }
                            )

                            // THIS TOOK FOREVEEEEEER THIS IS THE DAMAGE INDICATOR CODE!!!!! ////////////////////////////////////////
                            // This should display floating damage numbers when attacks happen
                            if(showDamage && currentDetections.isNotEmpty()) {
                                val detection = currentDetections.first()
                                val scaleX = size.width / 640f
                                val scaleY = size.height / 640f
                                val centerX = detection.boundingBox.centerX() * scaleX
                                val topY = detection.boundingBox.top * scaleY

                                // HIT FLASH EFFECT
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.5f),
                                    topLeft = Offset(left - 5f, top - 5f),
                                    size = Size(right - left + 10f, bottom - top + 10f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(15f, 15f)
                                )

                                // ANIMATION FOR FLOATING!!!!
                                damageOffset += 3f
                                drawContext.canvas.nativeCanvas.drawText(
                                    "-$damageAmount",
                                    centerX,
                                    topY - 140f - damageOffset,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.RED
                                        textSize = 60f
                                        isFakeBoldText = true
                                    }
                                )

                                // CODE THAT MAKES IT GO AWAY AFTER ANIMATING FOR A BIT
                                if (damageOffset > 80f) {
                                    showDamage = false
                                }
                            }
                        }
                        } // End of for-each
                        // ========================
                        // SPARK PARTICLE SYSTEM
                        // ========================
                        val updatedSparks = mutableListOf<Spark>()

                        sparks.forEach { spark ->
                            spark.x += spark.vx
                            spark.y += spark.vy
                            spark.vy += 0.5f
                            spark.life -= 0.03f

                            if (spark.life > 0f) {
                                updatedSparks.add(spark)

                                drawCircle(
                                    color = Color.Yellow.copy(alpha = spark.life),
                                    radius = 6f,
                                    center = Offset(
                                        spark.x * (size.width / 640f),
                                        spark.y * (size.height / 640f)
                                    )
                                )
                            }
                        }
                        sparks = updatedSparks

                    } // End of Canvas
                    // ADAM ADDED THESE /////////////////////////////////////////////////////////////////
                    // ADDED MORE NOTES LIKE IN JADENS' CODE EXPLAINING WHAT THINGS ARE
                    // ============================
                    // Attack Button UI
                    // ============================
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Makes the container span the full screen
                            .padding(bottom = 40.dp), // Adds space above the bottom
                        contentAlignment = Alignment.BottomCenter
                    ) {

                        // ========================
                        // Attack Button
                        // ========================
                        Button(
                            onClick = {
                                damageAmount = (10..30).random()
                                showDamage = true
                                damageOffset = 0f

                                // SPAWN SPARKS //
                                // ADAM ADDED THIS //
                                if (currentDetections.isNotEmpty()) {
                                    val detection = currentDetections.first()
                                    val centerX = detection.boundingBox.centerX().toFloat()
                                    val centerY = detection.boundingBox.centerY().toFloat()

                                    sparks = List(20) {
                                        Spark(
                                            x = centerX,
                                            y = centerY,
                                            vx = (-5..5).random().toFloat(),
                                            vy = (-10..-2).random().toFloat(),
                                            life = 1f
                                        )
                                    }
                                }
                                println("Attack button pressed - Damage: $damageAmount")
                            },
                            // This section gives the button rounded corners
                            shape = RoundedCornerShape(20.dp),
                            // This section sets the color of the button to Red
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            ),

                            // This section defines the physical size of the actual button
                            modifier = Modifier
                                .size(width = 200.dp, height = 70.dp)
                        ) {
                            Text(
                                text = "Attack",
                                color = Color.White
                            )
                        }
                    }
                }  // End of Attack Box
            } // End of Theme
        } // End of setContent
    } // End of onCreate
} // ARActivity