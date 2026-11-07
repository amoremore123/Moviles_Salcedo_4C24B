package com.tecsup.smartcatalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tecsup.smartcatalog.data.repository.ItemRepositoryPrueba
import com.tecsup.smartcatalog.ui.lista.ListaScreen
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import com.tecsup.smartcatalog.ui.theme.SmartCatalogTheme

class MainActivity : ComponentActivity() {
    private val listaViewModel: ListaViewModel by viewModels {
        ListaViewModel.Factory(ItemRepositoryPrueba())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartCatalogTheme {
                ListaScreen(viewModel = listaViewModel)
            }
        }
    }
}
