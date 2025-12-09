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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorop.Adapters.ObrasAdapter;
import com.example.myapplication.R;
import com.example.gestorop.model.Obra;
import com.example.gestorop.model.Sesion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MisObrasFragment extends Fragment {

    private RecyclerView recyclerView;
    private ObrasAdapter adapter;
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

        // 2. Configurar RecyclerView e Inicializar lista
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listaObras = new ArrayList<>(); // ¡Importante inicializar aquí!

        // Configurar Adapter
        // En MisObrasFragment.java, dentro de cargarObrasAsignadas / adapter...

        adapter = new ObrasAdapter(listaObras, obra -> {
            Bundle bundle = new Bundle();
            // Datos básicos
            bundle.putString("OBRA_ID", obra.getId());
            bundle.putString("OBRA_NOMBRE", obra.getNombre());
            bundle.putString("OBRA_UBICACION", obra.getUbicacion());

            // Datos para el Mapa
            bundle.putString("OBRA_LAT", obra.getLatitud());
            bundle.putString("OBRA_LNG", obra.getLongitud());

            // Datos de Usuarios
            bundle.putString("OBRA_SUPERVISOR_ID", obra.getSupervisorId());
            bundle.putString("OBRA_RESIDENTE_ID", obra.getResidenteId());

            // Datos de Fechas (Formateadas)
            android.text.format.DateFormat df = new android.text.format.DateFormat();
            String inicio = (obra.getFechaInicio() != null) ? df.format("dd/MM/yyyy", obra.getFechaInicio()).toString() : "Pendiente";
            String fin = (obra.getFechaFin() != null) ? df.format("dd/MM/yyyy", obra.getFechaFin()).toString() : "Pendiente";

            bundle.putString("OBRA_FECHA_INICIO", inicio);
            bundle.putString("OBRA_FECHA_FIN", fin);

            try {
                Navigation.findNavController(requireView())
                        .navigate(R.id.detalleObraFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error navegación", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        // 3. Cargar datos
        cargarObrasAsignadas();

        return view;
    }

    private void cargarObrasAsignadas() {
        progressBar.setVisibility(View.VISIBLE);
        txtVacio.setVisibility(View.GONE);

        if (listaObras == null) listaObras = new ArrayList<>();

        // Obtener ID y ROL de la sesión actual
        String miIdUsuario = Sesion.obtenerId(requireContext());
        String miRol = Sesion.obtenerRol(requireContext());

        Log.d("MisObras", "ID: " + miIdUsuario + " | Rol: " + miRol);

        // --- LÓGICA DE FILTRADO POR ROL (AQUÍ ESTÁ EL CAMBIO) ---
        com.google.firebase.firestore.Query query;

        if (miRol.equalsIgnoreCase("Admin")) {
            // 1. El Admin ve TODO
            query = db.collection("obras");

        } else if (miRol.equalsIgnoreCase("Residente")) {
            // 2. El Residente ve donde 'residenteId' sea igual a su ID
            query = db.collection("obras").whereEqualTo("residenteId", miIdUsuario);

        } else {
            // 3. El Supervisor (por defecto) ve donde 'supervisorId' sea igual a su ID
            query = db.collection("obras").whereEqualTo("supervisorId", miIdUsuario);
        }

        // Ejecutar consulta
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    listaObras.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                // Mapeo manual seguro
                                Obra obra = new Obra();
                                obra.setId(document.getId());
                                obra.setNombre(document.getString("nombre"));
                                obra.setUbicacion(document.getString("ubicacion"));
                                obra.setEstatus(document.getString("estatus"));
                                obra.setSupervisorId(document.getString("supervisorId"));
                                obra.setResidenteId(document.getString("residenteId"));

                                obra.setFechaInicio(document.getDate("fechaInicio"));
                                obra.setFechaFin(document.getDate("fechaFin"));

                                Object lat = document.get("latitud");
                                Object lon = document.get("longitud");
                                obra.setLatitud(lat != null ? String.valueOf(lat) : "0.0");
                                obra.setLongitud(lon != null ? String.valueOf(lon) : "0.0");

                                listaObras.add(obra);

                            } catch (Exception e) {
                                Log.e("MisObras", "Error mapeo: " + e.getMessage());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        txtVacio.setVisibility(View.VISIBLE);
                        // Mensaje personalizado según rol si está vacío
                        if (miRol.equalsIgnoreCase("Residente")) {
                            txtVacio.setText("No te han asignado obras todavía.");
                        } else {
                            txtVacio.setText("No hay obras disponibles.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}