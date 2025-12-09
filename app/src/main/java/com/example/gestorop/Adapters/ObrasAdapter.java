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
    private FirebaseFirestore db; // Instancia de Firebase para buscar nombres

    public ObrasAdapter(List<Obra> listaObras) {
        this.listaObras = listaObras;
        this.db = FirebaseFirestore.getInstance(); // Inicializamos DB
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

        // Datos básicos
        holder.txtNombre.setText(obra.getNombre());
        holder.txtUbicacion.setText("📍 " + obra.getUbicacion());
        holder.txtEstatus.setText(obra.getEstatus());

        // --- LÓGICA PARA BUSCAR EL SUPERVISOR ---
        String supervisorId = obra.getSupervisorId();

        if (supervisorId != null && !supervisorId.isEmpty()) {
            holder.txtSupervisor.setVisibility(View.VISIBLE);
            holder.txtSupervisor.setText("Supervisor: Buscando...");

            // Consultamos la colección "users" para obtener el email
            db.collection("users").document(supervisorId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Puedes usar "email" o "nombre" según lo que tengas en users
                            String email = documentSnapshot.getString("email");
                            holder.txtSupervisor.setText("Supervisor: " + (email != null ? email : "Sin nombre"));
                        } else {
                            holder.txtSupervisor.setText("Supervisor: Usuario no encontrado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.txtSupervisor.setText("Supervisor: Error al cargar");
                    });
        } else {
            // Si no tiene ID asignado (casos antiguos o errores)
            holder.txtSupervisor.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
    }

    static class ObraViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtUbicacion, txtEstatus, txtSupervisor;

        public ObraViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreObra);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacion);
            txtEstatus = itemView.findViewById(R.id.txtEstatus);
            txtSupervisor = itemView.findViewById(R.id.txtSupervisor); // Vinculamos el nuevo texto
        }
    }
}