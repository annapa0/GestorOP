package com.example.gestorop.model;

public class Usuario {
    private Integer id;
    private String nombre;
    private String email;

    private String rol; // "ADMIN", "SUPERVISOR", "RESIDENTE"

    public Usuario() {
        // Constructor vacío requerido obligatoriamente por Firebase
    }

    public Usuario(Integer uid, String nombre, String email, String rol) {
        this.id = uid;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}