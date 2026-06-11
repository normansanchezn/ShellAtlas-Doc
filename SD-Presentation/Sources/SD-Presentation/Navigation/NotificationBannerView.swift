//
//  NotificationBannerView.swift
//
//  Created by Norman Sánchez on 05/06/26.
//

import SwiftUI
import SD_Domain
import DS_Core
import SD_DesignSystem

// MARK: - Expanded Banner

struct NotificationBannerView: View {
    @Bindable var state: OutdatedNotificationState
    var onNavigate: () -> Void = {}

    var body: some View {
        if state.isVisible {
            bannerContent
                .transition(.move(edge: .top).combined(with: .opacity))
        }
    }

    private var bannerContent: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(SDColors.shellOrange.opacity(0.2))
                    .frame(width: 36, height: 36)
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundStyle(SDColors.shellOrange)
                    .font(.system(size: 16, weight: .semibold))
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("\(state.outdatedCount) documents need attention")
                    .font(.callout.weight(.semibold))
                Text("Tap to review and update")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Button(action: onNavigate) {
                Text("Review")
                    .font(.caption.weight(.semibold))
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(SDColors.shellOrange, in: .capsule)
                    .foregroundStyle(.white)
            }

            Button(action: state.collapse) {
                Image(systemName: "xmark")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(.background.secondary, in: .rect(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .strokeBorder(SDColors.shellOrange.opacity(0.3), lineWidth: 1)
        )
        .padding(.horizontal, 16)
        .padding(.top, 8)
    }
}

// MARK: - FAB (Floating Action Button)

struct NotificationFAB: View {
    @Bindable var state: OutdatedNotificationState
    var onNavigate: () -> Void = {}

    var body: some View {
        if state.isVisible && !state.isExpanded {
            Button(action: {
                onNavigate()
            }) {
                ZStack(alignment: .topTrailing) {
                    Circle()
                        .fill(SDColors.shellOrange)
                        .frame(width: 52, height: 52)
                        .shadow(color: SDColors.shellOrange.opacity(0.4), radius: 8, x: 0, y: 4)

                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundStyle(.white)
                        .font(.system(size: 20))
                        .frame(width: 52, height: 52)

                    Text("\(state.outdatedCount)")
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(.white)
                        .frame(width: 18, height: 18)
                        .background(SDColors.shellRed, in: .circle)
                        .offset(x: 4, y: -4)
                }
            }
            .padding(20)
            .transition(.scale.combined(with: .opacity))
        }
    }
}
