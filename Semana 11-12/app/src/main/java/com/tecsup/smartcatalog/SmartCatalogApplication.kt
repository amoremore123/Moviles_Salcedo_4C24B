package com.tecsup.smartcatalog

import android.app.Application

/** Un único contenedor y una única instancia de Room por proceso, también al rotar la actividad. */
open class SmartCatalogApplication : Application() {
    open val container: AppContainer by lazy { AppContainer(this) }
}
