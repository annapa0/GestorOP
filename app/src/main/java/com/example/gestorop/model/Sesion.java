package com.example.gestorop.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Sesion {

    private static final String PREF_NAME = "SesionApp";
    private static final String KEY_ROL = "rol_usuario";

    // Guardar el rol
    public static void guardarRol(Context context, String rol) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROL, rol).apply();
    }

    // Leer el rol (úsalo en cualquier Activity o Fragment)
    public static String obtenerRol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROL, ""); // Devuelve "" si no hay datos
    }

    // Borrar datos (Logout)
    public static void cerrarSesion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}