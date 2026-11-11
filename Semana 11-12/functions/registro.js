"use strict";

const SMARTCATALOG_APP_ID = "1:890103965212:android:25ff41d45c9095c5fd21d9";

class ErrorRegistro extends Error {
    constructor(code, message) {
        super(message);
        this.code = code;
    }
}

// request.auth y request.app los verifica el SDK callable antes de llamar al handler.
function validarRegistro(request) {
    const { auth, app, data } = request;
    if (!auth?.uid) throw new ErrorRegistro("unauthenticated", "Inicia sesión.");
    if (app?.appId !== SMARTCATALOG_APP_ID) {
        throw new ErrorRegistro("permission-denied", "Solicitud fuera de SmartCatalog.");
    }
    const correo = auth.token?.email;
    if (auth.token?.email_verified !== true || typeof correo !== "string" || !correo) {
        throw new ErrorRegistro("failed-precondition", "Verifica tu correo.");
    }
    if (!/^[^/]{1,128}$/.test(auth.uid) || auth.uid === "." || auth.uid === ".." || /^__.*__$/.test(auth.uid)) {
        throw new ErrorRegistro("invalid-argument", "Identidad no válida.");
    }
    if (!data || Array.isArray(data) || Object.keys(data).length !== 1 || typeof data.nombre !== "string") {
        throw new ErrorRegistro("invalid-argument", "Envía únicamente el nombre.");
    }
    const nombre = data.nombre.trim();
    if (!nombre || nombre.length > 80) {
        throw new ErrorRegistro("invalid-argument", "El nombre debe tener entre 1 y 80 caracteres.");
    }
    return { uid: auth.uid, perfil: { nombre, correo, rol: "usuario" } };
}

async function finalizarRegistro(request, db) {
    const { uid, perfil } = validarRegistro(request);
    const referencia = db.collection("usuarios").doc(uid);
    return db.runTransaction(async (tx) => {
        const actual = await tx.get(referencia);
        if (actual.exists) {
            const datos = actual.data();
            if (datos.rol !== "usuario" || typeof datos.correo !== "string" ||
                datos.correo.trim().toLowerCase() !== perfil.correo.toLowerCase() ||
                typeof datos.nombre !== "string" || !datos.nombre.trim() || datos.nombre.trim().length > 80) {
                throw new ErrorRegistro("already-exists", "El perfil existente requiere revisión.");
            }
            return { estado: "existente" };
        }
        tx.create(referencia, perfil);
        return { estado: "creado" };
    });
}

module.exports = { ErrorRegistro, SMARTCATALOG_APP_ID, finalizarRegistro };
