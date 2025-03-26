package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.model.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
  private Long id;
  private String tid;
  private String teamName;
  private Long eventId;
  private String eventName;
  private List<ContacDto> contacts;
  private List<String> gidList;
  private boolean isPaid;
  private boolean hasPlayed;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime registeredAt;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ContacDto {
    private String name;
		private String number;
  }

  public static TeamResponse fromTeam(Team team) {
    return TeamResponse.builder()
      .id(team.getId())
      .tid(team.getTid())
      .teamName(team.getTeamName())
      .eventId(team.getEvent().getId())
      .eventName(team.getEvent().getName())
			.contacts(
				team.getContacts().stream()
					.map(contact -> new ContacDto(contact.getName(), contact.getContact()))
					.toList()
			)
      .gidList(team.getGidList())
      .isPaid(team.isPaid())
      .hasPlayed(team.isHasPlayed())
      .registeredAt(team.getRegisteredAt())
      .updatedAt(team.getUpdatedAt())
      .build();
  }
}
