package edu.pmdm.gympro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import edu.pmdm.gympro.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_inicio, R.id.nav_clientes, R.id.nav_grupos, R.id.nav_monitores, R.id.nav_analisis, R.id.nav_calendario
        ).setOpenableLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_inicio) {
                navController.popBackStack(R.id.nav_inicio, false);
            } else {
                navController.navigate(id);
            }
            if (id == R.id.nav_analisis) {
                navController.navigate(R.id.nav_analisis);
            }

            drawer.closeDrawers();
            return true;
        });

        // Botón de logout
        View headerView = binding.navView.getHeaderView(0);
        Button btnLogout = headerView.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, edu.pmdm.gympro.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Cargar datos al iniciar
        actualizarDatosAdministrador();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_admin) {
            Intent intent = new Intent(this, EditarAdministradorActivity.class);
            startActivityForResult(intent, 100); // Lanzar con requestCode
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            actualizarDatosAdministrador(); // Recargar datos del header
        }
    }

    private void actualizarDatosAdministrador() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = auth.getCurrentUser().getUid();

        View headerView = binding.navView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.textViewNombre);
        TextView tvCorreo = headerView.findViewById(R.id.textViewCorreo);
        ImageView ivFoto = headerView.findViewById(R.id.imageViewProfile);

        db.collection("administradores").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombreAdministrador");
                        String correo = documentSnapshot.getString("email");
                        String fotoUrl = documentSnapshot.getString("photo");

                        tvNombre.setText(nombre != null ? nombre : "Admin");
                        tvCorreo.setText(correo != null ? correo : "correo@ejemplo.com");

                        // Evaluar si hay una foto válida
                        if (fotoUrl != null && !fotoUrl.trim().isEmpty() && !fotoUrl.equals("logo_por_defecto")) {
                            try {
                                Glide.with(this).load(Uri.parse(fotoUrl)).into(ivFoto); // ✅ Usamos URI
                            } catch (Exception e) {
                                ivFoto.setImageResource(R.drawable.usuario_sinfondo);
                            }
                        } else {
                            ivFoto.setImageResource(R.drawable.usuario_sinfondo);
                        }

                    }
                });
    }
}
