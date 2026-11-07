package com.tecsup.smartcatalog

import com.tecsup.smartcatalog.data.auth.AuthRepository
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.notification.Notificador

/** Agrupa contratos recibidos por constructor, sin inicializar servicios concretos. */
class AppContainer(
    val authRepository: AuthRepository,
    val itemRepository: ItemRepository,
    val notificador: Notificador
)
