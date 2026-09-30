package com.bolivar.seguros.polizas.repository;

import com.bolivar.seguros.polizas.model.PolicyStatus;
import com.bolivar.seguros.polizas.model.Policy;
import com.bolivar.seguros.polizas.model.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByTypeAndStatus(PolicyType type, PolicyStatus status);
    List<Policy> findByType(PolicyType type);
    List<Policy> findByStatus(PolicyStatus status);
}
