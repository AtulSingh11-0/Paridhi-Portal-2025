package com.megatronix.paridhi.repository;

import com.megatronix.paridhi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository< User, Long > {
	boolean existsByEmail ( String email );
	Optional< User> findUserByEmail ( String email );
}
