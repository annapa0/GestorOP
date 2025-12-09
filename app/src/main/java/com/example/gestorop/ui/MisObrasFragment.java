package com.example.gestorop.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.gestorop.model.Sesion;
import com.example.gestorop.model.Obra; // Importando la clase que acabas de arreglar

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MisObrasFragment extends Fragment {

    private RecyclerView recyclerView;
    private com.example.gestorop.Adapters.ObrasAdapter adapter;
    private List<Obra> listaObras;
    private ProgressBar progressBar;
    private TextView txtVacio;

    private FirebaseFirestore db;

    public MisObrasFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_obras, container, false);

        db = FirebaseFirestore.getInstance();

        // 1. Vincular Vistas
        recyclerView = view.findViewById(R.id.recyclerViewObras);
        progressBar = view.findViewById(R.id.progressBar);
        txtVacio = view.findViewById(R.id.txtVacio);

        // 2. Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listaObras = new ArrayList<>();
        adapter = new com.example.gestorop.Adapters.ObrasAdapter(listaObras);
        recyclerView.setAdapter(adapter);

        // 3. Cargar datos
        cargarObrasAsignadas();

        return view;
    }

    private void cargarObrasAsignadas() {
        progressBar.setVisibility(View.VISIBLE);
        txtVacio.setVisibility(View.GONE);

        // A. Obtener datos de la Sesión
        String miIdUsuario = Sesion.obtenerId(requireContext());
        String miRol = Sesion.obtenerRol(requireContext());

        Log.d("MisObras", "Consultando obras para ID: " + miIdUsuario + " | Rol: " + miRol);

        // B. Definir la consulta según el Rol
        com.google.firebase.firestore.Query query;

        if (miRol.equalsIgnoreCase("Admin")) {
            // El Admin ve TODO
            query = db.collection("obras");
        } else {
            // Supervisor o Residente ven SOLO lo suyo
            query = db.collection("obras").whereEqualTo("supervisorId", miIdUsuario);
        }

        // C. Ejecutar la consulta
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    listaObras.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                Obra obra = new Obra();
                                obra.setId(document.getId());
                                obra.setNombre(document.getString("nombre"));
                                obra.setUbicacion(document.getString("ubicacion"));
                                obra.setEstatus(document.getString("estatus"));
                                obra.setSupervisorId(document.getString("supervisorId"));

                                // Fechas
                                obra.setFechaInicio(document.getDate("fechaInicio"));
                                obra.setFechaFin(document.getDate("fechaFin"));

                                // Coordenadas seguras
                                Object lat = document.get("latitud");
                                Object lon = document.get("longitud");

                                obra.setLatitud(lat != null ? String.valueOf(lat) : "0.0");
                                obra.setLongitud(lon != null ? String.valueOf(lon) : "0.0");

                                listaObras.add(obra);

                            } catch (Exception e) {
                                Log.e("MisObras", "Error al leer una obra: " + e.getMessage());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        txtVacio.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    Log.e("MisObras", "Error Firestore", e);
                });
    }
}