package com.example.aprbackendassignment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Friend {
    @Id
    private Long id;
}
