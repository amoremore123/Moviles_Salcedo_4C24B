package com.tecsup.campusgps.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tecsup.campusgps.model.Campus
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.model.UbicacionUi
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.ceil

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
            // Mejora 6: encuadra tu posición y todos los lugares a la vez (sin que se encimen)
            val puntos = lugares.map { GeoPoint(it.lugar.latitud, it.lugar.longitud) } +
                GeoPoint(estado.posicion.latitud, estado.posicion.longitud)
            encuadrar(mapa, puntos)
        }
    }
    LaunchedEffect(seleccionado) {
        val lugar = lugares.firstOrNull { it.lugar.id == seleccionado }?.lugar ?: return@LaunchedEffect
        mover(mapa, GeoPoint(lugar.latitud, lugar.longitud), 17.5)
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

    }

    // Primero los lugares y al final "Estás aquí", para que quede encima
    lugares.forEach { item ->
        // Mejora 6: pin con el emoji del lugar y su etiqueta al lado
        val icono = iconoLugarMapa(
            ctx,
            colorPin = if (item.lugar.id == idMasCercano) Color.rgb(0x1E, 0x8E, 0x3E) // verde
            else Color.rgb(0x1D, 0x4F, 0xBF),                                          // azul
            emoji = emojiLugar(item.lugar),
            etiqueta = etiquetaCorta(item.lugar)
        )
        val marcador = Marker(vista).apply {
            position = GeoPoint(item.lugar.latitud, item.lugar.longitud)
            title = item.lugar.nombre
            snippet = item.lugar.categoria
            this.icon = icono.dibujo
            setAnchor(icono.anclaX, icono.anclaY)
            setInfoWindowAnchor(icono.anclaX, 0f)
        }
        // Mejora 6: ya no se abre el globo gris; la etiqueta del pin muestra el nombre
        vista.overlays.add(marcador)
    }

    if (estado is UbicacionUi.Lista) {
        val icono = iconoYo(ctx)
        vista.overlays.add(Marker(vista).apply {
            position = GeoPoint(estado.posicion.latitud, estado.posicion.longitud)
            title = "Estás aquí"
            this.icon = icono.dibujo
            setAnchor(icono.anclaX, icono.anclaY)
            setInfoWindowAnchor(icono.anclaX, 0f)
        })
    }

    vista.invalidate()
}

/** Un ícono de marcador ya dibujado y el punto exacto (la punta del pin) que va sobre la coordenada. */
private class IconoMarcador(val dibujo: Drawable, val anclaX: Float, val anclaY: Float)

/** Pin de color con un emoji adentro y una etiqueta blanca a la derecha. */
private fun iconoLugarMapa(ctx: Context, colorPin: Int, emoji: String, etiqueta: String): IconoMarcador {
    val d = ctx.resources.displayMetrics.density
    val r = 17f * d
    val punta = 9f * d
    val margen = 5f * d
    val separacion = 3f * d
    val padH = 9f * d
    val padV = 4f * d

    val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12.5f * d
        typeface = Typeface.DEFAULT_BOLD
        color = Color.rgb(0x0B, 0x24, 0x63)
    }
    val anchoTexto = texto.measureText(etiqueta)
    val altoEtiqueta = texto.fontSpacing + 2 * padV

    val ancho = margen + 2 * r + separacion + anchoTexto + 2 * padH + margen
    val alto = margen + 2 * r + punta + margen
    val bmp = Bitmap.createBitmap(ceil(ancho).toInt(), ceil(alto).toInt(), Bitmap.Config.ARGB_8888)
    val lienzo = Canvas(bmp)

    val cx = margen + r
    val cy = margen + r
    dibujarPin(lienzo, cx, cy, r, punta, colorPin, d)

    // Emoji centrado dentro del pin
    val pinceEmoji = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = r * 0.95f
        textAlign = Paint.Align.CENTER
    }
    lienzo.drawText(emoji, cx, cy - (pinceEmoji.descent() + pinceEmoji.ascent()) / 2, pinceEmoji)

    // Etiqueta blanca redondeada
    val izq = cx + r + separacion
    val rect = RectF(izq, cy - altoEtiqueta / 2, izq + anchoTexto + 2 * padH, cy + altoEtiqueta / 2)
    val fondo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        setShadowLayer(3f * d, 0f, 1f * d, 0x40000000)
    }
    lienzo.drawRoundRect(rect, altoEtiqueta / 2, altoEtiqueta / 2, fondo)
    lienzo.drawText(etiqueta, izq + padH, rect.centerY() - (texto.descent() + texto.ascent()) / 2, texto)

    return IconoMarcador(BitmapDrawable(ctx.resources, bmp), cx / bmp.width, (cy + r + punta) / bmp.height)
}

