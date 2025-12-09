package com.example.gestorop.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Sesion {

    private static final String PREF_NAME = "SesionApp";

    // Claves para los datos
    private static final String KEY_ID = "id_usuario";       // <--- NUEVO
    private static final String KEY_ROL = "rol_usuario";
    private static final String KEY_EMAIL = "email_usuario"; // <--- NUEVO

    // --- GUARDAR SESIÓN COMPLETA (LOGIN) ---
    public static void guardarSesion(Context context, String id, String rol, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_ID, id);       // Guardamos ID
        editor.putString(KEY_ROL, rol);     // Guardamos Rol
        editor.putString(KEY_EMAIL, email); // Guardamos Email

        editor.apply();
    }

    // --- OBTENER DATOS INDIVIDUALES ---

    // Obtener ID (Para filtrar obras)
    public static String obtenerId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ID, "");
    }

    // Obtener ROL (Para ocultar botones)
    public static String obtenerRol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROL, "");
    }

    // Obtener EMAIL (Para mostrar en perfil)
    public static String obtenerEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, "");
    }

    // --- CERRAR SESIÓN ---
    public static void cerrarSesion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply(); // Borra ID, Rol y Email
    }
}