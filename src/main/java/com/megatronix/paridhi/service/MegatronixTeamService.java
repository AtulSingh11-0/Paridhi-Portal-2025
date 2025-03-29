package com.megatronix.paridhi.service;

import java.util.Comparator;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.megatronix.paridhi.constant.Role;
import com.megatronix.paridhi.constant.Year;
import com.megatronix.paridhi.dto.request.MegatronixTeamRequest;
import com.megatronix.paridhi.dto.response.MegatronixTeamResponse;
import com.megatronix.paridhi.exception.ForbiddenAccessException;
import com.megatronix.paridhi.exception.MemberProfileAlreadyExistsException;
import com.megatronix.paridhi.exception.MemberProfileNotFoundException;
import com.megatronix.paridhi.model.MegatronixTeam;
import com.megatronix.paridhi.model.User;
import com.megatronix.paridhi.repository.MegatronixTeamRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MegatronixTeamService {
	private final MegatronixTeamRepository megatronixTeamRepository;

	@Transactional
	public MegatronixTeamResponse createMemberProfile(MegatronixTeamRequest request, User user) {
		log.info("Creating member profile for: {}, year: {}, by: {}", request.getName(), request.getYear(), user.getEmail());

		// check if user has permission to access this resource
		checkUserPermission(user, "create");

		// check if member profile already exists
		if ( megatronixTeamRepository.existsByEmail(request.getEmail()) ) {
			log.error("Member profile already exists for email: {}", request.getEmail());
			throw new MemberProfileAlreadyExistsException("Member profile already exists for this email");
		}

		// create member profile and save to database
		MegatronixTeam member = MegatronixTeam.builder()
			.name(request.getName())
			.email(request.getEmail())
			.year(request.getYear())
			.linkedInLink(request.getLinkedInLink() == null ? "N/A" : request.getLinkedInLink())
			.facebookLink(request.getFacebookLink() == null ? "N/A" : request.getFacebookLink())
			.instagramLink(request.getInstagramLink() == null ? "N/A" : request.getInstagramLink())
			.githubLink(request.getGithubLink() == null ? "N/A" : request.getGithubLink())
			.imageLink(request.getImageLink())
			.build();

		MegatronixTeam savedMember = megatronixTeamRepository.save(member);
		log.info("Member profile created successfully for: {}, year: {}", savedMember.getName(), savedMember.getYear());

		// return a response of type MegatronixTeamResponse object
		return MegatronixTeamResponse.fromMegatronixTeam(savedMember);
	}

	@Transactional
	public MegatronixTeamResponse updateMemberProfile(Long id, MegatronixTeamRequest request, User user) {
		log.info("Updating member profile for: {}, year: {}, by: {}", request.getName(), request.getYear(), user.getEmail());

		// check if user has permission to access this resource
		checkUserPermission(user, "update");

		// check if member profile already exists
		var existingMember = megatronixTeamRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Member profile not found for id: {}", id);
				return new MemberProfileNotFoundException("Member profile not found for ID: " + id);
			});
		
		// update member profile and save to database
		existingMember.setName(request.getName());
		existingMember.setEmail(request.getEmail());
		existingMember.setYear(request.getYear());
		existingMember.setLinkedInLink(request.getLinkedInLink() == null ? existingMember.getLinkedInLink() : request.getLinkedInLink());
		existingMember.setFacebookLink(request.getFacebookLink() == null ? existingMember.getFacebookLink() : request.getFacebookLink());
		existingMember.setInstagramLink(request.getInstagramLink() == null ? existingMember.getInstagramLink() : request.getInstagramLink());
		existingMember.setGithubLink(request.getGithubLink() == null ? existingMember.getGithubLink() : request.getGithubLink());
		existingMember.setImageLink(request.getImageLink() == null ? existingMember.getImageLink() : request.getImageLink());

		MegatronixTeam updatedMember = megatronixTeamRepository.save(existingMember);
		log.info("Member profile updated successfully for: {}, year: {}", updatedMember.getName(), updatedMember.getYear());

		// return a response of type MegatronixTeamResponse object
		return MegatronixTeamResponse.fromMegatronixTeam(updatedMember);
	}

	@Transactional
	public void deleteMemberProfile(Long id, User user) {
		log.info("Deleting member profile with ID: {}, by: {}", id, user.getEmail());
		
		// check if user has permission to access this resource
		checkUserPermission(user, "delete");
		
		// check if member profile exists
		var existingMember = megatronixTeamRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Member profile not found for id: {}", id);
				return new MemberProfileNotFoundException("Member profile not found for ID: " + id);
			});
		
		// delete the member profile
		megatronixTeamRepository.delete(existingMember);
		log.info("Member profile deleted successfully for: {}, year: {}", existingMember.getName(), existingMember.getYear());
	}

	public Page<MegatronixTeamResponse> getAllMemberProfilesSortedByYear(int page, int size) {
		log.info("Fetching all member profiles sorted by year, page: {}, size: {}", page, size);
		Page<MegatronixTeam> members = megatronixTeamRepository.findAll(PageRequest.of(page, size));

		// custom comparator for year enum
		Comparator<MegatronixTeam> yearComparator = (m1, m2) -> {
			// defining a custom order for the years
			Map<Year, Integer> yearOrder = Map.of(
				Year.FOURTH, 4,
				Year.THIRD, 3,
				Year.SECOND, 2,
				Year.FIRST, 1
			);

			return Integer.compare(
				yearOrder.getOrDefault(m2.getYear(), -1),
				yearOrder.getOrDefault(m1.getYear(), -1)
			);
		};
		
		// sort the members by year using the custom comparator
		var sortedMembers = members.getContent().stream()
			.sorted(yearComparator)
			.toList();

		// create a new page with the sorted members
		members = new PageImpl<>(sortedMembers, members.getPageable(), members.getTotalElements());
		log.info("Fetched {} member profiles sorted by year", members.getTotalElements());
		
		// return a page of type MegatronixTeamResponse object
		return members.map(MegatronixTeamResponse::fromMegatronixTeam);
	}

	private void checkUserPermission(User user, String methodType) {
		if ( !user.getRole().equals(Role.ROLE_SUPERADMIN) ) {
			log.error("User: {} does not have permission to {} member profile", user.getEmail(), methodType);
			throw new ForbiddenAccessException("User does not have permission to access this resource");
		}
	}
}
