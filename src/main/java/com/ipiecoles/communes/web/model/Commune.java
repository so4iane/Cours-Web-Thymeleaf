package com.ipiecoles.communes.web.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.*;

@Entity
public class Commune {
    @Id
    //5 chiffres, le 2e peut être une lettre A ou B et Obligatoire
    @NotBlank(message = "Le code Insee ne doit pas être nul")
    @Pattern(regexp = "^[0-9]{1}[0-9AB]{1}[0-9]{3}$", message = "Le code INSEE doit contenir 5 chiffres (Le deuxième caractère peut être A ou B pour les communes de Corse)")
    private String codeInsee;
    //Taille max 50
    //Lettres, majuscules, minuscules, tirets, apostrophes, espace et éventuellement terminer par 1 ou 2 chiffres
    //Obligatoire
    @NotBlank(message = "Le nom de la commune ne doit pas être nul")
    @Pattern(regexp = "^[A-Za-z-' ]+[0-9]{0,2}$", message = "Le nom de la commune ne peut contenir que des lettres, des tirets, des espaces et éventuellement le numéro d'arrondissement")
    private String nom;
    @Column(length = 5)
    //5 chiffres et Obligatoire
    @NotBlank(message = "Le code Postal ne doit pas être nul")
    @Pattern(regexp = "^[0-9]{5}$", message = "Le code postal doit contenir 5 chiffres")
    private String codePostal;
    //Interval latitude?Facultatif
    @Min(value = -90)
    @Max(value = 90)
    private Double latitude;
    //Interval longitude? Facultatif
    @Min(value = -180)
    @Max(value = 180)
    private Double longitude;

    public Commune() {
    }

    public Commune(String codeInsee, String nom, String codePostal, Double latitude, Double longitude) {
        this.codeInsee = codeInsee;
        this.nom = nom;
        this.codePostal = codePostal;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCodeInsee() {
        return codeInsee;
    }

    public void setCodeInsee(String codeInsee) {
        this.codeInsee = codeInsee;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Commune{" +
                "codeInsee='" + codeInsee + '\'' +
                ", nom='" + nom + '\'' +
                ", codePostal='" + codePostal + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public Long getDistance(Double latitude, Double longitude) {
        Double lat1 = Math.toRadians(latitude);
        Double lng1 = Math.toRadians(longitude);
        Double lat2 = Math.toRadians(this.latitude);
        Double lng2 = Math.toRadians(this.longitude);

        double dlon = lng2 - lng1;
        double dlat = lat2 - lat1;

        double a = Math.pow((Math.sin(dlat / 2)), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(6371.009 * c);
    }
}
