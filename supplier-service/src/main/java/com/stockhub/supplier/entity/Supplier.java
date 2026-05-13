package com.stockhub.supplier.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    // Primary key - auto generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Integer supplierId;

    // Company name of supplier
    @Column(name = "name", nullable = false)
    private String name;

    // Contact person name
    @Column(name = "contact_person")
    private String contactPerson;

    // Supplier email
    @Column(name = "email")
    private String email;

    // Supplier phone
    @Column(name = "phone")
    private String phone;

    // Supplier city
    @Column(name = "city")
    private String city;

    // Supplier country
    @Column(name = "country")
    private String country;

    // Payment terms like NET-30, NET-60
    // NET-30 means payment due in 30 days
    @Column(name = "payment_terms")
    private String paymentTerms;

    // How many days to deliver after order
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    // Rating out of 5 updated after each delivery
    @Column(name = "rating")
    @Builder.Default
    private Double rating = 0.0;

    // Total number of ratings received
    // Used to calculate average rating
    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    // False means no new POs allowed
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // Auto set when record created
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Auto set when record updated
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}