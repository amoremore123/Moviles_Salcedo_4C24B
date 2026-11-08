package com.tecsup.smartcatalog.testing

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/** Solo existe en el APK de pruebas: ninguna actividad de la suite consulta Internet real. */
class SmartCatalogTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, SmartCatalogTestApplication::class.java.name, context)
}
