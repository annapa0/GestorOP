package com.example.gestorop.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.myapplication.R;

public class OpcionesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_opciones, container, false);

        AutoCompleteTextView menu = view.findViewById(R.id.menuOpciones);

        String[] opciones = {
                "Opción 1",
                "Opción 2",
                "Opción 3"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                opciones
        );

        menu.setAdapter(adapter);

        menu.setOnItemClickListener((parent, view1, position, id) -> {
            String seleccion = opciones[position];
            // Usar opción seleccionada
        });

        return view;
    }
}