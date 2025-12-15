package com.weitzel.trustychain.repository;

import com.weitzel.trustychain.model.entity.ProductChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductChainRepository extends JpaRepository<ProductChain, UUID> {
    Optional<ProductChain> findTopByProductCodeOrderByIdDesc(String actor);
    List<ProductChain> findByActorId(String actorId);
}