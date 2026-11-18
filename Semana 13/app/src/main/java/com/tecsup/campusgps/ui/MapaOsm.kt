package com.tecsup.campusgps.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tecsup.campusgps.model.Campus
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.model.UbicacionUi
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

/**
 * Mejora 4: mapa de OpenStreetMap (osmdroid) para cuando no hay clave de Google Maps.
 * Recibe exactamente los mismos datos que MapaUbicacion y solo los dibuja:
 * tu posición en rojo, los lugares en azul, el más cercano en verde y el círculo del radio.
 */
@Composable
fun MapaOsm(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    idMasCercano: Int?,
    modifier: Modifier = Modifier,
    radio: Int? = null
) {
    val contexto = LocalContext.current

    val mapa = remember {
        // Política de OpenStreetMap: cada app debe identificarse con un User-Agent propio
        Configuration.getInstance().userAgentValue = contexto.packageName
        MapView(contexto).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            // Mejora 5: el mapa arranca sobre Tecsup Lima, igual que el de Google
            mover(this, GeoPoint(Campus.CENTRO.latitud, Campus.CENTRO.longitud), 17.0)
        }
    }

    // osmdroid necesita enterarse de onResume/onPause de la pantalla
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapa.onResume()
                Lifecycle.Event.ON_PAUSE -> mapa.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observador)
        onDispose {
            lifecycle.removeObserver(observador)
            mapa.onDetach()
        }
    }

    // Igual que en MapaUbicacion: la cámara viaja a tu posición y al lugar seleccionado
    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            mover(mapa, GeoPoint(estado.posicion.latitud, estado.posicion.longitud), 17.0)
        }
    }
    LaunchedEffect(seleccionado) {
        val lugar = lugares.firstOrNull { it.lugar.id == seleccionado }?.lugar ?: return@LaunchedEffect
        mover(mapa, GeoPoint(lugar.latitud, lugar.longitud), 18.0)
    }

    AndroidView(
        factory = { mapa },
        // MapView dibuja teselas fuera de su área: se recorta para no tapar el resto de la pantalla
        modifier = modifier.clipToBounds(),
        update = { vista -> dibujar(vista, estado, lugares, seleccionado, idMasCercano, radio) }
    )
}

/** Vuelve a dibujar las capas del mapa con el estado actual (sin calcular nada). */
private fun dibujar(
    vista: MapView,
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    idMasCercano: Int?,
    radio: Int?
) {
    val ctx = vista.context
    vista.overlays.clear()
    vista.overlays.add(CopyrightOverlay(ctx)) // "© OpenStreetMap contributors"

    if (estado is UbicacionUi.Lista) {
        val yo = GeoPoint(estado.posicion.latitud, estado.posicion.longitud)

        // Círculo del radio de cercanía (mejora 2)
        if (radio != null) {
            vista.overlays.add(Polygon(vista).apply {
                points = Polygon.pointsAsCircle(yo, radio.toDouble())
                fillPaint.color = Color.argb(0x33, 0x15, 0x65, 0xC0)
                outlinePaint.color = Color.rgb(0x15, 0x65, 0xC0)
                outlinePaint.strokeWidth = 3f
                setOnClickListener { _, _, _ -> false } // que no abra ventana al tocarlo
            })
        }

        vista.overlays.add(Marker(vista).apply {
            position = yo
            title = "Estás aquí"
            icon = iconoDeColor(ctx, Color.rgb(0xE5, 0x39, 0x35)) // rojo
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        })
    }

    lugares.forEach { item ->
        val marcador = Marker(vista).apply {
            position = GeoPoint(item.lugar.latitud, item.lugar.longitud)
            title = item.lugar.nombre
            snippet = item.lugar.categoria
            icon = iconoDeColor(
                ctx,
                if (item.lugar.id == idMasCercano) Color.rgb(0x2E, 0x7D, 0x32) // verde
                else Color.rgb(0x15, 0x65, 0xC0)                             // azul
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        vista.overlays.add(marcador)
        if (item.lugar.id == seleccionado) marcador.showInfoWindow()
    }

    vista.invalidate()
}

/**
 * Mueve la cámara con zoom. osmdroid ignora el zoom si el mapa todavía no tiene tamaño,
 * así que si aún no se dibujó, espera a su primer layout.
 */
private fun mover(mapa: MapView, punto: GeoPoint, zoom: Double) {
    val accion = {
        mapa.controller.setZoom(zoom)
        mapa.controller.animateTo(punto)
    }
    if (mapa.isLayoutOccurred) accion()
    else mapa.addOnFirstLayoutListener { _, _, _, _, _ -> accion() }
}

private fun iconoDeColor(ctx: Context, color: Int): Drawable? =
    ContextCompat.getDrawable(ctx, org.osmdroid.library.R.drawable.marker_default)
        ?.mutate()
        ?.also { DrawableCompat.setTint(it, color) }
