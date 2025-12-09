package com.example.gestorop.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.gestorop.model.Obra;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class ObrasAdapter extends RecyclerView.Adapter<ObrasAdapter.ObraViewHolder> {

    private List<Obra> listaObras;
    private FirebaseFirestore db;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Obra obra);
    }

    public ObrasAdapter(List<Obra> listaObras, OnItemClickListener listener) {
        this.listaObras = listaObras;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ObraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_obra_card, parent, false);
        return new ObraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObraViewHolder holder, int position) {
        Obra obra = listaObras.get(position);

        holder.txtNombre.setText(obra.getNombre());
        holder.txtUbicacion.setText("📍 " + obra.getUbicacion());
        holder.txtEstatus.setText(obra.getEstatus());

        // 1. MOSTRAR SUPERVISOR (Si existe)
        if (obra.getSupervisorId() != null && !obra.getSupervisorId().isEmpty()) {
            holder.txtSupervisor.setVisibility(View.VISIBLE);
            db.collection("users").document(obra.getSupervisorId()).get()
                    .addOnSuccessListener(doc -> {
                        if(doc.exists()) holder.txtSupervisor.setText("Sup: " + doc.getString("email"));
                    });
        } else {
            holder.txtSupervisor.setVisibility(View.GONE);
        }

        // 2. MOSTRAR RESIDENTE (NUEVO)
        if (obra.getResidenteId() != null && !obra.getResidenteId().isEmpty()) {
            holder.txtResidente.setVisibility(View.VISIBLE);
            holder.txtResidente.setText("Cargando residente...");

            db.collection("users").document(obra.getResidenteId()).get()
                    .addOnSuccessListener(doc -> {
                        if(doc.exists()) {
                            // Aquí mostramos claramente a quién se asignó
                            holder.txtResidente.setText("➡ Asignado a: " + doc.getString("email"));
                        }
                    });
        } else {
            holder.txtResidente.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(obra);
        });
    }

    @Override
    public int getItemCount() { return listaObras.size(); }

    static class ObraViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtUbicacion, txtEstatus, txtSupervisor, txtResidente; // Agregado txtResidente

        public ObraViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreObra);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacion);
            txtEstatus = itemView.findViewById(R.id.txtEstatus);
            txtSupervisor = itemView.findViewById(R.id.txtSupervisor);
            txtResidente = itemView.findViewById(R.id.txtResidente); // Vinculado
        }
    }
}