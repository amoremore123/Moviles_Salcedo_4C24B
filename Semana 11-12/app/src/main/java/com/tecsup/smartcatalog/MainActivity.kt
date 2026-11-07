package com.tecsup.smartcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tecsup.smartcatalog.ui.lista.ListaScreen
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import com.tecsup.smartcatalog.ui.theme.SmartCatalogTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }
    private val listaViewModel: ListaViewModel by viewModels { container.listaViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCatalogTheme {
                ListaScreen(viewModel = listaViewModel, imageLoader = container.imageLoader)
            }
        }
    }
}
