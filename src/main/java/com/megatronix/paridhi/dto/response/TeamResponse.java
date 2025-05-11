package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Position;
import com.megatronix.paridhi.model.Event;
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
	private List<ContactDto> contacts;
	private List<String> gidList;
	private boolean isPaid;
	private boolean hasPlayed;
	private boolean isQualified;
	private Position position;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registeredAt;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ContactDto {
		private String name;
		private String contact;
	}

	public static TeamResponse fromTeam(Team team) {

		Event event = team.getEvent();
		Long eventId = event != null ? event.getId() : null;
		String eventName = event != null ? event.getName() : null;

		List<String> gidList = new ArrayList<>(team.getGidList());

		return TeamResponse.builder()
				.id(team.getId())
				.tid(team.getTid())
				.teamName(team.getTeamName())
				.eventId(eventId)
				.eventName(eventName)
				.contacts(team.getContacts().stream()
						.map(contact -> ContactDto.builder()
								.name(contact.getName())
								.contact(contact.getContact())
								.build())
						.toList())
				.gidList(gidList)
				.isPaid(team.isPaid())
				.hasPlayed(team.isHasPlayed())
				.isQualified(team.isQualified())
				.position(team.getPosition())
				.registeredAt(team.getRegisteredAt())
				.updatedAt(team.getUpdatedAt())
				.build();
	}
}
