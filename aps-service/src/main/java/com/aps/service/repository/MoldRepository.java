package com.aps.service.repository;

import com.aps.domain.entity.Mold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MoldRepository extends JpaRepository<Mold, UUID> {

    Optional<Mold> findByMoldCode(String moldCode);

    boolean existsByMoldCode(String moldCode);

    boolean existsByMoldCodeAndIdNot(String moldCode, UUID id);

    List<Mold> findAllByOrderByMoldCodeAsc();
}
