package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CloudNotesApp
import com.example.ui.CloudNotesViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CloudNotesApplication
        val factory = CloudNotesViewModel.Factory(app.repository)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val viewModel: CloudNotesViewModel = viewModel(factory = factory)
            CloudNotesApp(
                viewModel = viewModel,
                widthSizeClass = windowSizeClass.widthSizeClass
            )
        }
    }
}
