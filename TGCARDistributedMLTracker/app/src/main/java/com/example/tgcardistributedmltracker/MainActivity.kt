package com.example.tgcardistributedmltracker

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tgcardistributedmltracker.ui.theme.TGCARDistributedMLTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
        setContent {
            TGCARDistributedMLTrackerTheme {
                MainScreen(onStartAR = {
                    // Launch the ARActivity we built
                    startActivity(Intent(this, ARActivity::class.java))
                })
            }
        }
    }
}

@Composable
fun MainScreen(onStartAR: () -> Unit) {
    var hasCameraPermission by remember { mutableStateOf(false) }

    // Launcher for handling runtime camera permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TGC_AR_Distributed_MLTracker",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (hasCameraPermission) {
                Button(onClick = onStartAR) {
                    Text("Enter AR Arena")
                }
            } else {
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Permission")
                }
            }
        }
    }
}