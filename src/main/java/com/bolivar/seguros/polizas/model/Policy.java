package com.bolivar.seguros.polizas.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "polizas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private PolicyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private PolicyStatus status;

    @Column(name = "vigencia_meses", nullable = false)
    private Integer validityMonths;

    @Column(name = "valor_canon", nullable = false)
    private BigDecimal rentAmount;

    @Column(name = "valor_prima", nullable = false)
    private BigDecimal premiumAmount;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate startDate;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate endDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Risk> risks = new ArrayList<>();

    public void addRisk(Risk risk) {
        risks.add(risk);
        risk.setPolicy(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Policy)) return false;
        Policy policy = (Policy) o;
        return id != null && id.equals(policy.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
