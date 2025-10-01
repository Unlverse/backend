package com.tikkeul.mote.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscription",
        uniqueConstraints = @UniqueConstraint(columnNames = {"admin_id", "sub_plate"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @Column(name = "sub_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "sub_plate", nullable = false, length = 50)
    private String subPlate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "sub_price")
    private int subPrice;

    @Column(length = 500)
    private String memo;
}