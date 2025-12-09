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
    private OnItemClickListener listener; // <--- 1. Interfaz para el clic

    // 2. Definimos la interfaz
    public interface OnItemClickListener {
        void onItemClick(Obra obra);
    }

    // 3. Actualizamos el constructor para recibir el listener
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

        // Lógica de Supervisor (copiada del paso anterior)
        String supervisorId = obra.getSupervisorId();
        if (supervisorId != null && !supervisorId.isEmpty()) {
            holder.txtSupervisor.setVisibility(View.VISIBLE);
            holder.txtSupervisor.setText("Cargando supervisor...");
            db.collection("users").document(supervisorId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) holder.txtSupervisor.setText("Sup: " + doc.getString("email"));
                    });
        } else {
            holder.txtSupervisor.setVisibility(View.GONE);
        }

        // 4. DETECTAR EL CLIC EN LA TARJETA
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(obra);
        });
    }

    @Override
    public int getItemCount() { return listaObras.size(); }

    static class ObraViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtUbicacion, txtEstatus, txtSupervisor;
        public ObraViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreObra);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacion);
            txtEstatus = itemView.findViewById(R.id.txtEstatus);
            txtSupervisor = itemView.findViewById(R.id.txtSupervisor);
        }
    }
}