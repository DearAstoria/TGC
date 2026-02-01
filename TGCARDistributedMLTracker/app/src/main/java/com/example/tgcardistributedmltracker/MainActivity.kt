package com.example.tgcardistributedmltracker

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
import com.google.ar.core.Pose
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.renderable.ModelRenderable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TGCARDistributedMLTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ARSceneViewContainer(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ARSceneViewContainer(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->

            val arSceneView = ArSceneView(context)

            // Show detected planes
            arSceneView.planeRenderer.isEnabled = true

            // Optional: Light estimation & shadows
            arSceneView.lightEstimationMode = io.github.sceneview.ar.arcore.LightEstimationMode.ENVIRONMENTAL_HDR

            // Tap listener: place a 3D model on a detected plane
            arSceneView.setOnTapArPlaneListener { hitResult, plane, _ ->

                // Create anchor at tap location
                val anchor = hitResult.createAnchor()

                // Load 3D model from assets
                ModelRenderable.load(context, Uri.parse("models/card_template.glb")).thenAccept { renderable ->
                    val modelNode = ModelNode().apply {
                        this.renderable = renderable
                        // Optional scale/rotation
                        localScale.set(0.5f, 0.5f, 0.5f)
                    }

                    // Attach model to anchor
                    modelNode.anchor = anchor
                    arSceneView.scene.addChild(modelNode)
                }
            }

            arSceneView
        }
    )
}