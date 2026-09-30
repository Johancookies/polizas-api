package com.bolivar.seguros.polizas.repository;

import com.bolivar.seguros.polizas.model.Risk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskRepository extends JpaRepository<Risk, Long> {
}
