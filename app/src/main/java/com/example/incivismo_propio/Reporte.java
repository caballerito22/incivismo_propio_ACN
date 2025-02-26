package com.example.incivismo_propio;

public class Reporte {
    private String ubicacion;
    private String latitud;
    private String longitud;
    private String problema;
    private String url;

    public Reporte() {
    }

    public Reporte(String ubicacion, String latitud, String longitud, String problema, String url) {
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.problema = problema;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
