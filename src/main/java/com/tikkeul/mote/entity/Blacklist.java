package com.tikkeul.mote.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "black_list",
        uniqueConstraints = @UniqueConstraint(columnNames = {"admin_id", "black_plate"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "black_id")
    private Long blackId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "black_plate", nullable = false, length = 50)
    private String blackPlate;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "black_timestamp", nullable = false, updatable = false)
    private LocalDateTime blackTimestamp;

    @PrePersist
    public void prePersist() {
        this.blackTimestamp = LocalDateTime.now();
    }
}
