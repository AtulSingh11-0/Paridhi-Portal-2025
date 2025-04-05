package com.megatronix.paridhi.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorizedMembersResponse {
	private List<MegatronixTeamResponse> members;
	private List<MegatronixTeamResponse> developers;
}
