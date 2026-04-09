package com.example.tgcardistributedmltracker//package com.example.tgcardistributedmltracker
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.viewinterop.AndroidView
//import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
//import io.github.sceneview.SceneView
//import io.github.sceneview.node.ModelNode
//import io.github.sceneview.math.Rotation
//import io.github.sceneview.math.Scale
//
//class ModelViewerActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            TGCARDistributedMLTrackerTheme {
//                Box(modifier = Modifier.fillMaxSize()) {
//
//                    AndroidView(
//                        factory = { context ->
//                            val sceneView = SceneView(context)
//
//                            // Create a ModelNode to hold your 3D model
//                            val modelNode = ModelNode(sceneView.engine).apply {
//                                loadModelGlbAsync(
//                                    glbFileLocation = "models/card_template.glb"
//                                )
//                                // Optional: scale and rotation
//                                scale = Scale(0.5f)
//                                rotation = Rotation(0f, 180f, 0f)
//                            }
//
//                            // Add the model to the scene
//                            sceneView.addChild(modelNode)
//
//                            sceneView
//                        },
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//            }
//        }
//    }
//}