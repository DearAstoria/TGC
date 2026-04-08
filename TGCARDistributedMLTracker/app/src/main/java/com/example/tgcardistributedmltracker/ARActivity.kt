package com.example.tgcardistributedmltracker

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
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
                var currentDetections by remember { mutableStateOf<List<DetectionResult>>(emptyList()) }
                Box() {
                ARScene(
                    modifier = Modifier.fillMaxSize(),
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
//                                                                val hit = hitResults.firstOrNull { it.trackable is Plane }
        //
        //                                                        if (hit != null) {
        //                                                            // 3. Creating an Anchor at that physical location
        //                                                            val anchor = hit.createAnchor()
        //                                                            val anchorNode = AnchorNode(engine, anchor)
        //
        //                                                            // vv Not done yet, might not use, might have wrong syntax vv
        //                                                            //                                        // 4. Attach the 3D model
        //                                                            //                                        val modelNode = ModelNode(engine).apply {
        //                                                            //                                            loadModelGlbAsync(
        //                                                            //                                                glbFileLocation = "models/${detection.className.lowercase()}.glb",
        //                                                            //                                                scaleToUnits = 0.1f
        //                                                            //                                            )
        //                                                            //                                        }
        //                                                            //
        //                                                            //                                        anchorNode.addChild(modelNode)
        //                                                            //                                        arSceneView.addChild(anchorNode)
        //                                                            // YY                                                     YY
        //                                                        }
                                                            } // End of detections-loop
                                                            } // End of if (!detections.isEmpty())
                                                                    // Didn't need this anymore since I started .use
        //                                                    it.close() // Cleans up memory/Closes the frame
                                                                    // Note: Do not process frame after closing, image
                                                                    // object is borrowed memory and will fail if trying
                                                                    // to process after closing. This is why it's at the
                                                                    // end. -__o.o__-
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
//                    anchor?.let {
//                        AnchorNode(anchor = it) {
//                            ModelNode(modelInstance = helmet, scaleToUnits = 0.5f)
//                        }
//                    }
//                }

                // VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
//                var engine = rememberEngine()
//                var arSceneView: ARSceneView? = null
//                val materialLoader = remember { io.github.sceneview.loaders.MaterialLoader(engine,context) }
//                val cameraStream = rememberARCameraStream(materialLoader) // Starting the camera stream
//
//                Box(modifier = Modifier.fillMaxSize()) {
//                    ARScene(
//                        // Configure AR session features
//                        sessionFeatures = setOf(),
//                        sessionCameraConfig = null,
//
//                        // Configure AR session settings
//                        sessionConfiguration = { session, config ->
//                            // Enable depth if supported on the device
//                            config.depthMode = Config.DepthMode.AUTOMATIC
////                                when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
////                                    true -> Config.DepthMode.DISABLED
////                                    else -> Config.DepthMode.DISABLED
////                                }
////                            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
//                            // I believe this one vv uses the depth mode or it just makes it simpler
//                            // to disable it at first so yeah!
//                            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
////                            // FIXED helps initial link
////                            config.focusMode = Config.FocusMode.FIXED
//                            // Sometimes the 'Auto' light estimation causes the 'Processing error: null'
//                            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
//                            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE                            // Temporarily disabling these while debugging
////                            session.configure(session.config.apply {
////                                depthMode = Config.DepthMode.DISABLED
////                                lightEstimationMode = Config.LightEstimationMode.DISABLED
////                            })
//                            // FPS improvement in order to have sync process work properly.
//                            val filter = CameraConfigFilter(session).apply {
//                                setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30))
//                            }
//                            val configs = session.getSupportedCameraConfigs(filter)
//                            if (configs.isNotEmpty()) {
//                                session.cameraConfig = configs[0]
//                            }
//                        },
//
//                        // Enable plane detection visualization
//                        planeRenderer = true,
//
//                        // Configure camera stream
//                        cameraStream = cameraStream,
//
//                        // Session lifecycle callbacks
//                        onSessionCreated = { session ->
//                            // Handle session creation
//                        },
//                        onSessionResumed = { session ->
//                            // Handle session resume
//                        },
//                        onSessionPaused = { session ->
//                            // Handle session pause
//                        },
//                        modifier = Modifier.fillMaxSize(),
//
//                        // Frame update callback
//                        onSessionUpdated = { session, frame ->
//                            scope.launch {
//
//                            } // End of launch
//                        },
//
//                        // Error handling
//                        onSessionFailed = { exception ->
//                            // Handle ARCore session errors
//                        },
//
//                        // Track camera tracking state changes
//                        onTrackingFailureChanged = { trackingFailureReason ->
//                            // Handle tracking failures
//                        }
//                    ) // End of ARScene

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
                        } // End of for-each
                    } // End of Canvas
                }  // End of Box
            } // End of Theme
        } // End of setContent
    } // End of onCreate
//    override fun onDestroy() {
//        // Making sure these dang things get destroyed cause they might be holding onto memory.
//        if (::converter.isInitialized) {
//            converter.destroy()
//        }
//        super.onDestroy()
//    }
} // ARActivity


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