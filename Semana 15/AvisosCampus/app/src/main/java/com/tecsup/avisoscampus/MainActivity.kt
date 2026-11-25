package com.tecsup.avisoscampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.avisoscampus.ui.PantallaAvisos
import com.tecsup.avisoscampus.ui.theme.AvisosCampusTheme
import com.tecsup.avisoscampus.viewmodel.AvisosViewModel
import com.tecsup.avisoscampus.viewmodel.AvisosViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvisosCampusTheme {
                val vm: AvisosViewModel = viewModel(factory = AvisosViewModelFactory())
                PantallaAvisos(vm)
            }
        }
    }
}
