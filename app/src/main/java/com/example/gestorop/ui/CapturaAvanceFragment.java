package com.example.gestorop.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color; // Importante para el círculo
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.gestorop.model.Sesion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Implementamos OnMapReadyCallback
public class CapturaAvanceFragment extends Fragment implements OnMapReadyCallback {

    // --- CONFIGURACIÓN DE GEOCERCA ---
    private static final float RADIO_PERMITIDO_METROS = 200.0f; // 200 metros a la redonda
    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int PERM_CODE = 100;

    // UI
    private ImageView imgPreview, iconPlay;
    private VideoView videoPreview;
    private TextView tvEstadoUbicacion, tvCoords, tvTitulo;
    private EditText etDesc;
    private Button btnFoto, btnVideo, btnGps, btnGuardar;
    private ProgressBar progressBar;
    private MapView mapView; // Mapa nuevo
    private GoogleMap gMap;

    // Archivos y Lógica
    private Uri uriFotoFinal = null;
    private Uri uriVideoFinal = null;
    private String currentPath;
    private String tipoEvidencia = "";

    // Ubicación
    private FusedLocationProviderClient fusedClient;
    private Location ubicacionUsuario;
    private Location ubicacionObra; // Ubicación fija de la obra
    private boolean estaEnZona = false; // Bandera de validación

    // Datos Obra
    private String obraId, obraNombre;
    private double obraLat, obraLng;

    private FirebaseFirestore db;
    private StorageReference storage;

    public CapturaAvanceFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            obraId = getArguments().getString("OBRA_ID");
            obraNombre = getArguments().getString("OBRA_NOMBRE");

            // Recibir coordenadas de la obra (Strings convertidos a Double)
            try {
                String latS = getArguments().getString("OBRA_LAT");
                String lngS = getArguments().getString("OBRA_LNG");
                obraLat = Double.parseDouble(latS);
                obraLng = Double.parseDouble(lngS);

                // Crear objeto Location para cálculos
                ubicacionObra = new Location("ProviderObra");
                ubicacionObra.setLatitude(obraLat);
                ubicacionObra.setLongitude(obraLng);
            } catch (Exception e) {
                obraLat = 0.0; obraLng = 0.0; // Manejo de error
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_captura_avance, container, false);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        // Vincular UI
        tvTitulo = view.findViewById(R.id.tvTituloAvance);
        imgPreview = view.findViewById(R.id.imgPreview);
        videoPreview = view.findViewById(R.id.videoPreview);
        iconPlay = view.findViewById(R.id.iconPlay);

        tvEstadoUbicacion = view.findViewById(R.id.tvEstadoUbicacion);
        tvCoords = view.findViewById(R.id.tvCoordenadas);
        etDesc = view.findViewById(R.id.etDescripcionAvance);

        btnFoto = view.findViewById(R.id.btnTomarFoto);
        btnVideo = view.findViewById(R.id.btnGrabarVideo);
        btnGps = view.findViewById(R.id.btnUbicacion);
        btnGuardar = view.findViewById(R.id.btnGuardarAvance);
        progressBar = view.findViewById(R.id.progressBarAvance);

        // Mapa
        mapView = view.findViewById(R.id.mapViewValidacion);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if(tvTitulo != null) tvTitulo.setText("Avance: " + obraNombre);

        // Listeners
        btnFoto.setOnClickListener(v -> tomarFoto());
        btnVideo.setOnClickListener(v -> grabarVideo());
        btnGps.setOnClickListener(v -> obtenerUbicacionEnTiempoReal());
        btnGuardar.setOnClickListener(v -> iniciarSubida());

        videoPreview.setOnClickListener(v -> { if(!videoPreview.isPlaying()) videoPreview.start(); });

