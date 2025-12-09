package com.example.gestorop.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log; // Importante para depurar
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.gestorop.model.Avance;
import com.example.gestorop.model.Sesion;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AvancesAdapter extends RecyclerView.Adapter<AvancesAdapter.ViewHolder> {

    private List<Avance> lista;
    private Context context;
    private String rolActual;
    private FirebaseFirestore db;

    public AvancesAdapter(Context context, List<Avance> lista) {
        this.context = context;
        this.lista = lista;
        this.rolActual = Sesion.obtenerRol(context);
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Avance avance = lista.get(position);

        // --- DEPURACIÓN ---
        Log.d("ADAPTER_DEBUG", "Pos: " + position + " | Foto: " + avance.getFotoUrl() + " | Video: " + avance.getVideoUrl());
        // ------------------

        // 1. Textos
        holder.tvEtapa.setText(avance.getEtapa());
        holder.tvDesc.setText(avance.getDescripcion());
        holder.tvUsuario.setText("Por: " + avance.getUsuarioEmail());

        if (avance.getFechaRegistro() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvFecha.setText(sdf.format(avance.getFechaRegistro()));
        }

        // 2. Lógica de FOTO (Blindada)
        String urlFoto = avance.getFotoUrl();
        if (urlFoto != null && !urlFoto.trim().isEmpty()) {
            holder.imgEvidencia.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(urlFoto)
                    .placeholder(android.R.drawable.ic_menu_gallery) // Imagen de carga
                    .error(android.R.drawable.stat_notify_error)     // Imagen si falla
                    .into(holder.imgEvidencia);
        } else {
            holder.imgEvidencia.setVisibility(View.GONE);
        }

        // 3. Lógica de VIDEO (Blindada)
        String urlVideo = avance.getVideoUrl();
        if (urlVideo != null && !urlVideo.trim().isEmpty()) {
            holder.btnVerVideo.setVisibility(View.VISIBLE);

            holder.btnVerVideo.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(urlVideo), "video/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "No se encontró reproductor", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.btnVerVideo.setVisibility(View.GONE);
        }

        // 4. Estado y Color
        String estado = avance.getEstado() != null ? avance.getEstado() : "PENDIENTE";
        holder.tvEstado.setText("ESTADO: " + estado);

        if (estado.equals("APROBADO")) holder.tvEstado.setTextColor(Color.GREEN);
        else if (estado.equals("RECHAZADO")) holder.tvEstado.setTextColor(Color.RED);
        else holder.tvEstado.setTextColor(Color.parseColor("#FFA000")); // Naranja

        // 5. Botones Supervisor
        boolean esSupervisor = rolActual.equalsIgnoreCase("Supervisor") || rolActual.equalsIgnoreCase("Admin");

        if (esSupervisor && estado.equals("PENDIENTE")) {
            holder.layoutAcciones.setVisibility(View.VISIBLE);
            holder.btnAprobar.setOnClickListener(v -> actualizarEstado(avance.getId(), "APROBADO", position));
            holder.btnRechazar.setOnClickListener(v -> actualizarEstado(avance.getId(), "RECHAZADO", position));
        } else {
            holder.layoutAcciones.setVisibility(View.GONE);
        }
    }

    private void actualizarEstado(String id, String nuevoEstado, int position) {
        db.collection("avances").document(id).update("estado", nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show();
                    lista.get(position).setEstado(nuevoEstado);
                    notifyItemChanged(position);
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEtapa, tvFecha, tvUsuario, tvDesc, tvEstado;
        ImageView imgEvidencia;
        Button btnVerVideo;
        LinearLayout layoutAcciones;
        Button btnAprobar, btnRechazar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEtapa = itemView.findViewById(R.id.tvEtapaAvance);
            tvFecha = itemView.findViewById(R.id.tvFechaAvance);
            tvUsuario = itemView.findViewById(R.id.tvUsuarioAvance);
            tvDesc = itemView.findViewById(R.id.tvDescAvance);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            imgEvidencia = itemView.findViewById(R.id.imgEvidencia);
            btnVerVideo = itemView.findViewById(R.id.btnVerVideo);
            layoutAcciones = itemView.findViewById(R.id.layoutAccionesSupervisor);
            btnAprobar = itemView.findViewById(R.id.btnAprobar);
            btnRechazar = itemView.findViewById(R.id.btnRechazar);
        }
    }
}