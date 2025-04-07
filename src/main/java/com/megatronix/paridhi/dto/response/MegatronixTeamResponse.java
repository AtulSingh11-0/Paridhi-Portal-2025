package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Designation;
import com.megatronix.paridhi.constant.Year;
import com.megatronix.paridhi.model.MegatronixTeam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MegatronixTeamResponse {
	private Long id;
	private String name;
	private String email;
	private Year year;
	private Map<String, String> socialLinks;
	private String imageLink;
	private Designation designation;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

	public static MegatronixTeamResponse fromMegatronixTeam(MegatronixTeam member) {
		return MegatronixTeamResponse.builder()
			.id(member.getId())
			.name(member.getName())
			.email(member.getEmail())
			.year(member.getYear())
			.socialLinks(new HashMap<>(
				Map.of(
					"linkedInLink", member.getLinkedInLink(),
					"facebookLink", member.getFacebookLink(),
					"instagramLink", member.getInstagramLink(),
					"githubLink", member.getGithubLink()
				).entrySet().stream()
					.collect(Collectors.toMap(
						Map.Entry::getKey, e -> e.getValue() == null ? "N/A" : e.getValue()
					))
			))
			.imageLink(member.getImageLink())
			.designation(member.getDesignation())
			.createdAt(member.getCreatedAt())
			.updatedAt(member.getUpdatedAt())
			.build();
	}
}
