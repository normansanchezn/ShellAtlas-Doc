package com.shelldocs.core.data.repository

import com.shelldocs.core.common.error.AppError
import com.shelldocs.core.common.result.DomainResult
import com.shelldocs.core.common.time.TimeProvider
import com.shelldocs.core.data.mapper.ContentBlockDtoMapper
import com.shelldocs.core.data.mapper.DocumentDtoMapper
import com.shelldocs.core.data.markdown.MarkdownParser
import com.shelldocs.core.data.network.dto.ContentJsonDto
import com.shelldocs.core.data.network.dto.DocumentAttributesDto
import com.shelldocs.core.data.network.dto.DocumentDto
import com.shelldocs.core.data.network.dto.DocumentVersionDto
import com.shelldocs.core.data.supabase.SupabasePostgrestApi
import com.shelldocs.core.data.supabase.SupabasePostgrestException
import com.shelldocs.core.domain.entity.document.Document
import com.shelldocs.core.domain.entity.document.DocumentAttributes
import com.shelldocs.core.domain.entity.document.DocumentVersion
import com.shelldocs.core.domain.entity.document.DraftReceipt
import com.shelldocs.core.domain.repository.DocumentRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SupabaseDocumentRepository(
    private val postgrest: SupabasePostgrestApi,
    private val timeProvider: TimeProvider,
    private val currentUserIdProvider: () -> String?,
    private val markdownParser: MarkdownParser = MarkdownParser(),
) : DocumentRepository {

    override suspend fun documents(): DomainResult<List<Document>> = guard {
        fetchDocuments()
    }

    override suspend fun document(id: String): DomainResult<Document> = guard {
        fetchDocuments(ids = listOf(id)).firstOrNull()
            ?: throw MissingResource("Document $id not found")
    }

    override suspend fun search(query: String): DomainResult<List<Document>> = guard {
        val needle = query.trim().lowercase()
        if (needle.isBlank()) return@guard emptyList()
        fetchDocuments().filter { document ->
            needle in document.title.lowercase() ||
                needle in document.summary.lowercase() ||
                needle in document.plainText.lowercase() ||
                document.attributes.tags.any { needle in it.lowercase() } ||
                needle in document.attributes.owner.lowercase() ||
                needle in document.attributes.module.lowercase() ||
                needle in document.attributes.team.lowercase() ||
                needle in document.attributes.platform.lowercase()
        }
    }

    override suspend fun create(
        title: String,
        markdown: String,
        parentFolderId: String?,
    ): DomainResult<Document> = guard {
        val parsed = markdownParser.parse(markdown)
        val now = nowIso()
        val createdBy = currentUserIdProvider()
        val documentRow = postgrest.insert<List<SupabaseDocumentRow>, CreateDocumentRow>(
            table = "documents",
            body = CreateDocumentRow(
                title = title,
                slug = slugify(title),
                status = "draft",
                classification = "internal",
                createdBy = createdBy,
                updatedBy = createdBy,
                updatedAt = now,
            ),
        ).first()
        val versionRow = postgrest.insert<List<SupabaseVersionRow>, CreateVersionRow>(
            table = "document_versions",
            body = CreateVersionRow(
                documentId = documentRow.id,
                versionNumber = 1,
                title = title,
                rawMarkdown = markdown,
                contentJson = ContentJsonDto(
                    schemaVersion = parsed.content.schemaVersion,
                    blocks = parsed.content.blocks.map(ContentBlockDtoMapper::toDto),
                ),
                contentPlaintext = parsed.plainText,
                contentHash = parsed.contentHash,
                changeSummary = "Initial version",
                createdBy = createdBy,
            ),
        ).first()
        postgrest.update<List<SupabaseDocumentRow>, UpdateDocumentRow>(
            table = "documents",
            query = "id=eq.${documentRow.id}",
            body = UpdateDocumentRow(
                currentVersionId = versionRow.id,
                updatedBy = createdBy,
                updatedAt = now,
            ),
        )
        upsertAttributes(
            documentId = documentRow.id,
            attributes = DocumentAttributes(parentFolderId = parentFolderId),
            summary = "",
        )
        fetchDocuments(ids = listOf(documentRow.id)).first()
    }

    override suspend fun publish(
        id: String,
        markdown: String,
        changeSummary: String,
    ): DomainResult<Document> = guard {
        val existing = requireDocumentRow(id)
        val parsed = markdownParser.parse(markdown)
        val now = nowIso()
        val nextVersion = nextVersionNumber(id)
        val createdBy = currentUserIdProvider()
        val versionRow = postgrest.insert<List<SupabaseVersionRow>, CreateVersionRow>(
            table = "document_versions",
            body = CreateVersionRow(
                documentId = id,
                versionNumber = nextVersion,
                title = existing.title,
                rawMarkdown = markdown,
                contentJson = ContentJsonDto(
                    schemaVersion = parsed.content.schemaVersion,
                    blocks = parsed.content.blocks.map(ContentBlockDtoMapper::toDto),
                ),
                contentPlaintext = parsed.plainText,
                contentHash = parsed.contentHash,
                changeSummary = changeSummary,
                createdBy = createdBy,
            ),
        ).first()
        postgrest.update<List<SupabaseDocumentRow>, UpdateDocumentRow>(
            table = "documents",
            query = "id=eq.$id",
            body = UpdateDocumentRow(
                status = "published",
                currentVersionId = versionRow.id,
                updatedBy = createdBy,
                updatedAt = now,
            ),
        )
        fetchDocuments(ids = listOf(id)).first()
    }

    override suspend fun saveDraft(id: String, markdown: String): DomainResult<DraftReceipt> = guard {
        val parsed = markdownParser.parse(markdown)
        val userId = currentUserIdProvider() ?: throw ValidationFailure("Sign in again before saving drafts.")
        val currentVersionId = requireDocumentRow(id).currentVersionId
        postgrest.upsert(
            table = "document_drafts",
            body = listOf(
                DraftRow(
                    documentId = id,
                    userId = userId,
                    baseVersionId = currentVersionId,
                    rawMarkdown = markdown,
                    contentJson = ContentJsonDto(
                        schemaVersion = parsed.content.schemaVersion,
                        blocks = parsed.content.blocks.map(ContentBlockDtoMapper::toDto),
                    ),
                    contentPlaintext = parsed.plainText,
                    contentHash = parsed.contentHash,
                    updatedAt = nowIso(),
                ),
            ),
        )
        DraftReceipt(
            documentId = id,
            contentHash = parsed.contentHash,
            savedAt = timeProvider.now(),
        )
    }

    override suspend fun versions(id: String): DomainResult<List<DocumentVersion>> = guard {
        postgrest.select<List<SupabaseVersionRow>>(
            table = "document_versions",
            query = "select=*&document_id=eq.$id&order=version_number.desc",
        ).map { row ->
            DocumentVersionDto(
                id = row.id,
                documentId = row.documentId,
                versionNumber = row.versionNumber,
                title = row.title,
                rawMarkdown = row.rawMarkdown,
                changeSummary = row.changeSummary.orEmpty(),
                createdAt = row.createdAt,
            )
        }.map { dto ->
            DocumentVersion(
                id = dto.id,
                documentId = dto.documentId,
                versionNumber = dto.versionNumber,
                title = dto.title,
                rawMarkdown = dto.rawMarkdown,
                changeSummary = dto.changeSummary,
                createdAt = kotlin.time.Instant.parse(dto.createdAt),
            )
        }
    }

    override suspend fun restoreVersion(id: String, versionId: String): DomainResult<Document> = guard {
        val existing = requireDocumentRow(id)
        val sourceVersion = postgrest.select<List<SupabaseVersionRow>>(
            table = "document_versions",
            query = "select=*&id=eq.$versionId&document_id=eq.$id",
        ).firstOrNull() ?: throw MissingResource("Version $versionId not found")
        val nextVersion = nextVersionNumber(id)
        val now = nowIso()
        val createdBy = currentUserIdProvider()
        val restored = postgrest.insert<List<SupabaseVersionRow>, CreateVersionRow>(
            table = "document_versions",
            body = CreateVersionRow(
                documentId = id,
                versionNumber = nextVersion,
                title = sourceVersion.title,
                rawMarkdown = sourceVersion.rawMarkdown,
                contentJson = sourceVersion.contentJson,
                contentPlaintext = sourceVersion.contentPlaintext,
                contentHash = "${sourceVersion.contentHash}-restore-${timeProvider.now().toEpochMilliseconds()}",
                changeSummary = "Restored from version ${sourceVersion.versionNumber}",
                sourceVersion = sourceVersion.id,
                createdBy = createdBy,
            ),
        ).first()
        postgrest.update<List<SupabaseDocumentRow>, UpdateDocumentRow>(
            table = "documents",
            query = "id=eq.$id",
            body = UpdateDocumentRow(
                title = existing.title,
                status = "published",
                currentVersionId = restored.id,
                updatedBy = createdBy,
                updatedAt = now,
            ),
        )
        fetchDocuments(ids = listOf(id)).first()
    }

    override suspend fun updateAttributes(id: String, attributes: DocumentAttributes): DomainResult<Document> = guard {
        upsertAttributes(id, attributes, summary = null)
        postgrest.update<List<SupabaseDocumentRow>, UpdateDocumentRow>(
            table = "documents",
            query = "id=eq.$id",
            body = UpdateDocumentRow(
                updatedBy = currentUserIdProvider(),
                updatedAt = nowIso(),
            ),
        )
        fetchDocuments(ids = listOf(id)).first()
    }

    override suspend fun delete(id: String): DomainResult<Unit> = guard {
        postgrest.update<List<SupabaseDocumentRow>, UpdateDocumentRow>(
            table = "documents",
            query = "id=eq.$id",
            body = UpdateDocumentRow(
                status = "deleted_source",
                deletedAt = nowIso(),
                updatedBy = currentUserIdProvider(),
                updatedAt = nowIso(),
            ),
        )
        Unit
    }

    private suspend fun fetchDocuments(ids: List<String>? = null): List<Document> {
        val query = buildString {
            append("select=id,title,slug,status,classification,current_version_id,created_at,updated_at")
            append("&deleted_at=is.null")
            if (!ids.isNullOrEmpty()) append("&id=in.(${ids.joinToString(",")})")
            append("&order=title.asc")
        }
        val documents = postgrest.select<List<SupabaseDocumentRow>>("documents", query)
        if (documents.isEmpty()) return emptyList()

        val versionIds = documents.mapNotNull { it.currentVersionId }.distinct()
        val docIds = documents.map { it.id }
        val versions = if (versionIds.isEmpty()) {
            emptyList()
        } else {
            postgrest.select<List<SupabaseVersionRow>>(
                "document_versions",
                "select=*&id=in.(${versionIds.joinToString(",")})",
            )
        }
        val attributes = postgrest.select<List<SupabaseAttributeRow>>(
            "document_attributes",
            "select=document_id,key,value&document_id=in.(${docIds.joinToString(",")})",
        )
        val versionById = versions.associateBy { it.id }
        return documents.map { document ->
            val version = document.currentVersionId?.let(versionById::get)
            val attrs = attributes.filter { it.documentId == document.id }
            toDomain(document, version, attrs)
        }
    }

    private fun toDomain(
        document: SupabaseDocumentRow,
        version: SupabaseVersionRow?,
        attributes: List<SupabaseAttributeRow>,
    ): Document {
        val dto = DocumentDto(
            id = document.id,
            title = document.title,
            summary = attributes.textValue("summary"),
            status = document.status,
            classification = document.classification,
            rawMarkdown = version?.rawMarkdown.orEmpty(),
            contentJson = version?.contentJson ?: ContentJsonDto(),
            contentPlaintext = version?.contentPlaintext.orEmpty(),
            attributes = DocumentAttributesDto(
                owner = attributes.textValue("owner"),
                module = attributes.textValue("module"),
                team = attributes.textValue("team"),
                platform = attributes.textValue("platform"),
                parentFolderId = attributes.textValue("parent_folder_id").ifBlank { null },
                tags = attributes.listValue("tags"),
            ),
            createdAt = document.createdAt,
            updatedAt = document.updatedAt,
        )
        return DocumentDtoMapper.toDomain(dto)
    }

    private suspend fun upsertAttributes(
        documentId: String,
        attributes: DocumentAttributes,
        summary: String?,
    ) {
        val rows = buildList {
            add(AttributeUpsertRow(documentId, "owner", jsonOf(attributes.owner)))
            add(AttributeUpsertRow(documentId, "module", jsonOf(attributes.module)))
            add(AttributeUpsertRow(documentId, "team", jsonOf(attributes.team)))
            add(AttributeUpsertRow(documentId, "platform", jsonOf(attributes.platform)))
            add(AttributeUpsertRow(documentId, "parent_folder_id", jsonOf(attributes.parentFolderId.orEmpty())))
            add(AttributeUpsertRow(documentId, "tags", JsonArray(attributes.tags.map(::JsonPrimitive))))
            summary?.let { add(AttributeUpsertRow(documentId, "summary", jsonOf(it))) }
        }
        postgrest.upsert("document_attributes", rows)
    }

    private suspend fun requireDocumentRow(id: String): SupabaseDocumentRow =
        postgrest.select<List<SupabaseDocumentRow>>(
            "documents",
            "select=id,title,slug,status,classification,current_version_id,created_at,updated_at&id=eq.$id&deleted_at=is.null",
        ).firstOrNull() ?: throw MissingResource("Document $id not found")

    private suspend fun nextVersionNumber(documentId: String): Int =
        postgrest.select<List<SupabaseVersionNumberRow>>(
            "document_versions",
            "select=version_number&document_id=eq.$documentId&order=version_number.desc&limit=1",
        ).firstOrNull()?.versionNumber?.plus(1) ?: 1

    private fun nowIso(): String = timeProvider.now().toString()

    private fun slugify(title: String): String =
        buildString {
            title.lowercase().forEach { char ->
                append(
                    when {
                        char.isLetterOrDigit() -> char
                        length == 0 || last() == '-' -> ""
                        else -> "-"
                    },
                )
            }
        }.trim('-') + "-${timeProvider.now().toEpochMilliseconds()}"

    private suspend inline fun <T> guard(crossinline block: suspend () -> T): DomainResult<T> = try {
        DomainResult.success(block())
    } catch (exception: ValidationFailure) {
        DomainResult.failure(AppError.Validation(exception.message ?: "Please review the information."))
    } catch (exception: MissingResource) {
        DomainResult.failure(AppError.NotFound(exception.message ?: "Resource not found."))
    } catch (exception: SupabasePostgrestException) {
        val message = exception.message.orEmpty()
        DomainResult.failure(
            when {
                "401" in message || "403" in message -> AppError.Unauthorized()
                "404" in message -> AppError.NotFound()
                else -> AppError.Network("Unable to reach Supabase")
            },
        )
    } catch (exception: Exception) {
        DomainResult.failure(AppError.Unknown(exception.message ?: "Unexpected error"))
    }

    private class ValidationFailure(message: String) : IllegalStateException(message)
    private class MissingResource(message: String) : IllegalStateException(message)
}

