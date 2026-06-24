package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Brand mark from `shell-icon.svg`: replaces the old Shell Atlas wordmark/badge artwork. */
val IconShellIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "IconShellIcon",
        defaultWidth = 24.dp,
        defaultHeight = 22.21.dp,
        viewportWidth = 122.88f,
        viewportHeight = 113.76f,
    ).apply {
        addPath(
            pathData = addPathNodes(
                "M61.44,113.76c-4.39,0-6.65-2.87-6.65-2.87c-3.61-0.16-31.84-0.08-31.84-0.08l-2.75-23.2 " +
                    "c0,0-17.39-12.26-18.8-13.67C-6.95,36.09,23.22,0,61.44,0c38.22,0,68.39,36.09,60.04,73.95 " +
                    "c-1.41,1.41-18.8,13.67-18.8,13.67l-2.75,23.2c0,0-28.23-0.08-31.84,0.08C68.09,110.9,65.84,113.76,61.44,113.76 " +
                    "L61.44,113.76z",
            ),
            pathFillType = PathFillType.NonZero,
            fill = SolidColor(Color(0xFFED1C24)),
        )
        addPath(
            pathData = addPathNodes(
                "M61.48,78.49l-2.67-66.96c-3.22-0.66-11.42,0.13-15.82,3.41l12.8,63.81L38.33,17.57 " +
                    "c-4.19-0.05-12.63,7.03-13.26,9.12l25.47,54.29L21.92,31.35c-2.82,1.05-8.99,12.41-8.01,13.73l31.9,39.05L12.99,50.78 " +
                    "c0,0-4.27,6.5-1.48,17.51l18.54,13.39l2.32,18.29H55.1c0,0,3.72,3.52,6.36,3.52c2.64,0,6.36-3.52,6.36-3.52h22.73 " +
                    "l2.32-18.29l18.54-13.39c2.78-11.01-1.48-17.51-1.48-17.51L77.1,84.13L109,45.08c0.98-1.32-5.19-12.68-8.01-13.73 " +
                    "L72.37,80.98l25.47-54.29c-0.63-2.1-9.07-9.18-13.26-9.12L67.12,78.75l12.8-63.81c-4.4-3.28-12.6-4.07-15.82-3.41 " +
                    "L61.48,78.49L61.48,78.49z",
            ),
            pathFillType = PathFillType.NonZero,
            fill = SolidColor(Color(0xFFFFD500)),
        )
    }.build()
}