/** Pin rojo de tu posición con el globo "Estás aquí" encima. */
private fun iconoYo(ctx: Context): IconoMarcador {
    val d = ctx.resources.displayMetrics.density
    val rojo = Color.rgb(0xE5, 0x39, 0x35)
    val r = 15f * d
    val punta = 8f * d
    val margen = 5f * d
    val flecha = 6f * d
    val padH = 11f * d
    val padV = 5f * d

    val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13.5f * d
        typeface = Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }
    val etiqueta = "Estás aquí"
    val anchoGlobo = texto.measureText(etiqueta) + 2 * padH
    val altoGlobo = texto.fontSpacing + 2 * padV

    val ancho = maxOf(anchoGlobo, 2 * r) + 2 * margen
    val alto = margen + altoGlobo + flecha + 3f * d + 2 * r + punta + margen
    val bmp = Bitmap.createBitmap(ceil(ancho).toInt(), ceil(alto).toInt(), Bitmap.Config.ARGB_8888)
    val lienzo = Canvas(bmp)
    val cx = bmp.width / 2f

    // Globo rojo con su flechita
    val pincel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = rojo
        setShadowLayer(3f * d, 0f, 1f * d, 0x40000000)
    }
    val globo = RectF(cx - anchoGlobo / 2, margen, cx + anchoGlobo / 2, margen + altoGlobo)
    lienzo.drawRoundRect(globo, altoGlobo / 2, altoGlobo / 2, pincel)
    lienzo.drawPath(Path().apply {
        moveTo(cx - flecha, globo.bottom - 1)
        lineTo(cx, globo.bottom + flecha)
        lineTo(cx + flecha, globo.bottom - 1)
        close()
    }, pincel)
    lienzo.drawText(etiqueta, cx, globo.centerY() - (texto.descent() + texto.ascent()) / 2,
        texto.apply { textAlign = Paint.Align.CENTER })

    // Pin rojo con punto blanco
    val cy = globo.bottom + flecha + 3f * d + r
    dibujarPin(lienzo, cx, cy, r, punta, rojo, d)
    lienzo.drawCircle(cx, cy, r * 0.38f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })

    return IconoMarcador(BitmapDrawable(ctx.resources, bmp), cx / bmp.width, (cy + r + punta) / bmp.height)
}

/** Gota de color (círculo + punta hacia abajo) con un anillo blanco interior. */
private fun dibujarPin(lienzo: Canvas, cx: Float, cy: Float, r: Float, punta: Float, color: Int, d: Float) {
    val pincel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        setShadowLayer(3f * d, 0f, 1.5f * d, 0x55000000)
    }
    lienzo.drawPath(Path().apply {
        moveTo(cx - r * 0.62f, cy + r * 0.78f)
        lineTo(cx, cy + r + punta)
        lineTo(cx + r * 0.62f, cy + r * 0.78f)
        close()
    }, pincel)
    lienzo.drawCircle(cx, cy, r, pincel)
    lienzo.drawCircle(cx, cy, r - 2.5f * d, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f * d
    })
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

/** Ajusta el zoom para que todos los puntos entren en el mapa, con margen para las etiquetas. */
private fun encuadrar(mapa: MapView, puntos: List<GeoPoint>) {
    if (puntos.isEmpty()) return
    val caja = BoundingBox.fromGeoPoints(puntos)
    val margen = (64 * mapa.resources.displayMetrics.density).toInt()
    val accion = { mapa.zoomToBoundingBox(caja, true, margen) }
    if (mapa.isLayoutOccurred) accion()
    else mapa.addOnFirstLayoutListener { _, _, _, _, _ -> accion() }
}
