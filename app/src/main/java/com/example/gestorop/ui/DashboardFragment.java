package com.example.gestorop.ui;
import android.os.Bundle;
/*import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestorop.model.Obra;
import com.example.gestorop.ui.adapters.ObraAdapter;
import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private ObraAdapter adapter;
    private List<Obra> listaObras;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Conectamos con el diseño XML
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Inicializamos Firebase
        db = FirebaseFirestore.getInstance();

        // Configuramos la lista
        recyclerView = view.findViewById(R.id.recyclerViewObras);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaObras = new ArrayList<>();
        adapter = new ObraAdapter(listaObras);
        recyclerView.setAdapter(adapter);

        // Llamamos a la función que descarga los datos
        cargarObras();

        return view;
    }

    private void cargarObras() {
        db.collection("obras") // <--- El nombre de tu colección en Firebase
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaObras.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convierte el documento JSON a un objeto Obra Java
                            Obra obra = document.toObject(Obra.class);
                            obra.setId(document.getId());
                            listaObras.add(obra);
                        }
                        adapter.notifyDataSetChanged(); // ¡Avisar que llegaron datos!
                    } else {
                        Toast.makeText(getContext(), "Error al cargar obras", Toast.LENGTH_SHORT).show();
                    }
                });
    }
} */