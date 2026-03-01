package com.example.adamtcgarfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
import com.google.ar.sceneform.SceneView
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import io.github.sceneview.loaders.ModelLoader

class ModelViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TGCARDistributedMLTrackerTheme {

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->

                        val sceneView = SceneView(context)

                        val modelLoader =
                            ModelLoader(sceneView.engine, context)

                        val modelNode =
                            ModelNode(engine = sceneView.engine)

                        modelNode.loadModelGlbAsync(
                            modelLoader = modelLoader,
                            glbFileLocation = "models/card_template.glb"
                        )

                        modelNode.isEditable = true

                        sceneView.addChildNode(modelNode)

                        sceneView
                    }
                )
            }
        }
    }
}