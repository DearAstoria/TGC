package com.example.tgcardistributedmltracker

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.renderable.ModelRenderable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TGCARDistributedMLTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        ARSceneViewContainer(modifier = Modifier.fillMaxSize())

                        // Placeholder overlay for ML confidence
                        Text(
                            text = "ML Confidence: 0.00",
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ARSceneViewContainer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val arSceneView = remember { ArSceneView(context) }

    LaunchedEffect(Unit) {
        arSceneView.planeRenderer.isEnabled = true
        arSceneView.lightEstimationMode = LightEstimationMode.ENVIRONMENTAL_HDR

        // Load initial 3D card model
        ModelRenderable.load(context, Uri.parse("models/card_template.glb"))
            .thenAccept { renderable ->
                val modelNode = ModelNode().apply {
                    this.renderable = renderable
                    localScale.set(0.5f, 0.5f, 0.5f)
                    localRotation.set(0f, 180f, 0f)
                }
                arSceneView.scene.addChild(modelNode)
            }

        // Tap to place AR card
        arSceneView.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent? ->
            val anchor: Anchor = hitResult.createAnchor()
            ModelRenderable.load(context, Uri.parse("models/card_template.glb"))
                .thenAccept { renderable ->
                    val modelNode = ModelNode().apply {
                        this.renderable = renderable
                        localScale.set(0.5f, 0.5f, 0.5f)
                    }
                    modelNode.anchor = anchor
                    arSceneView.scene.addChild(modelNode)
                }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { arSceneView }
    )
}