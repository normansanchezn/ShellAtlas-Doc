import Testing
import Foundation
@testable import ShellDoc

@Suite("DashboardViewModel")
struct DashboardViewModelTests {

    @Test("Dashboard loads correct total document count")
    func loadsCorrectTotalCount() async throws {
        let container = AppContainer()
        let vm = DashboardViewModel()
        await vm.load(container: container)
        #expect(vm.totalDocuments == 6)
    }

    @Test("Dashboard loads active documents")
    func loadsActiveDocuments() async throws {
        let container = AppContainer()
        let vm = DashboardViewModel()
        await vm.load(container: container)
        #expect(vm.activeDocuments > 0)
        #expect(vm.activeDocuments <= vm.totalDocuments)
    }

    @Test("Dashboard detects possibly outdated documents")
    func detectsPossiblyOutdated() async throws {
        let container = AppContainer()
        let vm = DashboardViewModel()
        await vm.load(container: container)
        #expect(vm.possiblyOutdated > 0)
    }

    @Test("Dashboard loads recent signals")
    func loadsRecentSignals() async throws {
        let container = AppContainer()
        let vm = DashboardViewModel()
        await vm.load(container: container)
        #expect(!vm.recentSignals.isEmpty)
    }

    @Test("Dashboard loads platform breakdown")
    func loadsPlatformBreakdown() async throws {
        let container = AppContainer()
        let vm = DashboardViewModel()
        await vm.load(container: container)
        #expect(!vm.documentsByPlatform.isEmpty)
    }

    @Test("Dashboard is not loading after load completes")
    func isNotLoadingAfterLoad() async throws {
        let container = AppContainer()
        let vm = DashboardViewModel()
        await vm.load(container: container)
        #expect(!vm.isLoading)
        #expect(vm.errorMessage == nil)
    }
}
