package com.debtmanager.debtorservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "debtors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Debtor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String document;

    @Column(length = 120)
    private String email;

    @Column(length = 20)
    private String type;

    @Column(length = 20)
    private String phone;
}
