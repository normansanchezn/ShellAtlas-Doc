import Foundation
import SD_Domain
import DS_Core

@MainActor
@Observable
final class MockSourcesViewModel {
    var documents: [KnowledgeDocument] = []
    var tickets: [KnowledgeTicket] = []
    var commits: [RepositoryCommit] = []
    var workflows: [WorkflowChange] = []
    var releases: [ReleaseNote] = []
    var owners: [DocumentOwner] = []
    var isLoading = false
    var errorMessage: String?

    func load(services: any AppServices) async throws {
        isLoading = true
        do {
            let docs = try await services.documentRepository.getDocuments()
            let t = try await services.ticketRepository.getTickets()
            let c = try await services.signalRepository.getCommits()
            let w = try await services.signalRepository.getWorkflowChanges()
            let r = try await services.releaseRepository.getReleases()
            let o = try await services.ownerRepository.getOwners()
            let (d, tRes, cRes, wRes, rRes, oRes) = try await (docs, t, c, w, r, o)
            documents = d
            tickets = tRes
            commits = cRes
            workflows = wRes
            releases = rRes
            owners = oRes
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}
