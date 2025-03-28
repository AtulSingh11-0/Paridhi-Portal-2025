package com.megatronix.paridhi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.megatronix.paridhi.constant.Department;
import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.constant.Year;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false, length = 64)
	private String password;

	private String profilePicture;

	@Column(length = 10)
	private String contact;

	private String college;

	@Enumerated(EnumType.STRING)
	private Year year;

	@Enumerated(EnumType.STRING)
	private Department department;

	@Column(name = "roll_no")
	private String rollNo;

	@OneToMany(
		mappedBy = "user", 
		cascade = CascadeType.ALL, 
		fetch = FetchType.EAGER,
		orphanRemoval = true
	)
	@Builder.Default
	private List<MRD> gids = new ArrayList<>();

	@Builder.Default
	private boolean isPaid = false;

	@Builder.Default
	private boolean isVerified = false;

	@Builder.Default
	private boolean isProfileCreated = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	private LocalDateTime lastLogin;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@JsonIgnore
	public List<String> getGids() {
		if (gids == null) {
			return new ArrayList<>();
		}
		return gids.stream()
			.map(MRD::getGid)
			.toList();
	}

	@PrePersist
	protected void createdAt() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(
			new SimpleGrantedAuthority(this.role.name())
		);
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;	
	}
}
