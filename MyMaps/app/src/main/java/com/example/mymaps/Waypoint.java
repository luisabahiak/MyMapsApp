package com.example.mymaps;

import android.location.Location;

public class Waypoint {
    private int id;
    private long trilhaId; // Referência para a tabela de Trilhas
    private double latitude;
    private double longitude;
    private double altitude;

    public Waypoint() {}

    // Construtor atualizado recebendo o id da trilha corrente
    public Waypoint(Location location, long trilhaId) {
        this.trilhaId = trilhaId;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getTrilhaId() { return trilhaId; }
    public void setTrilhaId(long trilhaId) { this.trilhaId = trilhaId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getAltitude() { return altitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }
}