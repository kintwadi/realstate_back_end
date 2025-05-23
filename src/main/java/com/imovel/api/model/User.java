package com.imovel.api.model;

import jakarta.persistence.*; //
import lombok.Data; //
import java.time.LocalDateTime; //
import java.util.List; //
import org.hibernate.annotations.CreationTimestamp; //
import org.hibernate.annotations.UpdateTimestamp; //

/**
 * Represents a user in the system
 */
@Entity //
@Table(name = "users") //
@Data //
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; //

    @Column(nullable = false, unique = true, length = 100) //
    private String email; //

    @Column(nullable = false) //
    private String password; //

    @Column(length = 20) //
    private String phone; //

    @Column //
    private String avatar; //

    @Enumerated(EnumType.STRING) //
    @Column(nullable = false) //
    private UserRole role; //

    @ElementCollection //
    @CollectionTable(name = "user_social_links", joinColumns = @JoinColumn(name = "user_id")) //
    @Column(name = "social_link") //
    private List<String> socialLinks; //

    @CreationTimestamp //
    @Column(nullable = false, updatable = false) //
    private LocalDateTime createdAt; //

    @UpdateTimestamp //
    @Column(nullable = false) //
    private LocalDateTime updatedAt; //
}