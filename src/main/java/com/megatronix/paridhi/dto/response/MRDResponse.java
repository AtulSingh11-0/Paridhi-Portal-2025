package com.megatronix.paridhi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Department;
import com.megatronix.paridhi.constant.Year;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MRDResponse {
  private Long id;
  private String gid;
  private String name;
  private String email;
  private String contact;
  private String college;
  private Year year;
  private Department department;
  private String rollNo;
  private boolean hasPaid;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime registeredAt;
}