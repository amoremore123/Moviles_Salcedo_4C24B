"use strict";

const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { ErrorRegistro, finalizarRegistro } = require("./registro");

initializeApp();

exports.finalizarRegistroSmartCatalog = onCall({ enforceAppCheck: true }, async (request) => {
    try {
        return await finalizarRegistro(request, getFirestore());
    } catch (error) {
        if (error instanceof ErrorRegistro) throw new HttpsError(error.code, error.message);
        throw new HttpsError("internal", "No se pudo confirmar el perfil. Intenta nuevamente.");
    }
});
