package com.example.gestorop.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.gestorop.model.Obra;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CrearObraFragment extends Fragment implements OnMapReadyCallback {

    // Vistas
    private TextInputEditText etNombre, etUbicacion, etFechaInicio, etFechaFin;
    private Button btnGuardar;
    private ImageButton btnBuscar; // Botón Lupa
    private ProgressBar progressBar;

    // Mapa
    private MapView mapView;
    private GoogleMap gMap;
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;

    // Firebase y Datos
    private FirebaseFirestore db;
    private final Calendar calendar = Calendar.getInstance();
    private String latitudActual = "0.0";
    private String longitudActual = "0.0";
    private static final int PERMISSION_REQUEST_CODE = 100;

    public CrearObraFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_obra, container, false);

        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        etNombre = view.findViewById(R.id.etNombreObra);
        etUbicacion = view.findViewById(R.id.etUbicacionObra);
        etFechaInicio = view.findViewById(R.id.etFechaInicio);
        etFechaFin = view.findViewById(R.id.etFechaFin);
        btnGuardar = view.findViewById(R.id.btnGuardarObra);
        btnBuscar = view.findViewById(R.id.btnBuscarEnMapa); // La Lupa
        progressBar = view.findViewById(R.id.progressBar);
        mapView = view.findViewById(R.id.mapView);

        // Inicializar Mapa y Cliente de Ubicación
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity());

        // Listeners
        etFechaInicio.setOnClickListener(v -> mostrarCalendario(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarCalendario(etFechaFin));
        btnGuardar.setOnClickListener(v -> validarYGuardarObra());

        // Listener para buscar dirección escrita
        btnBuscar.setOnClickListener(v -> buscarDireccionEscrita());

        return view;
    }

    // ==========================================
    // LÓGICA DEL MAPA
    // ==========================================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        verificarPermisoYCentrar();
        habilitarMovimientoDePin(); // Activar click en mapa -> Texto
    }

    // 1. Centrar mapa en GPS al iniciar
    private void verificarPermisoYCentrar() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                gMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 15f));

                        // Solo si el campo está vacío, llenamos con la ubicación actual
                        if (etUbicacion.getText().toString().isEmpty()) {
                            actualizarMarcadorYTexto(miUbicacion, false); // false = no mover cámara otra vez
                        }
                    }
                });
            } catch (SecurityException e) { Log.e("Mapa", "Error seguridad", e); }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    // 2. Click en Mapa -> Obtener Dirección (Reverse Geocoding)
    private void habilitarMovimientoDePin() {
        if (gMap == null) return;
        gMap.setOnMapClickListener(latLng -> {
            actualizarMarcadorYTexto(latLng, true);
        });
    }

    private void actualizarMarcadorYTexto(LatLng latLng, boolean moverCamara) {
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(latLng).title("Seleccionado"));
        if (moverCamara) {
            gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        latitudActual = String.valueOf(latLng.latitude);
        longitudActual = String.valueOf(latLng.longitude);

        // Hilo secundario para traducir coordenadas a texto
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String direccion = addresses.get(0).getAddressLine(0);
                    requireActivity().runOnUiThread(() -> etUbicacion.setText(direccion));
                }
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    // 3. Escribir Texto + Botón Lupa -> Mover Mapa (Geocoding)
    private void buscarDireccionEscrita() {
        String direccion = etUbicacion.getText().toString().trim();
        if (TextUtils.isEmpty(direccion)) {
            etUbicacion.setError("Escribe una dirección");
            return;
        }

        // Ocultar teclado
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etUbicacion.getWindowToken(), 0);

        Toast.makeText(getContext(), "Buscando...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(direccion, 1);

                requireActivity().runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng pos = new LatLng(address.getLatitude(), address.getLongitude());

                        // Actualizar mapa y variables
                        latitudActual = String.valueOf(pos.latitude);
                        longitudActual = String.valueOf(pos.longitude);

                        gMap.clear();
                        gMap.addMarker(new MarkerOptions().position(pos).title(direccion));
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
                        Toast.makeText(getContext(), "Ubicación encontrada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "No se encontró la dirección", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ==========================================
    // LÓGICA DE GUARDADO (FIREBASE)
    // ==========================================

    private void validarYGuardarObra() {
        String nombre = etNombre.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String fechaInicioStr = etFechaInicio.getText().toString().trim();
        String fechaFinStr = etFechaFin.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) { etNombre.setError("Requerido"); return; }
        if (TextUtils.isEmpty(ubicacion)) { etUbicacion.setError("Requerido"); return; }
        if (TextUtils.isEmpty(fechaInicioStr)) { Toast.makeText(getContext(), "Falta fecha inicio", Toast.LENGTH_SHORT).show(); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dateInicio = null, dateFin = null;
        try {
            dateInicio = sdf.parse(fechaInicioStr);
            if (!fechaFinStr.isEmpty()) dateFin = sdf.parse(fechaFinStr);
        } catch (ParseException e) { e.printStackTrace(); }

        // Creamos el objeto con la ubicación del texto final y las coordenadas (si se usó el mapa)
        Obra nuevaObra = new Obra(
                null,
                nombre,
                "Iniciando",
                latitudActual,
                longitudActual,
                ubicacion,
                "",
                dateInicio,
                dateFin
        );

        db.collection("obras").add(nuevaObra)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("id", documentReference.getId());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Obra registrada con éxito", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ==========================================
    // UTILIDADES Y CICLO DE VIDA
    // ==========================================

    private void mostrarCalendario(TextInputEditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            editText.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void limpiarFormulario() {
        etNombre.setText("");
        etUbicacion.setText("");
        etFechaInicio.setText("");
        etFechaFin.setText("");
        latitudActual = "0.0";
        longitudActual = "0.0";
        if(gMap != null) gMap.clear();
        verificarPermisoYCentrar(); // Resetear mapa al GPS actual
        btnGuardar.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verificarPermisoYCentrar();
            }
        }
    }

    @Override
    public void onResume() { super.onResume(); mapView.onResume(); }
    @Override
    public void onPause() { super.onPause(); mapView.onPause(); }
    @Override
    public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}