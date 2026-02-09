package com.example.tgcardistributedmltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.renderable.ModelRenderable
import io.github.sceneview.utils.toUri

class ModelViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TGCARDistributedMLTrackerTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    AndroidView(
                        factory = { context ->
                            val sceneView = SceneView(context)

                            // Create a ModelNode to hold your 3D model
                            val modelNode = ModelNode().apply {
                                renderable = ModelRenderable.loadModel(
                                    context,
                                    "models/card_template.glb".toUri()
                                )
                                // Optional: scale and rotation
                                localScale.set(0.5f, 0.5f, 0.5f)
                                localRotation.set(0f, 180f, 0f)
                            }

                            // Add the model to the scene
                            sceneView.addChild(modelNode)

                            sceneView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}