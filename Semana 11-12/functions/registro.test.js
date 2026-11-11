"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");
const { finalizarRegistro } = require("./registro");

function solicitud() {
    return {
        auth: { uid: "uid-prueba", token: { email: "vista@example.invalid", email_verified: true } },
        app: { appId: "1:890103965212:android:25ff41d45c9095c5fd21d9" },
        data: { nombre: " Nombre de prueba " }
    };
}

function almacen() {
    const perfiles = new Map();
    return {
        perfiles,
        collection(nombre) {
            assert.equal(nombre, "usuarios");
            return { doc: (id) => id };
        },
        runTransaction(trabajo) {
            return trabajo({
                get: async (id) => ({ exists: perfiles.has(id), data: () => perfiles.get(id) }),
                create: (id, datos) => perfiles.set(id, datos)
            });
        }
    };
}

test("crea solo el perfil propio como usuario y un reintento no modifica el perfil", async () => {
    const db = almacen();
    const request = solicitud();
    assert.deepEqual(await finalizarRegistro(request, db), { estado: "creado" });
    assert.deepEqual(db.perfiles.get("uid-prueba"), {
        nombre: "Nombre de prueba", correo: "vista@example.invalid", rol: "usuario"
    });
    request.data.nombre = "Otro nombre";
    assert.deepEqual(await finalizarRegistro(request, db), { estado: "existente" });
    assert.equal(db.perfiles.get("uid-prueba").nombre, "Nombre de prueba");
});

test("rechaza solicitudes sin Auth, sin App Check o desde otra app", async () => {
    const db = almacen();
    for (const [cambios, code] of [
        [{ auth: null }, "unauthenticated"],
        [{ app: null }, "permission-denied"],
        [{ app: { appId: "1:890103965212:android:otraApp" } }, "permission-denied"]
    ]) {
        await assert.rejects(finalizarRegistro({ ...solicitud(), ...cambios }, db), { code });
    }
    assert.equal(db.perfiles.size, 0);
});

test("rechaza rol admin, UID ajeno y correo enviados por el cliente", async () => {
    const db = almacen();
    for (const campo of [{ rol: "admin" }, { uid: "ajeno" }, { correo: "ajeno@example.invalid" }]) {
        const request = solicitud();
        Object.assign(request.data, campo);
        await assert.rejects(finalizarRegistro(request, db), { code: "invalid-argument" });
    }
    assert.equal(db.perfiles.size, 0);
});

test("rechaza correo sin verificar, nombre inválido y UID que alteraría la ruta", async () => {
    const db = almacen();
    const request = solicitud();
    request.auth.token.email_verified = false;
    await assert.rejects(finalizarRegistro(request, db), { code: "failed-precondition" });
    await assert.rejects(finalizarRegistro({ ...solicitud(), data: { nombre: " " } }, db), { code: "invalid-argument" });
    const identidad = solicitud().auth;
    identidad.uid = "otro/perfil";
    await assert.rejects(finalizarRegistro({ ...solicitud(), auth: identidad }, db), { code: "invalid-argument" });
    assert.equal(db.perfiles.size, 0);
});

test("un perfil administrativo existente se conserva intacto", async () => {
    const db = almacen();
    db.perfiles.set("uid-prueba", { nombre: "Admin", correo: "vista@example.invalid", rol: "admin" });
    await assert.rejects(finalizarRegistro(solicitud(), db), { code: "already-exists" });
    assert.equal(db.perfiles.get("uid-prueba").rol, "admin");
});
