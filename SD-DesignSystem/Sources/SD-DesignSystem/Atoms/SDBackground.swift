//
//  SDBackground.swift
//  SD-DesignSystem
//
//  Created by Norman Sánchez on 06/06/26.
//

import SwiftUI

public struct SDBackground: View {
    public var color: Color
    
    public init(color: Color) {
        self.color = color
    }
    
    public var body: some View {
        Rectangle()
            .fill(color)
    }
}

/// Preview
///
///
#Preview {
    SDBackground(color: .accentColor)
}
