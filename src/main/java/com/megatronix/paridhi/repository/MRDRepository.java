package com.megatronix.paridhi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.megatronix.paridhi.model.MRD;
import com.megatronix.paridhi.model.User;

@Repository
public interface MRDRepository extends JpaRepository<MRD, Long> {
  List<MRD> findAllByUser(User user);
  Optional<MRD> findByGid(String gid);
  boolean existsByGid(String gid);
  List<MRD> findByIsPaid(boolean isPaid);
}
