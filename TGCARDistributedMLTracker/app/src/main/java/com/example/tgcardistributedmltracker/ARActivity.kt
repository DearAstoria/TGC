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
import com.google.ar.core.Plane


class ARActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TGCARDistributedMLTrackerTheme {

                var arSceneView: ARSceneView? = null

                ARScene(
                    modifier = Modifier.fillMaxSize(),
                    planeRenderer = true,

                    onSessionCreated = { sceneView ->
                        arSceneView = sceneView
                    },

                    onGestureListener = remember {
                        object : GestureDetector.SimpleOnGestureListener() {

                            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

                                val sceneView = arSceneView ?: return false

                                val hitResult = sceneView.hitTest(e)
                                    .firstOrNull { hit ->
                                        hit.trackable is Plane &&
                                                (hit.trackable as Plane)
                                                    .isPoseInPolygon(hit.hitPose)
                                    } ?: return false

                                val anchorNode = AnchorNode(
                                    engine = sceneView.engine,
                                    anchor = hitResult.createAnchor()
                                )

                                val modelNode = ModelNode(
                                    engine = sceneView.engine
                                ).apply {
                                    loadModelGlbAsync(
                                        glbFileLocation = "models/card_template.glb",
                                        autoAnimate = true,
                                        scaleToUnits = 0.1f
                                    )
                                }

                                anchorNode.addChild(modelNode)
                                sceneView.addChild(anchorNode)

                                return true
                            }
                        }
                    }
                )
            }
        }


    }
}