private fun List<SupabaseAttributeRow>.textValue(key: String): String =
    firstOrNull { it.key == key }?.value?.jsonPrimitive?.contentOrNull.orEmpty()

private fun List<SupabaseAttributeRow>.listValue(key: String): List<String> =
    (firstOrNull { it.key == key }?.value as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty()

private fun jsonOf(value: String): JsonElement = JsonPrimitive(value)

@Serializable
private data class SupabaseDocumentRow(
    val id: String,
    val title: String,
    val slug: String,
    val status: String,
    val classification: String = "internal",
    @SerialName("current_version_id") val currentVersionId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
private data class SupabaseVersionRow(
    val id: String,
    @SerialName("document_id") val documentId: String,
    @SerialName("version_number") val versionNumber: Int,
    val title: String,
    @SerialName("raw_markdown") val rawMarkdown: String,
    @SerialName("content_json") val contentJson: ContentJsonDto = ContentJsonDto(),
    @SerialName("content_plaintext") val contentPlaintext: String = "",
    @SerialName("content_hash") val contentHash: String = "",
    @SerialName("change_summary") val changeSummary: String? = null,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
private data class SupabaseVersionNumberRow(
    @SerialName("version_number") val versionNumber: Int,
)

@Serializable
private data class SupabaseAttributeRow(
    @SerialName("document_id") val documentId: String,
    val key: String,
    val value: JsonElement,
)

@Serializable
private data class CreateDocumentRow(
    val title: String,
    val slug: String,
    val status: String,
    val classification: String,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("updated_by") val updatedBy: String? = null,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
private data class UpdateDocumentRow(
    val title: String? = null,
    val status: String? = null,
    @SerialName("current_version_id") val currentVersionId: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("updated_by") val updatedBy: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
private data class CreateVersionRow(
    @SerialName("document_id") val documentId: String,
    @SerialName("version_number") val versionNumber: Int,
    val title: String,
    @SerialName("raw_markdown") val rawMarkdown: String,
    @SerialName("content_json") val contentJson: ContentJsonDto,
    @SerialName("content_plaintext") val contentPlaintext: String,
    @SerialName("content_hash") val contentHash: String,
    @SerialName("change_summary") val changeSummary: String,
    @SerialName("source_version") val sourceVersion: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
)

@Serializable
private data class DraftRow(
    @SerialName("document_id") val documentId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("base_version_id") val baseVersionId: String? = null,
    @SerialName("raw_markdown") val rawMarkdown: String,
    @SerialName("content_json") val contentJson: ContentJsonDto,
    @SerialName("content_plaintext") val contentPlaintext: String,
    @SerialName("content_hash") val contentHash: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
private data class AttributeUpsertRow(
    @SerialName("document_id") val documentId: String,
    val key: String,
    val value: JsonElement,
    val source: String = "app",
)
