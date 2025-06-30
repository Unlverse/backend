package com.tikkeul.mote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_lot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLot {

    @Id
    @Column(name = "admin_id")
    private Long adminId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Column(name = "parking_lot_name", nullable = false, length = 100)
    private String parkingLotName;

    @Column(nullable = false, name = "base_price")
    private int basePrice;

    @Column(nullable = false, name = "price_per_minute")
    private Integer pricePerMinute;

    @Column(nullable = false, name = "total_lot")
    private Integer totalLot;
}
