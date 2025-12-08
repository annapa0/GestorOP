package com.example.gestorop.ui; // Asegúrate de que coincida con tu paquete

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import com.example.gestorop.model.Obra;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CrearObraFragment extends Fragment {

    // Vistas
    private TextInputEditText etNombre, etUbicacion, etFechaInicio, etFechaFin;
    private Button btnGuardar;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;

    // Calendario para el selector de fecha
    private final Calendar calendar = Calendar.getInstance();

    public CrearObraFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_obra, container, false);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Vincular vistas con el XML
        etNombre = view.findViewById(R.id.etNombreObra);
        etUbicacion = view.findViewById(R.id.etUbicacionObra);
        etFechaInicio = view.findViewById(R.id.etFechaInicio);
        etFechaFin = view.findViewById(R.id.etFechaFin);
        btnGuardar = view.findViewById(R.id.btnGuardarObra);
        progressBar = view.findViewById(R.id.progressBar);

        // Configurar Listeners para los campos de fecha (abrir calendario)
        etFechaInicio.setOnClickListener(v -> mostrarCalendario(etFechaInicio));
        etFechaFin.setOnClickListener(v -> mostrarCalendario(etFechaFin));

        // Configurar Listener para el botón guardar
        btnGuardar.setOnClickListener(v -> validarYGuardarObra());

        return view;
    }

    // Método para mostrar el DatePickerDialog
    private void mostrarCalendario(TextInputEditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    // Actualizar el calendario con la fecha seleccionada
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Formatear la fecha a String y ponerla en el EditText
                    String formatoFecha = "dd/MM/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(formatoFecha, Locale.getDefault());
                    editText.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void validarYGuardarObra() {
        String nombre = etNombre.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String fechaInicioStr = etFechaInicio.getText().toString().trim();
        String fechaFinStr = etFechaFin.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }
        if (TextUtils.isEmpty(ubicacion)) {
            etUbicacion.setError("La ubicación es obligatoria");
            return;
        }
        if (TextUtils.isEmpty(fechaInicioStr)) {
            Toast.makeText(getContext(), "Selecciona una fecha de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar carga y bloquear botón
        progressBar.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        // Parsear Fechas (String -> Date)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dateInicio = null;
        Date dateFin = null;

        try {
            dateInicio = sdf.parse(fechaInicioStr);
            if (!fechaFinStr.isEmpty()) {
                dateFin = sdf.parse(fechaFinStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Crear objeto Obra
        // Nota: Pasamos null en el ID porque Firebase lo generará, luego lo actualizamos.
        // Estatus default: "Iniciando", Coordenadas default: "0.0"
        Obra nuevaObra = new Obra(
                null,                  // ID
                nombre,                // Nombre
                "Iniciando",           // Estatus inicial
                "0.0",                 // Latitud (pendiente)
                "0.0",                 // Longitud (pendiente)
                ubicacion,             // Ubicación
                "",                    // Supervisor ID (Aún no asignado)
                dateInicio,            // Fecha Inicio
                dateFin                // Fecha Fin
        );

        // Guardar en Firestore
        db.collection("obras")
                .add(nuevaObra)
                .addOnSuccessListener(documentReference -> {
                    // Éxito: Obtenemos el ID generado y actualizamos el documento
                    String idGenerado = documentReference.getId();

                    documentReference.update("id", idGenerado)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Obra registrada con éxito", Toast.LENGTH_SHORT).show();
                                limpiarFormulario();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void limpiarFormulario() {
        etNombre.setText("");
        etUbicacion.setText("");
        etFechaInicio.setText("");
        etFechaFin.setText("");
        etNombre.requestFocus();
        btnGuardar.setEnabled(true);
    }
}