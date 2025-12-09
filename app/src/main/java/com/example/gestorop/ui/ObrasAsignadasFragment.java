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

import com.example.gestorop.Adapters.ObrasAdapter;
import com.example.myapplication.R;
import com.example.gestorop.model.Obra;
import com.example.gestorop.model.Sesion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ObrasAsignadasFragment extends Fragment {

    private RecyclerView recyclerView;
    private ObrasAdapter adapter;
    private List<Obra> listaObras;
    private ProgressBar progressBar;
    private TextView txtVacio;
    private FirebaseFirestore db;

    public ObrasAsignadasFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Asegúrate de crear este XML o reutilizar fragment_mis_obras
        View view = inflater.inflate(R.layout.fragment_mis_obras, container, false);

        // Cambiamos el título visualmente si reutilizas el XML
        TextView titulo = view.findViewById(R.id.tituloLista); // Asegúrate de tener este ID en el XML
        if(titulo != null) titulo.setText("Obras Asignadas a Residentes");

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewObras);
        progressBar = view.findViewById(R.id.progressBar);
        txtVacio = view.findViewById(R.id.txtVacio);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listaObras = new ArrayList<>();

        // Aquí no ponemos listener de click porque es solo para visualizar (o puedes ponerlo si quieres editar)
        adapter = new ObrasAdapter(listaObras, null);
        recyclerView.setAdapter(adapter);

        cargarObrasConResidente();

        return view;
    }

    private void cargarObrasConResidente() {
        progressBar.setVisibility(View.VISIBLE);
        txtVacio.setVisibility(View.GONE);
        listaObras.clear();

        String miId = Sesion.obtenerId(requireContext());

        // Buscamos las obras donde YO soy el supervisor
        db.collection("obras")
                .whereEqualTo("supervisorId", miId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // FILTRO MANUAL: Solo agregamos si YA TIENE RESIDENTE
                            String residenteId = document.getString("residenteId");

                            if (residenteId != null && !residenteId.isEmpty()) {
                                // Mapeo manual
                                Obra obra = new Obra();
                                obra.setId(document.getId());
                                obra.setNombre(document.getString("nombre"));
                                obra.setUbicacion(document.getString("ubicacion"));
                                obra.setEstatus(document.getString("estatus"));
                                obra.setSupervisorId(document.getString("supervisorId"));
                                obra.setResidenteId(residenteId);
                                // ... fechas y coordenadas ...

                                listaObras.add(obra);
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    }

                    if (listaObras.isEmpty()) {
                        txtVacio.setText("Aún no has asignado ninguna obra.");
                        txtVacio.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar", Toast.LENGTH_SHORT).show());
    }
}