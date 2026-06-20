package com.shelldocs.core.domain.entity.document

/** A classifiable document metadata field. */
enum class MetadataAttribute(val displayName: String, val required: Boolean) {
    OWNER("Owner", required = true),
    DEVELOPMENT_AREA("Development Area", required = true),
    APPLICATION_VERSION("Application Version", required = true),
    MODULE("Module", required = false),
    PLATFORM("Platform", required = false),
    TEAM("Team", required = false),
    DOCUMENT_TYPE("Document Type", required = false),
    TAGS("Tags", required = false);

    companion object {
        val REQUIRED: List<MetadataAttribute> = entries.filter { it.required }
    }
}
