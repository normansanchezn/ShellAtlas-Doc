package com.shelldocs.core.domain.usecase.assistant

import com.shelldocs.core.domain.entity.assistant.AssistantAvailability
import com.shelldocs.core.domain.repository.AssistantEngine

class CheckAssistantAvailabilityUseCase(private val engine: AssistantEngine) {

    suspend operator fun invoke(): AssistantAvailability = engine.availability()
}
