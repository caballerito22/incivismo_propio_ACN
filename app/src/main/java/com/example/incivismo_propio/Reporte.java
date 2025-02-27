package com.example.incivismo_propio;

public class Reporte {
    private String ubicacion;
    private Double latitud;
    private Double longitud;
    private String problema;
    private String url;

    public Reporte() {
    }

    public Reporte(String ubicacion, Double latitud, Double longitud, String problema, String url) {
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

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
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
