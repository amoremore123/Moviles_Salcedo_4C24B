package com.tecsup.smartcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tecsup.smartcatalog.ui.navigation.AppNav
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import com.tecsup.smartcatalog.ui.auth.SesionViewModel
import com.tecsup.smartcatalog.ui.theme.SmartCatalogTheme

class MainActivity : ComponentActivity() {
    private val container get() = (application as SmartCatalogApplication).container
    private val listaViewModel: ListaViewModel by viewModels { container.listaViewModelFactory }
    private val sesionViewModel: SesionViewModel by viewModels { container.sesionViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCatalogTheme {
                AppNav(container = container, sesionViewModel = sesionViewModel, listaViewModel = { listaViewModel })
            }
        }
    }
}
