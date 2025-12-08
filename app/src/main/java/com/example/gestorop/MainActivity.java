package com.example.gestorop;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.gestorop.ui.OpcionesFragment;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // Cargar el menú lateral solo una vez
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sideContainer, new OpcionesFragment())
                .commit();

        // Configurar NavHost
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        // Destinos principales (raíz)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.crearUsuarioFragment
        ).setOpenableLayout(drawerLayout).build();

        // Toolbar y navegación
        setSupportActionBar(toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Abrir menú lateral
        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(android.view.Gravity.START)
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController =
                ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))
                        .getNavController();

        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
