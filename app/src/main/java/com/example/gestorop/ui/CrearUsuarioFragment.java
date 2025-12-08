package com.example.gestorop.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.gestorop.model.Rol;
import com.example.myapplication.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CrearUsuarioFragment extends Fragment {

    private EditText etNombre, etEmail, etPass;
    private AutoCompleteTextView listaRoles;
    private Button btnRegistrar;

    // Usaremos auth para obtener el usuario actual (admin)
    // Pero usaremos una instancia secundaria para crear el nuevo
    private FirebaseAuth auth;
    private FirebaseFirestore db;

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

        // Inicializar Firebase (Instancia principal)
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Roles para el dropdown
        String[] roles = {"ADMIN", "SUPERVISOR", "RESIDENTE"};

        // Adapter para el dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                roles
        );

        listaRoles.setAdapter(adapter);
        listaRoles.setText(roles[0], false);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        return view;
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email  = etEmail.getText().toString().trim();
        String pass   = etPass.getText().toString().trim();
        String rolTxt = listaRoles.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || rolTxt.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if(pass.length() < 6){
            Toast.makeText(getContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        Rol rol;
        try {
            rol = Rol.valueOf(rolTxt);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Rol no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- TRUCO PARA NO CERRAR SESIÓN DEL ADMIN ---

        // 1. Obtenemos las opciones de la app actual
        FirebaseOptions firebaseOptions = FirebaseApp.getInstance().getOptions();

        // 2. Definimos un nombre para la app secundaria
        String tempAppName = "SecondaryAppForRegistration";

        FirebaseApp tempApp;
        try {
            // Intentamos recuperar la app si ya existe
            tempApp = FirebaseApp.getInstance(tempAppName);
        } catch (IllegalStateException e) {
            // Si no existe, la inicializamos
            tempApp = FirebaseApp.initializeApp(requireContext(), firebaseOptions, tempAppName);
        }

        // 3. Obtenemos una instancia de Auth ligada a esa app secundaria
        FirebaseAuth tempAuth = FirebaseAuth.getInstance(tempApp);

        // 4. Creamos el usuario en la instancia secundaria
        tempAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Obtenemos el UID del NUEVO usuario creado
                        String nuevoUid = task.getResult().getUser().getUid();

                        // 5. Guardamos en Firestore usando la instancia PRINCIPAL (db)
                        //    (porque 'db' todavía tiene la sesión del admin y permisos de escritura)
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("id", nuevoUid);
                        userData.put("nombre", nombre);
                        userData.put("email", email);
                        userData.put("rol", rol.name());
                        userData.put("estado", "activo");

                        db.collection("users")
                                .document(nuevoUid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();

                                    // IMPORTANTE: Cerrar sesión en la instancia secundaria para limpiar
                                    tempAuth.signOut();

                                    limpiarCampos();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error al guardar en BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    // Si falla la BD, podrías querer borrar el usuario creado en Auth,
                                    // pero por ahora lo dejamos así.
                                    tempAuth.signOut();
                                });

                    } else {
                        Toast.makeText(getContext(), "Error al crear usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etEmail.setText("");
        etPass.setText("");
        listaRoles.setText(""); // Limpia la selección visual, pero mantiene el adapter
        listaRoles.clearFocus();
    }
}