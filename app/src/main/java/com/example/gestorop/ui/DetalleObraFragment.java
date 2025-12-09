package com.example.gestorop.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class DetalleObraFragment extends Fragment {

    // Vistas
    private TextView tvTitulo, tvUbicacion;
    private AutoCompleteTextView spinnerResidente; // El ComboBox
    private Button btnGuardar;

    // Firebase
    private FirebaseFirestore db;

    // Variables de datos
    private String obraId, nombreObra, ubicacionObra;

    // Listas para manejar el ComboBox
    private ArrayList<String> listaCorreosResidentes = new ArrayList<>(); // Lo que se ve
    private ArrayList<String> listaIdsResidentes = new ArrayList<>();     // Lo que se guarda
    private String idResidenteSeleccionado = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recibimos los datos que nos envió la pantalla anterior (Mis Obras)
        if (getArguments() != null) {
            obraId = getArguments().getString("OBRA_ID");
            nombreObra = getArguments().getString("OBRA_NOMBRE");
            ubicacionObra = getArguments().getString("OBRA_UBICACION");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_obra, container, false);

        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        tvTitulo = view.findViewById(R.id.tvTituloDetalle);
        tvUbicacion = view.findViewById(R.id.tvUbicacionDetalle);
        spinnerResidente = view.findViewById(R.id.spinnerResidente);
        btnGuardar = view.findViewById(R.id.btnAsignarResidente);

        // Mostrar la información básica de la obra
        tvTitulo.setText(nombreObra);
        tvUbicacion.setText(ubicacionObra);

        // Cargar la lista de residentes en el combo box
        cargarListaDeResidentes();

        // Acción del botón guardar
        btnGuardar.setOnClickListener(v -> guardarAsignacion());

        return view;
    }

    private void cargarListaDeResidentes() {
        // Consultamos SOLO usuarios con rol "Residente"
        db.collection("users")
                .whereEqualTo("rol", "RESIDENTE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    listaCorreosResidentes.clear();
                    listaIdsResidentes.clear();

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String email = document.getString("email");
                            String id = document.getId();

                            if (email != null) {
                                listaCorreosResidentes.add(email); // Agregamos al visual
                                listaIdsResidentes.add(id);        // Agregamos al oculto
                            }
                        }

                        // Crear el adaptador para mostrar los datos en el dropdown
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                listaCorreosResidentes
                        );
                        spinnerResidente.setAdapter(adapter);

                        // Evento: Cuando seleccionan uno de la lista
                        spinnerResidente.setOnItemClickListener((parent, view, position, id) -> {
                            // Guardamos el ID correspondiente a la posición seleccionada
                            idResidenteSeleccionado = listaIdsResidentes.get(position);
                        });

                    } else {
                        Toast.makeText(getContext(), "No se encontraron residentes registrados.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar residentes", Toast.LENGTH_SHORT).show();
                    Log.e("DetalleObra", "Error Firebase", e);
                });
    }

    private void guardarAsignacion() {
        if (idResidenteSeleccionado.isEmpty()) {
            spinnerResidente.setError("Por favor selecciona un residente");
            spinnerResidente.requestFocus(); // Abrir o enfocar
            return;
        } else {
            spinnerResidente.setError(null);
        }

        // Actualizamos el documento de la obra en Firebase
        // Solo modificamos el campo "residenteId"
        db.collection("obras").document(obraId)
                .update("residenteId", idResidenteSeleccionado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Residente asignado correctamente", Toast.LENGTH_SHORT).show();

                    // Volver atrás automáticamente a la lista de obras
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al asignar residente", Toast.LENGTH_SHORT).show();
                });
    }
}