package com.shelldocs.core.domain.usecase.auth

import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.domain.entity.auth.TeamMember
import com.shelldocs.core.domain.repository.RoleRepository

class GetTeamMembersUseCase(private val roleRepository: RoleRepository) {

    suspend operator fun invoke(): DomainResult<List<TeamMember>> = roleRepository.teamMembers()
}
