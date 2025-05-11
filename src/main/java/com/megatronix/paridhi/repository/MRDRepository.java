package com.megatronix.paridhi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.MRD;
import com.megatronix.paridhi.model.User;

@Repository
public interface MRDRepository extends JpaRepository<MRD, Long> {
  List<MRD> findAllByUser(User user);
  Optional<MRD> findByGid(String gid);
  boolean existsByGid(String gid);
  List<MRD> findByIsPaid(boolean isPaid);
  boolean existsByGidAndUserEmail(String gid, String email);
	boolean existsByGidAndIsPaidTrue(String gid);

	@Query("SELECT DISTINCT m.user.email FROM MRD m WHERE m.gid IN :gidList")
	List<String> findUserEmailListByGidList(@Param("gidList") List<String> gidList);

	@Query("SELECT m.gid FROM MRD m WHERE m.user.email = :email")
	List<String> findGidListByUserEmail(@Param("email") String email);
}
