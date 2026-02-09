package com.example.tgcardistributedmltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.node.ModelNode
import io.github.sceneview.renderable.ModelRenderable
import io.github.sceneview.utils.toUri

class ARActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TGCARDistributedMLTrackerTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    AndroidView(
                        factory = { context ->
                            val arSceneView = ArSceneView(context)

                            // Enable AR Session
                            arSceneView.setupSession(ArSession(context))

                            // Optional: configure light estimation
                            arSceneView.lightEstimationMode = ArSceneView.LightEstimationMode.ENVIRONMENTAL_HDR

                            // Tap to place model
                            arSceneView.setOnTapArPlaneListener { hitResult ->
                                val modelNode = ModelNode().apply {
                                    // Load your GLB 3D card
                                    renderable = ModelRenderable.loadModel(
                                        context,
                                        "models/card_template.glb".toUri()
                                    )
                                    // Place the model at the anchor
                                    anchor = hitResult.createAnchor()
                                }
                                arSceneView.addChild(modelNode)
                            }

                            arSceneView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Placeholder UI overlay for ML confidence
                    Text(
                        text = "ML Confidence: 0.0",
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}