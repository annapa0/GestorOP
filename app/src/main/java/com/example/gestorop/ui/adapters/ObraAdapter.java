package com.example.gestorop.ui.adapters;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
 // Si sale rojo, haz Alt+Enter e impórtalo
import com.example.gestorop.model.Obra; // Importa tu modelo Obra
import com.example.myapplication.R;

import java.util.List;

public class ObraAdapter extends RecyclerView.Adapter<ObraAdapter.ObraViewHolder> {

    private List<Obra> listaObras;

    public ObraAdapter(List<Obra> listaObras) {
        this.listaObras = listaObras;
    }

    @NonNull
    @Override
    public ObraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_obra, parent, false);
        return new ObraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObraViewHolder holder, int position) {
        Obra obra = listaObras.get(position);
        holder.tvNombre.setText(obra.getNombre());
        holder.tvEstatus.setText(obra.getEstatus());

        // Cambiar color según estatus
        String estatus = obra.getEstatus();
        if ("TERMINANDO".equals(estatus)) {
            holder.tvEstatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
        } else if ("PROCESO".equals(estatus)) {
            holder.tvEstatus.setBackgroundColor(Color.parseColor("#FFC107")); // Amarillo
        } else {
            holder.tvEstatus.setBackgroundColor(Color.parseColor("#2196F3")); // Azul
        }
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
    }

    public static class ObraViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEstatus;

        public ObraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreObra);
            tvEstatus = itemView.findViewById(R.id.tvEstatus);
        }
    }
}