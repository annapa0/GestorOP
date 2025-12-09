package com.example.gestorop.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.gestorop.model.Avance;
import com.example.gestorop.Adapters.AvancesAdapter;
import com.example.gestorop.model.Sesion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListaAvancesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AvancesAdapter adapter;
    private List<Avance> listaAvances;
    private String obraId;
    private TextView tvVacio;

    private FirebaseFirestore db;

    public ListaAvancesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recibir el ID de la obra desde el fragmento anterior
        if (getArguments() != null) {
            obraId = getArguments().getString("OBRA_ID");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Reutilizamos el diseño de lista (asegúrate de que tenga recyclerViewObras y txtVacio)
        View view = inflater.inflate(R.layout.fragment_mis_obras, container, false);

        db = FirebaseFirestore.getInstance();

        // 1. Configurar Título (Opcional, visual)
        TextView titulo = view.findViewById(R.id.tituloLista); // Asegúrate que este ID exista en tu XML, si no, comenta esta línea
        if(titulo != null) titulo.setText("Bitácora de Avances");

        // 2. Vincular Vistas
        tvVacio = view.findViewById(R.id.txtVacio);
        recyclerView = view.findViewById(R.id.recyclerViewObras);

        // 3. Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listaAvances = new ArrayList<>();
        adapter = new AvancesAdapter(getContext(), listaAvances);
        recyclerView.setAdapter(adapter);

        // 4. Cargar Datos
        if (obraId != null) {
            cargarAvances();
        } else {
            Toast.makeText(getContext(), "Error: No se recibió ID de obra", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void cargarAvances() {
        String miRol = Sesion.obtenerRol(requireContext());
        String miId = Sesion.obtenerId(requireContext());

        Log.d("ListaAvances", "Consultando avances para Obra: " + obraId + " | Rol: " + miRol);

        Query query;

        // --- DEFINICIÓN DE LA CONSULTA ---
        // Base: Todos los avances de esta obra, ordenados por fecha (más reciente primero)
        query = db.collection("avances")
                .whereEqualTo("obraId", obraId)
                .orderBy("fechaRegistro", Query.Direction.DESCENDING);

        // Filtro adicional: Si es Residente, solo ve sus propios avances
        if (miRol.equalsIgnoreCase("Residente")) {
            query = query.whereEqualTo("usuarioId", miId);
        }

        // --- EJECUCIÓN ---
        query.get()
                .addOnSuccessListener(snapshots -> {
                    // [DEPURACIÓN] Muestra cuántos datos llegaron
                    int cantidad = snapshots.size();
                    // Toast.makeText(getContext(), "Se encontraron " + cantidad + " avances", Toast.LENGTH_SHORT).show();
                    Log.d("ListaAvances", "Datos encontrados: " + cantidad);

                    listaAvances.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        try {
                            Avance avance = doc.toObject(Avance.class);
                            avance.setId(doc.getId()); // Guardar el ID del documento para poder editarlo luego
                            listaAvances.add(avance);
                        } catch (Exception e) {
                            Log.e("ListaAvances", "Error al convertir documento: " + e.getMessage());
                        }
                    }

                    if (listaAvances.isEmpty()) {
                        tvVacio.setText("No hay avances registrados aún.");
                        tvVacio.setVisibility(View.VISIBLE);
                    } else {
                        tvVacio.setVisibility(View.GONE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // [ERROR] Aquí caerá si falta el índice o hay error de red
                    String errorMsg = e.getMessage();
                    Log.e("ListaAvances", "Error Firestore", e);

                    if (errorMsg != null && errorMsg.contains("index")) {
                        Toast.makeText(getContext(), "Falta crear índice en Firebase (Revisa Logcat)", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Error al cargar: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}