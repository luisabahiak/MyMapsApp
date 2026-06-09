package com.example.mymaps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.mymaps.databinding.ActivityRegistrarTrilhaBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RegistrarTrilhaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRegistrarTrilhaBinding binding;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private TrilhaDB trilhaDB;
    private boolean isGravando = false;
    private long idTrilhaAtual = -1;

    private Chronometer chronometer;
    private TextView txtVelAtual, txtVelMaxima, txtDistancia;
    private Button btnStartStop;
    private Button btnVoltar;



    private Location ultimaLocalizacao = null;
    private double distanciaTotal = 0.0;
    private float velocidadeMaxima = 0.0f;

    private Marker marcadorUsuario = null;
    private Circle circuloAcuracia = null;
    private Polyline linhaTrajeto = null;
    private ArrayList<LatLng> listaPontosTrajeto = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegistrarTrilhaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        trilhaDB = new TrilhaDB(this);

        chronometer = findViewById(R.id.chronometer);
        txtVelAtual = findViewById(R.id.txt_vel_atual);
        txtVelMaxima = findViewById(R.id.txt_vel_maxima);
        txtDistancia = findViewById(R.id.txt_distancia);
        btnStartStop = findViewById(R.id.btn_start_stop);
        btnVoltar = findViewById(R.id.buttonVoltar2);

        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        // Obtém a última localização conhecida imediatamente
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            processarNovaLocalizacao(location);
                        }
                    });
        }

        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000
        ).setMinUpdateIntervalMillis(1500)
                .build();

        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGravando) {
                    solicitarNomeTrilhaEIniciar();
                } else {
                    pararMonitoramentoTrilha();
                }
            }
        });

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void solicitarNomeTrilhaEIniciar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nova Trilha");
        builder.setMessage("Digite um nome para identificar esta trilha:");

        final EditText inputNome = new EditText(this);
        inputNome.setHint("Ex: Caminhada no Parque");
        builder.setView(inputNome);

        builder.setPositiveButton("Iniciar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nomeTrilha = inputNome.getText().toString().trim();
                if (nomeTrilha.isEmpty()) {
                    nomeTrilha = "Trilha Sem Nome";
                }
                executarInicioTrilha(nomeTrilha);
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void executarInicioTrilha(String nomeTrilha) {
        isGravando = true;
        btnStartStop.setText("Parar Trilha");
        btnStartStop.setBackgroundColor(Color.RED);

        String dataHoraInicio = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        idTrilhaAtual = trilhaDB.criarNovaTrilha(nomeTrilha, dataHoraInicio);

        distanciaTotal = 0.0;
        velocidadeMaxima = 0.0f;
        ultimaLocalizacao = null;
        listaPontosTrajeto.clear();

        if (linhaTrajeto != null) {
            linhaTrajeto.remove();
            linhaTrajeto = null;
        }

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        Toast.makeText(this, "Gravando: " + nomeTrilha, Toast.LENGTH_SHORT).show();
    }

    private void pararMonitoramentoTrilha() {
        isGravando = false;
        btnStartStop.setText("Iniciar Trilha");
        btnStartStop.setBackgroundColor(Color.parseColor("#448C84"));

        chronometer.stop();

        String dataHoraFim = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        if (idTrilhaAtual != -1) {
            trilhaDB.finalizarTrilha(idTrilhaAtual, dataHoraFim);
        }

        Toast.makeText(this, "Trilha salva e registrada no histórico!", Toast.LENGTH_LONG).show();
        idTrilhaAtual = -1;
    }

    private void processarNovaLocalizacao(Location location) {
        LatLng latLngAtual = new LatLng(location.getLatitude(), location.getLongitude());
        atualizarElementosVisuaisMapa(location, latLngAtual);

        if (!isGravando || idTrilhaAtual == -1) {
            return;
        }

        Waypoint novoPonto = new Waypoint(location, idTrilhaAtual);
        trilhaDB.registrarWaypoint(novoPonto);

        float velocidadAtual = location.hasSpeed() ? location.getSpeed() * 3.6f : 0.0f;
        if (velocidadAtual > velocidadeMaxima) {
            velocidadeMaxima = velocidadAtual;
        }

        if (ultimaLocalizacao != null) {
            double incrementoDistancia = ultimaLocalizacao.distanceTo(location) / 1000.0;
            distanciaTotal += incrementoDistancia;
        }
        ultimaLocalizacao = location;

        txtVelAtual.setText(String.format("Vel. Instantânea: %.1f km/h", velocidadAtual));
        txtVelMaxima.setText(String.format("Vel. Máxima: %.1f km/h", velocidadeMaxima));
        txtDistancia.setText(String.format("Distância: %.2f km", distanciaTotal));

        listaPontosTrajeto.add(latLngAtual);
        desenharLinhaTrajeto();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // --- CORREÇÃO 1: LEITURA DINÂMICA DO TIPO DE MAPA ---
        SharedPreferences sharedPref = getSharedPreferences("ConfigsAppTrilhas", Context.MODE_PRIVATE);
        String tipoMapa = sharedPref.getString("tipo_mapa", "vetorial");

        if (tipoMapa.equals("satelite")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location localizacaoAtual = locationResult.getLastLocation();
                if (localizacaoAtual != null) {
                    processarNovaLocalizacao(localizacaoAtual);
                }
            }
        };


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }

    }

    private void atualizarElementosVisuaisMapa(Location location, LatLng latLngAtual) {

        if (marcadorUsuario == null) {
            marcadorUsuario = mMap.addMarker(
                    new MarkerOptions()
                            .position(latLngAtual)
                            .title("Sua posição")
                            .anchor(0.5f, 1.0f)
                            .icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapa))
            );


        } else {

            marcadorUsuario.setPosition(latLngAtual);
            if(location.hasBearing()){
                marcadorUsuario.setRotation(
                        location.getBearing()
                );
            }

        }
        if (circuloAcuracia != null) circuloAcuracia.remove();

        circuloAcuracia = mMap.addCircle(new CircleOptions()
                .center(latLngAtual)
                .radius(location.getAccuracy())
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(50, 0, 0, 255))
                .strokeWidth(2));

        // --- CORREÇÃO 2: LEITURA DINÂMICA DA ORIENTAÇÃO DA NAVEGAÇÃO ---
        SharedPreferences sharedPref = getSharedPreferences("ConfigsAppTrilhas", Context.MODE_PRIVATE);
        String formaNav = sharedPref.getString("forma_nav", "north_up");

        if (formaNav.equals("course_up") && location.hasBearing()) {
            // Se for Course Up, cria uma posição de câmera rotacionada com os graus (bearing) fornecidos pelo GPS
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLngAtual)
                    .zoom(16f)
                    .bearing(location.getBearing()) // Alinha o topo do mapa com o rumo do usuário
                    .tilt(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            // Se for North Up (ou o GPS ainda não tiver bearing), mantém o Norte fixo para cima
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngAtual, 16f));
        }
    }

    private void desenharLinhaTrajeto() {
        if (linhaTrajeto != null) {
            linhaTrajeto.remove();
        }
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(listaPontosTrajeto)
                .color(Color.CYAN)
                .width(10);
        linhaTrajeto = mMap.addPolyline(polylineOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}