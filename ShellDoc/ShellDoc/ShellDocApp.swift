import SwiftUI
import SD_Presentation
import SD_Domain


@main
struct ShellDocApp: App {
    @State private var container = AppContainer()

    var body: some Scene {
        WindowGroup {
            RootNavigationView()
                .environment(\.appServices, container)
        }
    }
}
