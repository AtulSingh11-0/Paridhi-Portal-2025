package com.megatronix.paridhi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.Gallery;
import java.util.List;


@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
	@NonNull
	Page<Gallery> findAll(@NonNull Pageable pageable);
	List<Gallery> findByParidhiYear(String paridhiYear);
}
