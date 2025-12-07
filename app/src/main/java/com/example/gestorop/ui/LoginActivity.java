package com.example.gestorop.ui;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gestorop.MainActivity; // <--- Verifica este import
import com.example.myapplication.R;
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
        // Mostramos un Toast para que el usuario sepa que está cargando
        Toast.makeText(this, "Ingresando...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login correcto, ahora buscamos el rol
                        FirebaseUser user = mAuth.getCurrentUser();
                        obtenerRolUsuario(user.getUid());
                    } else {
                        // Error en login
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void obtenerRolUsuario(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String rol = documentSnapshot.getString("rol");
                        if (rol != null) {
                            irAlMenuPrincipal(rol);
                        } else {
                            Toast.makeText(this, "El usuario no tiene rol asignado.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Si el usuario existe en Auth pero no en Firestore
                        Toast.makeText(this, "Usuario no encontrado en la base de datos.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void irAlMenuPrincipal(String rol) {
        // Guardamos el rol para usarlo después (ej. para ocultar botones)
        // Por ahora lo pasamos directo al Main
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ROL_USUARIO", rol);

        // Estas flags evitan que el usuario pueda volver al Login con el botón "Atrás"
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}