import Foundation
import SD_Domain
import DS_Core
import SwiftUI

@MainActor
@Observable
final class EditorViewModel {

    var draft: DocumentDraft? = nil
    var isPreviewMode: Bool = false
    var isSaving: Bool = false
    var lastSaved: Date? = nil
    var errorMessage: String? = nil
    var isLoading: Bool = false

    private var autoSaveTask: Task<Void, Never>? = nil

    var isDirty: Bool { draft?.isDirty ?? false }

    func load(documentID: String, services: any AppServices) async {
        isLoading = true
        errorMessage = nil
        do {
            let doc = try await services.getDocumentDetailUseCase.execute(id: documentID)
            draft = DocumentDraft(from: doc)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func save() async {
        guard var d = draft, d.isDirty else { return }
        isSaving = true
        // MVP: save mutates the draft only (no persistence layer yet)
        try? await Task.sleep(for: .milliseconds(400))
        d.lastAutoSaved = Date()
        draft = d
        lastSaved = Date()
        isSaving = false
    }

    func scheduleAutoSave() {
        autoSaveTask?.cancel()
        autoSaveTask = Task {
            try? await Task.sleep(for: .seconds(30))
            await save()
        }
    }

    func cancelAutoSave() {
        autoSaveTask?.cancel()
    }
}
