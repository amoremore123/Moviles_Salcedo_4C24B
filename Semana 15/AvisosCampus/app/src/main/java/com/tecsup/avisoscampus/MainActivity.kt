package com.tecsup.avisoscampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.avisoscampus.ui.AppAvisosCampus
import com.tecsup.avisoscampus.ui.theme.AvisosCampusTheme
import com.tecsup.avisoscampus.viewmodel.AvisosViewModel
import com.tecsup.avisoscampus.viewmodel.AvisosViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvisosCampusTheme {
                // Una sola instancia del ViewModel para toda la app: se pasa a cada
                // pantalla, así el estado no se pierde al navegar
                val vm: AvisosViewModel = viewModel(factory = AvisosViewModelFactory())
                AppAvisosCampus(vm)
            }
        }
    }
}
