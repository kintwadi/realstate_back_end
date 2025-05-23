package com.imovel.api.model; //

import jakarta.persistence.Column; //
import jakarta.persistence.Embeddable; //
import lombok.Data; //

@Embeddable //
@Data //
public class Location {
    @Column(name = "address") //
    private String address; //

    @Column(name = "city") //
    private String city; //

    @Column(name = "state") //
    private String state; //

    @Column(name = "country") //
    private String country; //

    @Column(name = "latitude") //
    private Double latitude; //

    @Column(name = "longitude") //
    private Double longitude; //
}