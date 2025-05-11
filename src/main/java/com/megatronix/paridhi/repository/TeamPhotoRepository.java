package com.megatronix.paridhi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.TeamPhoto;

@Repository
public interface TeamPhotoRepository extends JpaRepository<TeamPhoto, Long> {
}
