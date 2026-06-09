package com.example.mymaps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int REQUEST_LOCATION_UPDATES=1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnView = findViewById(R.id.button_visualizar);
        Button btnReg = findViewById(R.id.button_registrar);
        Button btnConfig = findViewById(R.id.button_config);
        Button btnSair = findViewById(R.id.button_sair);

        btnView.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnConfig.setOnClickListener(this);
        btnSair.setOnClickListener(this);

        if (!possuiPermissaoLocalizacao()) {
            solicitarPermissaoLocalizacao();
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.button_registrar){
            Intent i = new Intent(MainActivity.this, RegistrarTrilhaActivity.class);
            startActivity(i);
        }
        if(id == R.id.button_visualizar){
            Intent i = new Intent(MainActivity.this, ConsultarTrilhasActivity.class);
            startActivity(i);
        }
        if(id == R.id.button_config){
            Intent i = new Intent(MainActivity.this, ConfiguracaoActivity.class);
            startActivity(i);
        }
        if(id == R.id.button_sair){
            finish();
        }
    }
    private boolean possuiPermissaoLocalizacao() {

        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void solicitarPermissaoLocalizacao() {

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                REQUEST_LOCATION_UPDATES
        );
    }

}