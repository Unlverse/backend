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
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(nullable = false, name = "price_per_minute")
    private Integer pricePerMinute;

    @Column(nullable = false, name = "total_lot")
    private Integer totalLot;

    //주차 기본요금 쓸거면 DB 컬럼도 추가
    @Column(nullable = false, name = "base_price")
    private Integer basePrice;
}
