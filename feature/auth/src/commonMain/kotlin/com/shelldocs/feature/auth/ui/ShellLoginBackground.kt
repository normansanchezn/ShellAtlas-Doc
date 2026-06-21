package com.shelldocs.feature.auth.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val TWO_PI = (PI * 2).toFloat()

private data class Particle(
    val x: Float,
    val y: Float,
    val speedX: Float,
    val speedY: Float,
    val phaseX: Float,
    val phaseY: Float,
    val radius: Float,
    val baseAlpha: Float,
    val pulseSpeed: Float,
    val pulsePha: Float,
    val isAccent: Boolean,
)

private data class GlowOrb(
    val x: Float,
    val y: Float,
    val radius: Float,
    val driftX: Float,
    val driftY: Float,
    val phase: Float,
    val color: Color,
)

private fun buildParticles(count: Int): List<Particle> {
    val rng = Random(seed = 0x5E11)
    return List(count) { i ->
        Particle(
            x = rng.nextFloat(),
            y = rng.nextFloat(),
            speedX = rng.nextFloat() * 0.25f + 0.08f,
            speedY = rng.nextFloat() * 0.18f + 0.05f,
            phaseX = rng.nextFloat() * TWO_PI,
            phaseY = rng.nextFloat() * TWO_PI,
            radius = rng.nextFloat() * 2.4f + 0.6f,
            baseAlpha = rng.nextFloat() * 0.35f + 0.1f,
            pulseSpeed = rng.nextFloat() * 0.4f + 0.15f,
            pulsePha = rng.nextFloat() * TWO_PI,
            isAccent = i % 11 == 0,
        )
    }
}

private fun buildGlowOrbs(): List<GlowOrb> =
    listOf(
        GlowOrb(
            x = 0.18f,
            y = 0.16f,
            radius = 0.28f,
            driftX = 0.024f,
            driftY = 0.018f,
            phase = 0.2f,
            color = Color(0x40FFD100),
        ),
        GlowOrb(
            x = 0.84f,
            y = 0.24f,
            radius = 0.22f,
            driftX = 0.018f,
            driftY = 0.024f,
            phase = 1.7f,
            color = Color(0x24FF8C6A),
        ),
        GlowOrb(
            x = 0.72f,
            y = 0.82f,
            radius = 0.24f,
            driftX = 0.02f,
            driftY = 0.014f,
            phase = 3.1f,
            color = Color(0x2214B8A6),
        ),
    )

