package com.tecsup.smartcatalog.testing

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

internal fun ComposeTestRule.capturarMejora(etapa: String, pantalla: String) {
    if (InstrumentationRegistry.getArguments().getString("capturarMejoras") != etapa) return
    val instrumentacion = InstrumentationRegistry.getInstrumentation()
    val captura = onRoot().captureToImage().asAndroidBitmap()
    try {
        File(instrumentacion.targetContext.cacheDir, "$etapa-$pantalla.png").outputStream().use {
            check(captura.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    } finally {
        captura.recycle()
    }
}
