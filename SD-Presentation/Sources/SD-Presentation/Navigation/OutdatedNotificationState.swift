//
//  OutdatedNotificationState.swift
//  DS-Core
//
//  Created by Norman Sánchez on 05/06/26.
//

import Foundation
import SD_Domain
import DS_Core
import SwiftUI  

@MainActor
@Observable
public final class OutdatedNotificationState {
    var isExpanded: Bool = true
    var outdatedCount: Int = 0
    var isVisible: Bool = false

    func load(services: any AppServices) async throws {
        do {
            let docs = try await services.getDocumentsUseCase.execute()
            var count = 0
            for doc in docs {
                if let result = try? await services.evaluateDocumentHealthUseCase.execute(document: doc),
                   result.healthScore < 80 {
                    count += 1
                }
            }
            outdatedCount = count
            isVisible = count > 0
        } catch {
            isVisible = false
        }
    }

    func collapse() {
        withAnimation(.spring(duration: 0.35)) { isExpanded = false }
    }

    func expand() {
        withAnimation(.spring(duration: 0.35)) { isExpanded = true }
    }
}
