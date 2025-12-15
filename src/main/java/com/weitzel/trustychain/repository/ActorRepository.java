package com.weitzel.trustychain.repository;

import com.weitzel.trustychain.model.entity.Actor;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActorRepository extends JpaRepository<Actor, UUID> {
}
