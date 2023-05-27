package com.dirtify.songstreamingmicroservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<SongModel, Long> {
    @Override
    boolean existsById(Long aLong);

    @Override
    Optional<SongModel> findById(Long aLong);
}
