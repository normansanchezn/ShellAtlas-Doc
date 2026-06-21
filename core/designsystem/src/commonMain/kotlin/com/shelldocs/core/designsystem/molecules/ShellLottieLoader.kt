package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import shelldocs.core.designsystem.generated.resources.Res

/**
 * App-wide Lottie loading indicator (`shell_atlas_loading_lottie.json`),
 * used both as the cold-launch splash and inside [ShellLoadingOverlay].
 */
@Composable
fun ShellLottieLoader(
    modifier: Modifier = Modifier.size(120.dp),
) {
    val jsonString by produceState<String?>(initialValue = null) {
        value = Res.readBytes("files/shell_atlas_loading_lottie.json").decodeToString()
    }
    val composition by rememberLottieComposition(jsonString) {
        LottieCompositionSpec.JsonString(jsonString ?: "")
    }
    if (jsonString != null) {
        val painter = rememberLottiePainter(
            composition = composition,
            isPlaying = true,
            iterations = Compottie.IterateForever,
        )
        Image(painter = painter, contentDescription = null, modifier = modifier)
    }
}
