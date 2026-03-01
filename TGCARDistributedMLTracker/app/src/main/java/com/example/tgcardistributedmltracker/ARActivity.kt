package com.example.tgcardistributedmltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme
import io.github.sceneview.node.ModelNode

class ARActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TGCARDistributedMLTrackerTheme {




                                    anchor = hitResult.createAnchor()

                    )

                    )
                }
            }
    }
}