/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Direccion")
@Getter
@Setter
@NoArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDireccion")
    private Integer id;

    @Column(name = "formatted_address", length = 510, nullable = false)
    private String formattedAddress;

    @Column(name = "line1", length = 50, nullable = false)
    private String line1;

    @Column(name = "sublocality", length = 50, nullable = false)
    private String sublocality;

    @Column(name = "locality", length = 50, nullable = false)
    private String locality;

    @Column(name = "admin_area2", length = 50)
    private String adminArea2;

    @Column(name = "admin_area1", length = 50, nullable = false)
    private String adminArea1;

    @Column(name = "postal_code", length = 50, nullable = false)
    private String postalCode;

    @Column(name = "country_code", length = 50, nullable = false)
    private String countryCode;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "provider_place_id", length = 10)
    private String providerPlaceId;
}