/**
 * Full-bleed animated background for the sign-in screen.
 *
 * 68 particles orbit their seed positions with independent sine/cosine cycles.
 * Nearby pairs are joined by faint lines (network-graph aesthetic).  Every 11th
 * node is tinted Shell yellow.  Full cycle = 22 s.
 *
 * On desktop and web, moving the mouse cursor near particles repels them.
 * The repulsion is filtered to [PointerType.Mouse] so touch devices never see
 * the effect.  When the cursor leaves, an exponential spring (critically
 * damped, low stiffness) eases particles back to their orbit — giving a
 * springy "memory of position" feel without individual particle physics.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShellLoginBackground(
    isDarkTheme: Boolean,
    animate: Boolean = true,
    modifier: Modifier = Modifier,
) {
    // Monotonically increasing clock — never resets, so sin/cos never jump at cycle boundaries.
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animate) {
        if (animate) {
            var lastMs = 0L
            while (true) {
                withFrameMillis { ms ->
                    if (lastMs != 0L) time += (ms - lastMs) / 22_000f * TWO_PI
                    lastMs = ms
                }
            }
        } else {
            time = 0f
        }
    }

    val particles = remember { buildParticles(count = 68) }
    val glowOrbs = remember { buildGlowOrbs() }

    // Mouse-repulsion state — only ever non-null when a real mouse pointer moves.
    var mouseActive by remember { mutableStateOf(false) }
    var lastPointerPos by remember { mutableStateOf<Offset?>(null) }

    // Critically-damped spring: instant lock-in when mouse enters, smooth
    // exponential decay back to 0 when mouse exits (~0.8 s to settle).
    val influence by animateFloatAsState(
        targetValue = if (mouseActive) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "mouse-influence",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(animate) {
                if (!animate) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull()
                        when (event.type) {
                            PointerEventType.Move -> {
                                if (change?.type == PointerType.Mouse) {
                                    lastPointerPos = change.position
                                    mouseActive = true
                                }
                            }
                            PointerEventType.Exit -> mouseActive = false
                            else -> {}
                        }
                    }
                }
            },
    ) {
        if (isDarkTheme) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B0D10), Color(0xFF131720)),
                ),
            )

            val w = size.width
            val h = size.height
            val connectionThreshold = w * 0.14f
            val repelRadius = w * 0.13f
            val repelStrength = w * 0.05f
            val accentColor = Color(0xFFFFD100)
            val nodeColor = Color.White

            val orbits = particles.map { p ->
                Offset(
                    x = (p.x + sin(time * p.speedX + p.phaseX) * 0.06f) * w,
                    y = (p.y + cos(time * p.speedY + p.phaseY) * 0.05f) * h,
                )
            }

            val ptr = lastPointerPos
            val positions = if (ptr != null && influence > 0.001f) {
                orbits.map { orbit ->
                    val dx = orbit.x - ptr.x
                    val dy = orbit.y - ptr.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < repelRadius && dist > 0.5f) {
                        val strength = influence * repelStrength * (1f - dist / repelRadius)
                        Offset(orbit.x + dx / dist * strength, orbit.y + dy / dist * strength)
                    } else {
                        orbit
                    }
                }
            } else {
                orbits
            }

            for (i in particles.indices) {
                for (j in i + 1 until particles.size) {
                    val dx = positions[i].x - positions[j].x
                    val dy = positions[i].y - positions[j].y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < connectionThreshold) {
                        val alpha = (1f - dist / connectionThreshold) * 0.12f
                        drawLine(
                            color = nodeColor.copy(alpha = alpha),
                            start = positions[i],
                            end = positions[j],
                            strokeWidth = 0.6f,
                        )
                    }
                }
            }

            particles.forEachIndexed { i, p ->
                val pulse = sin(time * p.pulseSpeed + p.pulsePha) * 0.12f
                val alpha = (p.baseAlpha + pulse).coerceIn(0.05f, 0.80f)
                drawCircle(
                    color = if (p.isAccent) accentColor else nodeColor,
                    radius = p.radius * density,
                    center = positions[i],
                    alpha = alpha,
                )
            }
        } else {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F1E8),
                        Color(0xFFF9F8F4),
                        Color(0xFFEEF3F4),
                    ),
                ),
            )

            glowOrbs.forEach { orb ->
                val center = Offset(
                    x = (orb.x + sin(time * 0.12f + orb.phase) * orb.driftX) * w,
                    y = (orb.y + cos(time * 0.1f + orb.phase) * orb.driftY) * h,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orb.color, orb.color.copy(alpha = 0f)),
                        center = center,
                        radius = orb.radius * w,
                    ),
                    radius = orb.radius * w,
                    center = center,
                )
            }

            val contourColor = Color(0x1E7F8A93)
            repeat(5) { index ->
                val inset = index * (w * 0.072f)
                val top = -h * (0.2f - index * 0.028f) + sin(time * 0.09f + index) * 10f
                drawRoundRect(
                    color = contourColor.copy(alpha = 0.10f - index * 0.013f),
                    topLeft = Offset(-w * 0.04f + inset, top),
                    size = androidx.compose.ui.geometry.Size(
                        width = w * 0.84f,
                        height = h * 0.56f,
                    ),
                    cornerRadius = CornerRadius(w * 0.2f, w * 0.2f),
                    style = Stroke(width = 0.8f, cap = StrokeCap.Round),
                )
                drawRoundRect(
                    color = contourColor.copy(alpha = 0.07f - index * 0.01f),
                    topLeft = Offset(w * 0.56f - inset * 0.46f, h * 0.5f + index * 10f),
                    size = androidx.compose.ui.geometry.Size(
                        width = w * 0.48f,
                        height = h * 0.26f,
                    ),
                    cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
                    style = Stroke(width = 0.7f, cap = StrokeCap.Round),
                )
            }

            val bandColor = Color(0x14888F99)
            repeat(3) { index ->
                val y = h * (0.24f + index * 0.2f) + sin(time * 0.06f + index) * 5f
                drawLine(
                    color = bandColor.copy(alpha = 0.13f - index * 0.018f),
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = w, y = y + 10f),
                    strokeWidth = 0.7f,
                )
            }

            repeat(10) { index ->
                val column = index % 5
                val row = index / 5
                val baseX = w * (0.16f + column * 0.17f)
                val baseY = h * (0.3f + row * 0.28f)
                val drift = sin(time * 0.12f + index) * 6f
                drawCircle(
                    color = Color(0x16838B95),
                    radius = 1.6f,
                    center = Offset(baseX + drift, baseY + drift * 0.24f),
                )
            }

            repeat(3) { index ->
                val x = w * (0.16f + index * 0.27f)
                val y = h * (0.16f + index * 0.22f)
                drawRoundRect(
                    color = Color(0x12FFFFFF),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(
                        width = w * 0.14f,
                        height = h * 0.06f,
                    ),
                    cornerRadius = CornerRadius(24f, 24f),
                )
            }
        }
    }
}
