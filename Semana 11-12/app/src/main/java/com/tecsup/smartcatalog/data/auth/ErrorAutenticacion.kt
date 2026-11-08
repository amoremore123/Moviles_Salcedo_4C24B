package com.tecsup.smartcatalog.data.auth

enum class TipoErrorAutenticacion { CONFIGURACION, CREDENCIALES, RED, PERFIL_AUSENTE, PERFIL_INVALIDO, SESION_CAMBIADA, GENERAL }

/** No retiene excepciones del SDK, correos, contraseñas, tokens ni mensajes internos como causa. */
class ErrorAutenticacion(val tipo: TipoErrorAutenticacion) : Exception(
    when (tipo) {
        TipoErrorAutenticacion.CONFIGURACION -> "Firebase aún no está configurado para SmartCatalog."
        TipoErrorAutenticacion.CREDENCIALES -> "No se pudo iniciar sesión. Verifica tus credenciales."
        TipoErrorAutenticacion.RED -> "No se pudo verificar la cuenta con el servidor. Revisa tu conexión e inténtalo nuevamente."
        TipoErrorAutenticacion.PERFIL_AUSENTE, TipoErrorAutenticacion.PERFIL_INVALIDO -> "La cuenta no tiene un perfil autorizado. Consulta al responsable del laboratorio."
        TipoErrorAutenticacion.SESION_CAMBIADA -> "La sesión cambió durante la verificación. Inténtalo nuevamente."
        TipoErrorAutenticacion.GENERAL -> "No se pudo verificar la sesión. Inténtalo nuevamente."
    }
)
