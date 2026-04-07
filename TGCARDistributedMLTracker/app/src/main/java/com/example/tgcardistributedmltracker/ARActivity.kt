package com.example.tgcardistributedmltracker

import android.graphics.Bitmap
import android.os.Bundle
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
import io.github.sceneview.ar.ARSceneView
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
import com.google.ar.core.Plane
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.loaders.MaterialLoader
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import io.github.sceneview.rememberEngine


class ARActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TGCARDistributedMLTrackerTheme {
                var engine = rememberEngine()
                var arSceneView: ARSceneView? = null
                val context = LocalContext.current
                val materialLoader = remember { io.github.sceneview.loaders.MaterialLoader(engine,context) }
                val cardDetector = remember { CardDetector(context) }
                var lastInferenceTime: Long = 0
                val bitmap = remember { Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888) }
                val converter = remember { YuvToRgbConverter(context) }
                var currentDetections by remember { mutableStateOf<List<DetectionResult>>(emptyList()) }

                // ADAM ADDED THIS FOR THE BEGINNINGS OF A DAMAGE INDICATOR!!!!!! ////////////////////////////////////////////////////////////

                // DAMAGE INDICATOR MAIN CODE STATE ITSELF??? //////////////////
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


                Box(modifier = Modifier.fillMaxSize()) {
                    ARScene(
                        // Configure AR session features
                        sessionFeatures = setOf(),
                        sessionCameraConfig = null,

                        // Configure AR session settings
                        sessionConfiguration = { session, config ->
                            // Enable depth if supported on the device
                            config.depthMode =
                                when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                                    true -> Config.DepthMode.AUTOMATIC
                                    else -> Config.DepthMode.DISABLED
                                }
                            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        },

                        // Enable plane detection visualization
                        planeRenderer = true,

                        // Configure camera stream
                        cameraStream = rememberARCameraStream(materialLoader),

                        // Session lifecycle callbacks
                        onSessionCreated = { session ->
                            // Handle session creation
                        },
                        onSessionResumed = { session ->
                            // Handle session resume
                        },
                        onSessionPaused = { session ->
                            // Handle session pause
                        },
                        modifier = Modifier.fillMaxSize(),
                        // Frame update callback
                        onSessionUpdated = { session, frame ->
                            val currentTime = System.currentTimeMillis()

                            // Only run inference every 100ms (10 FPS) to save battery
                            if (currentTime - lastInferenceTime > 100) {
                                lastInferenceTime = currentTime

                                // Acquire the camera image/frame
                                val cameraImage = frame.acquireCameraImage()

                                // Only process if the image exists
                                cameraImage?.let {
                                    converter.yuvToRgb(it, bitmap)

                                    // Feed the bitmap
                                    val detections = cardDetector.detectCard(bitmap)
                                    // Updating currentDetections,
                                    currentDetections = detections
                                    // Handling Detections code:
                                    // vv might update the UI here and/or
                                    // vv creating Anchor 3D models                               vv
                                    for (detection in detections) {
                                        // 1. Getting the center of the card's bounding box
                                        val centerX = detection.boundingBox.centerX()
                                        val centerY = detection.boundingBox.centerY()

                                        // 2. Performing a Hit-Test to see where that point hits the
                                        // physical floor/table
                                        val hitResults = frame.hitTest(centerX, centerY)
                                        val hit = hitResults.firstOrNull { it.trackable is Plane }

                                        if (hit != null) {
                                            // 3. Creating an Anchor at that physical location
                                            val anchor = hit.createAnchor()
                                            val anchorNode = AnchorNode(engine, anchor)

                                            // vv Not done yet, might not use, might have wrong syntax vv
//                                        // 4. Attach the 3D model
//                                        val modelNode = ModelNode(engine).apply {
//                                            loadModelGlbAsync(
//                                                glbFileLocation = "models/${detection.className.lowercase()}.glb",
//                                                scaleToUnits = 0.1f
//                                            )
//                                        }
//
//                                        anchorNode.addChild(modelNode)
//                                        arSceneView.addChild(anchorNode)
                                            // YY                                                     YY
                                        }
                                    } // End of detections-loop

                                    it.close() // Cleans up memory/Closes the frame
                                    // Note: Do not process frame after closing, image
                                    // object is borrowed memory and will fail if trying
                                    // to process after closing. This is why it's at the
                                    // end. -__o.o__-
                                }
                            }
                        },

                        // Error handling
                        onSessionFailed = { exception ->
                            // Handle ARCore session errors
                        },

                        // Track camera tracking state changes
                        onTrackingFailureChanged = { trackingFailureReason ->
                            // Handle tracking failures
                        }
                    ) // End of ARScene

                    // The Debug Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        currentDetections.forEach { detection ->
                            // YOLOv8 returns 0-640 coordinates; we need to scale them to the screen size
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
                }  // End of Box
            }
        }
    }
}


//                ARScene(
//                    modifier = Modifier.fillMaxSize(),
//                    planeRenderer = true,
//
//                    onSessionCreated = { sceneView ->
//                        arSceneView = sceneView
//                    },
//
//                    onGestureListener = remember {
//                        object : GestureDetector.SimpleOnGestureListener() {
//
//                            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
//
//                                val sceneView = arSceneView ?: return false
//
//                                val hitResult = sceneView.hitTest(e)
//                                    .firstOrNull { hit ->
//                                        hit.trackable is Plane &&
//                                                (hit.trackable as Plane)
//                                                    .isPoseInPolygon(hit.hitPose)
//                                    } ?: return false
//
//                                val anchorNode = AnchorNode(
//                                    engine = sceneView.engine,
//                                    anchor = hitResult.createAnchor()
//                                )
//
//                                val modelNode = ModelNode(
//                                    engine = sceneView.engine
//                                ).apply {
//                                    loadModelGlbAsync(
//                                        glbFileLocation = "models/card_template.glb",
//                                        autoAnimate = true,
//                                        scaleToUnits = 0.1f
//                                    )
//                                }
//
//                                anchorNode.addChild(modelNode)
//                                sceneView.addChild(anchorNode)
//
//                                return true
//                            }
//                        }
//                    }
//                )