        return view;
    }

    // ==========================================
    // MAPA Y GEOCERCA
    // ==========================================
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // 1. Dibujar la OBRA y el CÍRCULO PERMITIDO
        if (obraLat != 0.0 && obraLng != 0.0) {
            LatLng posObra = new LatLng(obraLat, obraLng);

            // Marcador de la obra
            gMap.addMarker(new MarkerOptions().position(posObra).title("Ubicación de la Obra"));

            // Círculo de área permitida
            gMap.addCircle(new CircleOptions()
                    .center(posObra)
                    .radius(RADIO_PERMITIDO_METROS) // ej. 200 metros
                    .strokeWidth(2f)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.parseColor("#220000FF"))); // Azul transparente

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posObra, 15f));
        }

        // Intentar obtener ubicación al cargar el mapa
        obtenerUbicacionEnTiempoReal();
    }

    private void obtenerUbicacionEnTiempoReal() {
        if (!checkPermissions()) return;

        try {
            gMap.setMyLocationEnabled(true); // Mostrar puntito azul
        } catch (SecurityException e) {}

        Toast.makeText(getContext(), "Verificando zona...", Toast.LENGTH_SHORT).show();

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                ubicacionUsuario = location;
                validarDistancia();
            } else {
                tvEstadoUbicacion.setText("Estado: GPS Inactivo");
                tvEstadoUbicacion.setTextColor(Color.RED);
            }
        });
    }

    private void validarDistancia() {
        if (ubicacionObra == null || ubicacionUsuario == null) return;

        // Calcular distancia en metros
        float distanciaEnMetros = ubicacionUsuario.distanceTo(ubicacionObra);

        tvCoords.setText(String.format("Distancia: %.2f metros (Máx %.0fm)", distanciaEnMetros, RADIO_PERMITIDO_METROS));

        if (distanciaEnMetros <= RADIO_PERMITIDO_METROS) {
            // DENTRO DE LA ZONA
            estaEnZona = true;
            tvEstadoUbicacion.setText("✅ UBICACIÓN VÁLIDA (DENTRO DE LA OBRA)");
            tvEstadoUbicacion.setTextColor(Color.parseColor("#4CAF50")); // Verde
            btnGuardar.setBackgroundColor(Color.parseColor("#4CAF50")); // Botón Verde
        } else {
            // FUERA DE LA ZONA
            estaEnZona = false;
            tvEstadoUbicacion.setText("❌ UBICACIÓN INVÁLIDA (FUERA DE RANGO)");
            tvEstadoUbicacion.setTextColor(Color.RED);
            btnGuardar.setBackgroundColor(Color.parseColor("#CCCCCC")); // Botón Gris

            // Centrar mapa para mostrar cuán lejos está
            LatLng miPos = new LatLng(ubicacionUsuario.getLatitude(), ubicacionUsuario.getLongitude());
            gMap.animateCamera(CameraUpdateFactory.newLatLng(miPos));
        }

        validarBoton();
    }

    private void validarBoton() {
        // Para habilitar el botón se requiere:
        // 1. Estar en la zona (Geocerca)
        // 2. Tener al menos una foto o video

        if (estaEnZona && (uriFotoFinal != null || uriVideoFinal != null)) {
            btnGuardar.setEnabled(true);
        } else {
            btnGuardar.setEnabled(false);
        }
    }

    // ==========================================
    // CÁMARA (FOTO Y VIDEO - Código previo simplificado)
    // ==========================================
    // (Mantén los métodos dispatchTakePictureIntent, dispatchTakeVideoIntent,
    // createMediaFile y onActivityResult iguales que en la respuesta anterior)

    // ... AQUÍ VAN LOS MÉTODOS DE CÁMARA (Copia los de la respuesta anterior) ...
    // Solo recuerda llamar a validarBoton() dentro de onActivityResult

    // --- CÁMARA (FOTO) ---
    private void tomarFoto() {
        if (!checkPermissions()) return;
        try {
            File file = crearArchivo("IMG_", ".jpg", Environment.DIRECTORY_PICTURES);
            if (file != null) {
                Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                currentPath = uri.toString();
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void grabarVideo() {
        if (!checkPermissions()) return;
        try {
            File file = crearArchivo("VID_", ".mp4", Environment.DIRECTORY_MOVIES);
            if (file != null) {
                Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                currentPath = uri.toString();
                startActivityForResult(intent, REQUEST_VIDEO);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private File crearArchivo(String prefix, String suffix, String dir) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = requireActivity().getExternalFilesDir(dir);
        return File.createTempFile(prefix + timeStamp + "_", suffix, storageDir);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE) {
                uriFotoFinal = Uri.parse(currentPath);
                imgPreview.setImageURI(uriFotoFinal);
            } else if (requestCode == REQUEST_VIDEO) {
                uriVideoFinal = Uri.parse(currentPath);
                tvTitulo.setText("Video grabado correctamente"); // Feedback visual simple
                videoPreview.setVisibility(View.VISIBLE);
                iconPlay.setVisibility(View.VISIBLE);
                videoPreview.setVideoURI(uriVideoFinal);
                videoPreview.seekTo(100);
            }
            validarBoton(); // Re-validar si ya se puede guardar
        }
    }

    // ==========================================
    // SUBIDA (CHAIN - Código previo)
    // ==========================================
    private void iniciarSubida() {
        if (!estaEnZona) {
            Toast.makeText(getContext(), "Debes estar en la obra para reportar.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        // Iniciar cadena: Foto -> Video -> Firestore
        if (uriFotoFinal != null) subirFoto();
        else subirVideo(null);
    }

    private void subirFoto() {
        String name = "IMG_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.child("evidencias/" + obraId + "/" + name);
        ref.putFile(uriFotoFinal).addOnSuccessListener(t ->
                ref.getDownloadUrl().addOnSuccessListener(uri -> subirVideo(uri.toString()))
        ).addOnFailureListener(e -> fallar(e.getMessage()));
    }

    private void subirVideo(String urlFoto) {
        if (uriVideoFinal != null) {
            String name = "VID_" + System.currentTimeMillis() + ".mp4";
            StorageReference ref = storage.child("evidencias/" + obraId + "/" + name);
            ref.putFile(uriVideoFinal).addOnSuccessListener(t ->
                    ref.getDownloadUrl().addOnSuccessListener(uri -> guardarEnFirestore(urlFoto, uri.toString()))
            ).addOnFailureListener(e -> fallar(e.getMessage()));
        } else {
            guardarEnFirestore(urlFoto, null);
        }
    }

    private void guardarEnFirestore(String urlFoto, String urlVideo) {
        String userId = Sesion.obtenerId(requireContext());
        String userEmail = Sesion.obtenerEmail(requireContext());

        Map<String, Object> data = new HashMap<>();
        data.put("obraId", obraId);
        data.put("usuarioId", userId);
        data.put("usuarioEmail", userEmail);
        data.put("descripcion", etDesc.getText().toString());
        data.put("fecha", Timestamp.now());
        // Guardamos la ubicación REAL donde se tomó la foto (verificada)
        data.put("latitud", ubicacionUsuario.getLatitude());
        data.put("longitud", ubicacionUsuario.getLongitude());
        data.put("fotoUrl", urlFoto);
        data.put("videoUrl", urlVideo);

        db.collection("avances").add(data)
                .addOnSuccessListener(d -> {
                    Toast.makeText(getContext(), "Avance Verificado y Guardado", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .addOnFailureListener(e -> fallar("Error DB"));
    }

    private void fallar(String m) {
        progressBar.setVisibility(View.GONE);
        btnGuardar.setEnabled(true);
        Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show();
    }

    // Permisos
    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, PERM_CODE);
            return false;
        }
        return true;
    }

    // Ciclo de vida del mapa
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}