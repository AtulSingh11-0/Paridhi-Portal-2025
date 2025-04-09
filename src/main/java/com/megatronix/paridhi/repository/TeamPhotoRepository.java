package com.megatronix.paridhi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.TeamPhoto;
import java.util.List;
import com.megatronix.paridhi.constant.Category;


@Repository
public interface TeamPhotoRepository extends JpaRepository<TeamPhoto, Long> {
	List<TeamPhoto> findByCategory(Category category);
}
