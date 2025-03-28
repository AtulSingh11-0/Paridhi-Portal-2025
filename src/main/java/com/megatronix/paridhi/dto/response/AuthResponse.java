package com.megatronix.paridhi.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.megatronix.paridhi.constant.Department;
import com.megatronix.paridhi.constant.Year;
import com.megatronix.paridhi.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
	private String token;
	private UserDto user;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserDto {
		private Long id;
		private String profilePicture;
		private String name;
		private String email;
		private String contact;
		private String college;
		private Year year;
		private Department department;
		private String roll;
		private List<String> gids;
		private boolean isPaid;
		private boolean isVerified;
		private boolean isProfileCreated;
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime lastLogin;
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime createdAt;
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime updatedAt;

		public static UserDto fromUser(User user) {
			return UserDto.builder()
				.id(user.getId())
				.profilePicture(user.getProfilePicture())
				.name(user.getName())
				.email(user.getEmail())
				.contact(user.getContact())
				.college(user.getCollege())
				.year(user.getYear())
				.department(user.getDepartment())
				.roll(user.getRollNo())
				.gids(user.getGids())
				.isPaid(user.isPaid())
				.isVerified(user.isVerified())
				.isProfileCreated(user.isProfileCreated())
				.lastLogin(user.getLastLogin())
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.build();
		}
	}
}