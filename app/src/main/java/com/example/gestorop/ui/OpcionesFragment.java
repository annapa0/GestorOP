package com.example.gestorop.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;

public class OpcionesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opciones, container, false);

        Button btnPerfil = view.findViewById(R.id.btnPerfil);
        Button btnCrearUsuario = view.findViewById(R.id.btnCrearUsuario);
        Button btnCerrar = view.findViewById(R.id.btnCerrarSesion);

        btnPerfil.setOnClickListener(v -> {
            // Acción del botón Perfil
        });

        btnCrearUsuario.setOnClickListener(v -> {
            // Acción del botón Configuración
        });

        btnCerrar.setOnClickListener(v -> {
            // Acción de cerrar sesión
        });


        return view;
    }
}