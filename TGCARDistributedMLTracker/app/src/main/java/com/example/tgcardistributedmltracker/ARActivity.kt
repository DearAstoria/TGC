package com.example.tgcardistributedmltracker

import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import com.google.ar.core.Config
import com.google.ar.core.Plane
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.loaders.MaterialLoader
import androidx.compose.runtime.remember
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

                    // Frame update callback
                    onSessionUpdated = { session, updatedFrame ->
                        // Process AR frame updates

                    },

                    // Error handling
                    onSessionFailed = { exception ->
                        // Handle ARCore session errors
                    },

                    // Track camera tracking state changes
                    onTrackingFailureChanged = { trackingFailureReason ->
                        // Handle tracking failures
                    }

                )

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
            }
        }


    }
}