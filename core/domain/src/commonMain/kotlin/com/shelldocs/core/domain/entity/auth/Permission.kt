package com.shelldocs.core.domain.entity.auth

/** Fine-grained capabilities; roles are mapped to these in [RolePermissions]. */
enum class Permission {
    VIEW_DOCUMENTS,
    EDIT_DOCUMENTS,
    PUBLISH_DOCUMENTS,
    DELETE_DOCUMENTS,
    USE_ASSISTANT,
    VIEW_ANALYTICS,
    RUN_SOURCE_SYNC,
    MANAGE_INTEGRATIONS,
    MANAGE_MEMBERS,
}
