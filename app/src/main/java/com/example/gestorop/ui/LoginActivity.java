package com.example.gestorop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorop.MainActivity;
import com.example.myapplication.R;
// Importamos tu clase de Sesión
import com.example.gestorop.model.Sesion;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;

    // Instancias de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Conectar con la vista
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> validarDatos());

        // (Opcional) Verificar si ya hay sesión iniciada para no pedir login otra vez
        verificarSesionExistente();
    }

    private void verificarSesionExistente() {
        String rolGuardado = Sesion.obtenerRol(this);
        if (!rolGuardado.isEmpty() && mAuth.getCurrentUser() != null) {
            // Si ya hay datos y el usuario de Firebase sigue activo, pasamos directo
            irAlMenuPrincipal(
                    Sesion.obtenerId(this),
                    rolGuardado,
                    Sesion.obtenerEmail(this)
            );
        }
    }

    private void validarDatos() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        loginFirebase(email, password);
    }

    private void loginFirebase(String email, String password) {
        Toast.makeText(this, "Ingresando...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login correcto en Auth, ahora buscamos los datos en Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            obtenerDatosUsuario(user.getUid());
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void obtenerDatosUsuario(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        // IMPORTANTE: Asegúrate de que en Firebase el campo se llame "role" o "rol"
                        // En el fragmento anterior usamos "role", así que usaré ese aquí.
                        String rol = documentSnapshot.getString("role");
                        String email = documentSnapshot.getString("email");

                        // Validación extra por si usaste "rol" en vez de "role"
                        if (rol == null) {
                            rol = documentSnapshot.getString("rol");
                        }

                        if (rol != null) {
                            // ¡ÉXITO! Pasamos los 3 datos: ID, ROL y EMAIL
                            irAlMenuPrincipal(uid, rol, email);
                        } else {
                            Toast.makeText(this, "El usuario no tiene rol asignado en la BD.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut(); // Cerramos sesión porque falta el dato crítico
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en la colección 'users'.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void irAlMenuPrincipal(String id, String rol, String email) {
        // 1. Guardamos la sesión completa (ID, Rol, Email)
        Sesion.guardarSesion(this, id, rol, email);

        // 2. Configuramos el salto al MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        // 3. Borramos el historial para que no puedan volver al Login con "Atrás"
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 4. Iniciamos
        startActivity(intent);
        finish(); // Cerramos esta actividad
    }
}