package com.tecsup.campusgps.data

import com.tecsup.campusgps.model.Lugar

interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
