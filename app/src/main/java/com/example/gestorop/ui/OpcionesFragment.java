package com.example.gestorop.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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

        btnPerfil.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.homeFragment)
        );

        btnCrearUsuario.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.crearUsuarioFragment)
        );

        btnCerrar.setOnClickListener(v -> {
            // Aquí agregas tu lógica para cerrar sesión
        });

        return view;
    }
}
