package com.tecsup.ayudacampus.ui.navigation

object Rutas {
    const val Splash = "Splash"
    const val Login = "Login"
    const val MisTickets = "MisTickets"
    const val Panel = "Panel"
    const val NuevoTicket = "NuevoTicket"
    const val TicketId = "ticketId"
    const val DetalleTicket = "DetalleTicket/{ticketId}"
    const val AtenderTicket = "AtenderTicket/{ticketId}"
    fun atenderTicket(id: Int): String = "AtenderTicket/$id"
    fun detalleTicket(id: Int): String = "DetalleTicket/$id"
}
