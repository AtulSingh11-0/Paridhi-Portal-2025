package com.megatronix.paridhi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.User;

@Repository
public interface UserRepository extends JpaRepository< User, Long > {
	boolean existsByEmail ( String email );
	Optional< User> findUserByEmail ( String email );
	List< User> findAllByIsProfileCreated ( boolean isProfileCreated );
}
