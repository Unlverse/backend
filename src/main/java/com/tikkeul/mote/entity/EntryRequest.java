package com.tikkeul.mote.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_request",
        uniqueConstraints = @UniqueConstraint(columnNames = {"admin_id", "new_plate"})
)
@Setter
@Getter
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
