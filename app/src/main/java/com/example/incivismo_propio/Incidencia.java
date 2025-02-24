package com.example.incivismo_propio;

public class Incidencia {
    private String ubicacion;
    private String latitud;
    private String longitud;
    private String problema;

    public Incidencia() {
    }

    public Incidencia(String ubicacion, String latitud, String longitud, String problema) {
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.problema = problema;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getProblema() {
        return problema;
    }

    public void setProblema(String problema) {
        this.problema = problema;
    }
}
