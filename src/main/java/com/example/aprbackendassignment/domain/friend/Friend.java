package com.example.aprbackendassignment.domain.friend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "friends",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_friends_pair", columnNames = {"user_a", "user_b"})
        },
        indexes = {
                @Index(name = "ix_friends_a_created_at", columnList = "user_a, created_at DESC"),
                @Index(name = "ix_friends_b_created_at", columnList = "user_b, created_at DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_a", nullable = false)
    private Long userA;

    @Column(name = "user_b", nullable = false)
    private Long userB;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}
