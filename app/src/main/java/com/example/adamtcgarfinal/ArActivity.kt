import android.os.Bundle
import androidx.activity.ComponentActivity

class ARActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 0)
        }

        setContent {
            TGCARDistributedMLTrackerTheme {

                var coordinates = "Tap to place cube"

                Box(modifier = Modifier.fillMaxSize()) {

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->

                            val arSceneView = ArSceneView(context)
                            val modelLoader = ModelLoader(arSceneView.engine, context)

                            arSceneView.onTapAr =
                                { hitResult: HitResult, _: Plane, _: MotionEvent ->

                                    val anchorNode =
                                        arSceneView.createAnchorNode(hitResult)

                                    val modelNode =
                                        ModelNode(engine = arSceneView.engine)

                                    modelNode.loadModelGlbAsync(
                                        modelLoader = modelLoader,
                                        glbFileLocation = "models/card_template.glb"
                                    )

                                    anchorNode.addChildNode(modelNode)
                                    arSceneView.addChildNode(anchorNode)

                                    val pose = hitResult.hitPose
                                    coordinates =
                                        "X: %.2f Y: %.2f Z: %.2f"
                                            .format(pose.tx(), pose.ty(), pose.tz())
                                }

                            arSceneView
                        }
                    )

                    Text(
                        text = coordinates,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}