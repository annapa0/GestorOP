package com.example.gestorop.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myapplication.R;
// Importamos la clase de Sesión (Asegúrate que el paquete sea correcto)
import com.example.gestorop.model.Sesion;

public class OpcionesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opciones, container, false);

        // 1. Vincular Botones
        Button btnPerfil = view.findViewById(R.id.btnPerfil);
        Button btnCrearUsuario = view.findViewById(R.id.btnCrearUsuario);
        Button btnCreaObra = view.findViewById(R.id.btnCreaObra);
        Button btnCerrar = view.findViewById(R.id.btnCerrarSesion);
        Button btnMisObras = view.findViewById(R.id.btnMisObras);
        // 2. OBTENER EL ROL DE LA SESIÓN
        String rolActual = Sesion.obtenerRol(requireContext());

        // 3. APLICAR LÓGICA DE VISIBILIDAD SEGÚN EL ROL

        if (rolActual.equalsIgnoreCase("Admin")) {
            // El Admin ve TODO, así que no ocultamos nada (se quedan visibles por defecto)

        } else if (rolActual.equalsIgnoreCase("Supervisor")) {
            // Supervisor: Ocultamos Crear Usuario y Crear Obra
            btnCrearUsuario.setVisibility(View.GONE);
            btnCreaObra.setVisibility(View.GONE);

        } else if (rolActual.equalsIgnoreCase("Residente")) {
            // Residente: Ocultamos Crear Usuario y Crear Obra (Igual que supervisor en este caso)
            btnCrearUsuario.setVisibility(View.GONE);
            btnCreaObra.setVisibility(View.GONE);
        }

        // 4. Configurar Listeners (Navegación)

        btnPerfil.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.homeFragment)
        );

        btnCrearUsuario.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.crearUsuarioFragment)
        );

        btnCreaObra.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.crearObraFragment)
        );

        btnMisObras.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.misObrasFragment)
        );

        // 5. Lógica de Cerrar Sesión
        btnCerrar.setOnClickListener(v -> {
            // Borramos los datos de la sesión
            Sesion.cerrarSesion(requireContext());

            // Redirigimos al Login y borramos el historial para que no puedan volver atrás
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            // Si estás en un fragment, a veces es bueno asegurar que la activity se cierre o cambie
            requireActivity().finish();
        });

        return view;
    }
}