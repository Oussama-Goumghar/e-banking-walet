package com.ensa.repository;

import com.ensa.domain.Walet;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the Walet entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WaletRepository extends JpaRepository<Walet, Long> {}
