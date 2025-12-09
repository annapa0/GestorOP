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
    private List<Obra> listaObras; // Esta es la variable que estaba null
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

        // --- CORRECCIÓN: INICIALIZAR LA LISTA AQUÍ ---
        listaObras = new ArrayList<>();
        // ---------------------------------------------

        // Configurar el Adaptador con el evento de Clic para ir al Detalle
        adapter = new ObrasAdapter(listaObras, obra -> {
            // Al hacer clic, enviamos los datos al fragmento de detalle
            Bundle bundle = new Bundle();
            bundle.putString("OBRA_ID", obra.getId());
            bundle.putString("OBRA_NOMBRE", obra.getNombre());
            bundle.putString("OBRA_UBICACION", obra.getUbicacion());

            // Navegar al detalle (Asegúrate de tener la acción o el fragmento en navigation.xml)
            try {
                Navigation.findNavController(requireView())
                        .navigate(R.id.detalleObraFragment, bundle);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error de navegación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        // Seguridad extra: si por alguna razón la lista es null, la creamos
        if (listaObras == null) {
            listaObras = new ArrayList<>();
        }

        String miIdUsuario = Sesion.obtenerId(requireContext());
        String miRol = Sesion.obtenerRol(requireContext());

        Log.d("MisObras", "ID: " + miIdUsuario + " | Rol: " + miRol);

        com.google.firebase.firestore.Query query;

        if (miRol.equalsIgnoreCase("Admin")) {
            query = db.collection("obras");
        } else {
            // Supervisor ve solo sus obras
            query = db.collection("obras").whereEqualTo("supervisorId", miIdUsuario);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    // AQUÍ OCURRÍA EL ERROR ANTES
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
                                obra.setResidenteId(document.getString("residenteId")); // Nuevo campo

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
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}