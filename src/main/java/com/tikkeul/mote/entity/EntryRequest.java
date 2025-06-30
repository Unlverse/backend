package com.tikkeul.mote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entry_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "new_plate", nullable = false, length = 50)
    private String newPlate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;
}
