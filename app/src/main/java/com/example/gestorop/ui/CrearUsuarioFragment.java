package com.example.gestorop.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gestorop.model.Rol;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.example.myapplication.R;

import java.util.HashMap;
import java.util.Map;

public class CrearUsuarioFragment extends Fragment {

    private EditText etNombre, etEmail, etPass;
    private AutoCompleteTextView listaRoles;
    private Button btnRegistrar;

    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crear_usuario, container, false);

        // UI components

        etNombre = view.findViewById(R.id.etNombre);
        etEmail  = view.findViewById(R.id.etEmail);
        etPass   = view.findViewById(R.id.etPass);
        listaRoles = view.findViewById(R.id.listaRoles);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);

        // Firebase
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        // Roles para dropdown
        String[] roles = {"ADMIN", "SUPERVISOR", "RESIDENTE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                roles
        );
        listaRoles.setAdapter(adapter);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        return view;
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email  = etEmail.getText().toString().trim();
        String pass   = etPass.getText().toString().trim();
        String rolTxt = listaRoles.getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || rolTxt.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir rol a enum
        Rol rol;
        try {
            rol = Rol.valueOf(rolTxt);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Rol no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String uid = auth.getCurrentUser().getUid();

                        // Datos del usuario
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", uid);
                        userData.put("nombre", nombre);
                        userData.put("email", email);
                        userData.put("rol", rol.name());
                        userData.put("estado", "activo");

                        // Guardar en Firebase Realtime Database
                        dbRef.child(uid)
                                .setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(),
                                            "Usuario registrado correctamente",
                                            Toast.LENGTH_SHORT).show();

                                    // Limpiar campos
                                    etNombre.setText("");
                                    etEmail.setText("");
                                    etPass.setText("");
                                    listaRoles.setText("");

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Error al guardar: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()
                                );

                    } else {
                        Toast.makeText(getContext(),
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
