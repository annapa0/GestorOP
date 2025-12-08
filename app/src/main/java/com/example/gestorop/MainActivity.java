package com.example.gestorop;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gestorop.ui.HomeFragment;
import com.example.gestorop.ui.OpcionesFragment;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            abrirMenu();
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContainer, new HomeFragment())
                    .replace(R.id.sideContainer, new OpcionesFragment())
                    .commit();
        }

    }



    // Abrir menú
    public void abrirMenu() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    // Cerrar menú
    public void cerrarMenu() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}