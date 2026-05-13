package com.stockhub.warehouse.repository;

import com.stockhub.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseRepository
        extends JpaRepository<Warehouse, Integer> {

    List<Warehouse> findByIsActiveTrue();

    List<Warehouse> findByManagerId(Integer managerId);
}