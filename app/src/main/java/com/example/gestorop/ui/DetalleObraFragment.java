package com.example.gestorop.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.gestorop.model.Sesion;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class DetalleObraFragment extends Fragment implements OnMapReadyCallback {

    // Vistas de Información
    private TextView tvTitulo, tvUbicacion, tvInicio, tvFin, tvSupervisor, tvResidente;

    // Vistas de Supervisión (Asignación)
    private LinearLayout layoutAsignacion;
    private AutoCompleteTextView spinnerResidente;
    private Button btnGuardarAsignacion;

    // Vistas de Residente (Registro)
    private Button btnRegistrarAvance;

    // Mapa
    private MapView mapView;
    private GoogleMap gMap;

    // Firebase
    private FirebaseFirestore db;

    // Datos recibidos
    private String obraId, nombre, ubicacion, latStr, lngStr, supId, resId, fInicio, fFin;

    // Variables lógicas
    private ArrayList<String> listaCorreos = new ArrayList<>();
    private ArrayList<String> listaIds = new ArrayList<>();
    private String nuevoResidenteId = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Recibir todos los datos del Bundle
        if (getArguments() != null) {
            obraId = getArguments().getString("OBRA_ID");
            nombre = getArguments().getString("OBRA_NOMBRE");
            ubicacion = getArguments().getString("OBRA_UBICACION");
            latStr = getArguments().getString("OBRA_LAT");
            lngStr = getArguments().getString("OBRA_LNG");
            supId = getArguments().getString("OBRA_SUPERVISOR_ID");
            resId = getArguments().getString("OBRA_RESIDENTE_ID");
            fInicio = getArguments().getString("OBRA_FECHA_INICIO");
            fFin = getArguments().getString("OBRA_FECHA_FIN");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_obra, container, false);
        db = FirebaseFirestore.getInstance();

        // 1. Vincular Vistas
        tvTitulo = view.findViewById(R.id.tvTituloDetalle);
        tvUbicacion = view.findViewById(R.id.tvUbicacionDetalle);
        tvInicio = view.findViewById(R.id.tvFechaInicio);
        tvFin = view.findViewById(R.id.tvFechaFin);
        tvSupervisor = view.findViewById(R.id.tvSupervisorInfo);
        tvResidente = view.findViewById(R.id.tvResidenteInfo);

        // Panel de Supervisor
        layoutAsignacion = view.findViewById(R.id.layoutAsignacion);
        spinnerResidente = view.findViewById(R.id.spinnerResidente);
        btnGuardarAsignacion = view.findViewById(R.id.btnAsignarResidente);

        // Botón de Residente
        btnRegistrarAvance = view.findViewById(R.id.btnRegistrarAvance);

        // Configuración Mapa
        mapView = view.findViewById(R.id.mapViewDetalle);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 2. Llenar Textos
        if (nombre != null) tvTitulo.setText(nombre);
        if (ubicacion != null) tvUbicacion.setText(ubicacion);
        if (fInicio != null) tvInicio.setText("Inicio: " + fInicio);
        if (fFin != null) tvFin.setText("Fin: " + fFin);

        // Buscar nombres de usuarios
        buscarNombreUsuario(supId, tvSupervisor, "Supervisor: ");
        buscarNombreUsuario(resId, tvResidente, "Residente: ");

        // 3. Lógica de Roles
        String miRol = Sesion.obtenerRol(requireContext());

        if (miRol.equalsIgnoreCase("Residente")) {
            // -- MODO RESIDENTE --
            layoutAsignacion.setVisibility(View.GONE); // No puede asignar
            btnRegistrarAvance.setVisibility(View.VISIBLE); // Puede reportar

            btnRegistrarAvance.setOnClickListener(v -> irACapturaDeAvance());

        } else {
            // -- MODO SUPERVISOR / ADMIN --
            layoutAsignacion.setVisibility(View.VISIBLE); // Puede asignar
            btnRegistrarAvance.setVisibility(View.GONE); // No reporta desde aquí

            cargarListaResidentes(); // Llenar combo box

            // Forzar apertura del combo box al tocarlo
            spinnerResidente.setOnClickListener(v -> {
                if(spinnerResidente.getAdapter() != null) spinnerResidente.showDropDown();
            });

            btnGuardarAsignacion.setOnClickListener(v -> guardarCambiosAsignacion());
        }

        return view;
    }

    // --- NAVEGACIÓN A CAPTURA ---
    private void irACapturaDeAvance() {
        Bundle bundle = new Bundle();
        bundle.putString("OBRA_ID", obraId);
        bundle.putString("OBRA_NOMBRE", nombre);
        // ¡IMPORTANTE! Enviamos las coordenadas para la validación GPS
        bundle.putString("OBRA_LAT", latStr);
        bundle.putString("OBRA_LNG", lngStr);

        try {
            Navigation.findNavController(requireView())
                    .navigate(R.id.capturaAvanceFragment, bundle);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error navegación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- MAPA (SOLO LECTURA) ---
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        try {
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);

            if (lat != 0.0 && lng != 0.0) {
                LatLng pos = new LatLng(lat, lng);
                gMap.addMarker(new MarkerOptions().position(pos).title("Ubicación de Obra"));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
            }
            // Habilitar gestos básicos
            gMap.getUiSettings().setScrollGesturesEnabled(true);
            gMap.getUiSettings().setZoomGesturesEnabled(true);
        } catch (Exception e) {
            Log.e("Mapa", "Error coordenadas", e);
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void buscarNombreUsuario(String uid, TextView textView, String prefijo) {
        if (uid == null || uid.isEmpty()) {
            textView.setText(prefijo + "Sin asignar");
            return;
        }
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String email = doc.getString("email");
                if (email == null) email = doc.getString("nombre"); // Fallback
                textView.setText(prefijo + (email != null ? email : "Desconocido"));
            }
        });
    }

    private void cargarListaResidentes() {
        // Busca usuarios con rol 'Residente' (ajusta mayúsculas según tu DB)
        db.collection("users").whereEqualTo("rol", "RESIDENTE").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaCorreos.clear();
                    listaIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String email = doc.getString("email");
                        if (email != null) {
                            listaCorreos.add(email);
                            listaIds.add(doc.getId());
                        }
                    }
                    if (getContext() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, listaCorreos);
                        spinnerResidente.setAdapter(adapter);
                    }

                    spinnerResidente.setOnItemClickListener((p, v, pos, id) -> nuevoResidenteId = listaIds.get(pos));
                });
    }

    private void guardarCambiosAsignacion() {
        if (nuevoResidenteId.isEmpty()) {
            spinnerResidente.setError("Selecciona uno");
            return;
        }
        db.collection("obras").document(obraId).update("residenteId", nuevoResidenteId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Asignado correctamente!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show());
    }

    // --- CICLO DE VIDA MAPA ---
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}