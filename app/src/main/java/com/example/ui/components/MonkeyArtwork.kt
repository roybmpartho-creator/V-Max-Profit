package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun MonkeyArtwork(
    level: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val config = getMonkeyConfig(level)
    val imageRes = when (level) {
        1 -> R.drawable.img_ape_sentinel
        2 -> R.drawable.img_ape_vanguard
        3 -> R.drawable.img_ape_sovereign
        4 -> R.drawable.img_ape_prime
        5 -> R.drawable.img_ape_sovereign
        6 -> R.drawable.img_ape_vanguard
        7 -> R.drawable.img_ape_sentinel
        8 -> R.drawable.img_ape_prime
        9 -> R.drawable.img_ape_sovereign
        else -> R.drawable.img_ape_sentinel
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(config.accentColor, config.accentColor.copy(alpha = 0.3f), Color.Transparent)
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Cyber Ape Level $level",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

private fun DrawScope.drawTechRing(w: Float, h: Float, color: Color) {
    drawCircle(
        color = color.copy(alpha = 0.2f),
        radius = w * 0.44f,
        center = Offset(w * 0.5f, h * 0.48f),
        style = Stroke(width = 1.5.dp.toPx())
    )
    // Circuit nodes
    val cx = w * 0.5f
    val cy = h * 0.48f
    val r = w * 0.44f
    for (i in 0..5) {
        val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
        val nx = cx + r * kotlin.math.cos(angle)
        val ny = cy + r * kotlin.math.sin(angle)
        drawCircle(color, radius = 2.5.dp.toPx(), center = Offset(nx, ny))
    }
}

private fun DrawScope.drawMouthExpression(w: Float, h: Float, config: MonkeyVisualConfig) {
    when (config.mouthStyle) {
        MouthStyle.GOLD_TOOTH_SMILE -> {
            val mouthPath = Path().apply {
                moveTo(w * 0.40f, h * 0.72f)
                quadraticBezierTo(w * 0.5f, h * 0.79f, w * 0.60f, h * 0.72f)
            }
            drawPath(mouthPath, Color(0xFF1E1714), style = Stroke(width = 3.dp.toPx()))
            // Gold tooth
            drawRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(w * 0.47f, h * 0.73f),
                size = Size(w * 0.045f, h * 0.035f)
            )
        }
        MouthStyle.CYBER_RESPIRATOR -> {
            val maskPath = Path().apply {
                moveTo(w * 0.38f, h * 0.68f)
                lineTo(w * 0.62f, h * 0.68f)
                lineTo(w * 0.58f, h * 0.83f)
                lineTo(w * 0.42f, h * 0.83f)
                close()
            }
            drawPath(maskPath, Color(0xFF1E293B))
            drawPath(maskPath, config.accentColor, style = Stroke(width = 2.dp.toPx()))
            // LED vents
            drawLine(
                color = config.accentColor,
                start = Offset(w * 0.44f, h * 0.75f),
                end = Offset(w * 0.56f, h * 0.75f),
                strokeWidth = 2.dp.toPx()
            )
        }
        MouthStyle.CONFIDENT_SMIRK -> {
            val mouthPath = Path().apply {
                moveTo(w * 0.40f, h * 0.73f)
                quadraticBezierTo(w * 0.50f, h * 0.77f, w * 0.62f, h * 0.71f)
            }
            drawPath(mouthPath, Color(0xFF1E1714), style = Stroke(width = 3.dp.toPx()))
        }
    }
}

private fun DrawScope.drawEyesOrVisor(w: Float, h: Float, config: MonkeyVisualConfig) {
    when (config.eyeStyle) {
        EyeStyle.CYBER_VISOR -> {
            // Neon visor across eyes
            val visorPath = Path().apply {
                moveTo(w * 0.28f, h * 0.38f)
                lineTo(w * 0.72f, h * 0.38f)
                lineTo(w * 0.68f, h * 0.48f)
                lineTo(w * 0.32f, h * 0.48f)
                close()
            }
            drawPath(visorPath, Color(0xFF0F172A))
            drawPath(visorPath, config.accentColor, style = Stroke(width = 2.5.dp.toPx()))
            // Visor scanner glow line
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, config.accentColor, Color.White, config.accentColor, Color.Transparent)
                ),
                start = Offset(w * 0.30f, h * 0.43f),
                end = Offset(w * 0.70f, h * 0.43f),
                strokeWidth = 3.dp.toPx()
            )
        }
        EyeStyle.GLOWING_OPTICS -> {
            // Left eye
            drawCircle(Color(0xFF0B132B), radius = w * 0.08f, center = Offset(w * 0.38f, h * 0.42f))
            drawCircle(config.accentColor, radius = w * 0.05f, center = Offset(w * 0.38f, h * 0.42f))
            drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.36f, h * 0.40f))
            // Right eye
            drawCircle(Color(0xFF0B132B), radius = w * 0.08f, center = Offset(w * 0.62f, h * 0.42f))
            drawCircle(config.accentColor, radius = w * 0.05f, center = Offset(w * 0.62f, h * 0.42f))
            drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.60f, h * 0.40f))
        }
        EyeStyle.LASER_SIGHT -> {
            // Left eye normal
            drawCircle(Color(0xFF0B132B), radius = w * 0.075f, center = Offset(w * 0.38f, h * 0.42f))
            drawCircle(Color(0xFFFFB800), radius = w * 0.045f, center = Offset(w * 0.38f, h * 0.42f))
            drawCircle(Color.White, radius = w * 0.018f, center = Offset(w * 0.37f, h * 0.41f))
            // Right eye cyber-monocle / laser
            drawCircle(Color(0xFF1E293B), radius = w * 0.09f, center = Offset(w * 0.62f, h * 0.42f))
            drawCircle(Color(0xFFFF1744), radius = w * 0.055f, center = Offset(w * 0.62f, h * 0.42f))
            drawCircle(Color.White, radius = w * 0.02f, center = Offset(w * 0.62f, h * 0.42f))
            drawCircle(Color(0xFFFF1744), radius = w * 0.09f, center = Offset(w * 0.62f, h * 0.42f), style = Stroke(width = 2.dp.toPx()))
        }
    }
}

private fun DrawScope.drawAccessory(w: Float, h: Float, config: MonkeyVisualConfig) {
    when (config.accessory) {
        AccessoryType.IMPERIAL_CROWN -> {
            val crownPath = Path().apply {
                moveTo(w * 0.30f, h * 0.22f)
                lineTo(w * 0.34f, h * 0.09f)
                lineTo(w * 0.42f, h * 0.16f)
                lineTo(w * 0.50f, h * 0.06f)
                lineTo(w * 0.58f, h * 0.16f)
                lineTo(w * 0.66f, h * 0.09f)
                lineTo(w * 0.70f, h * 0.22f)
                close()
            }
            drawPath(crownPath, Color(0xFFFFD700))
            drawPath(crownPath, Color(0xFFB8860B), style = Stroke(width = 2.dp.toPx()))
            // Crown jewels
            drawCircle(Color(0xFFFF1744), radius = w * 0.022f, center = Offset(w * 0.50f, h * 0.14f))
            drawCircle(Color(0xFF00E5FF), radius = w * 0.016f, center = Offset(w * 0.38f, h * 0.18f))
            drawCircle(Color(0xFF00E5FF), radius = w * 0.016f, center = Offset(w * 0.62f, h * 0.18f))
        }
        AccessoryType.CYBER_ANTENNAE -> {
            drawLine(
                color = config.accentColor,
                start = Offset(w * 0.42f, h * 0.19f),
                end = Offset(w * 0.35f, h * 0.08f),
                strokeWidth = 3.dp.toPx()
            )
            drawCircle(config.accentColor, radius = w * 0.035f, center = Offset(w * 0.35f, h * 0.08f))
            drawLine(
                color = config.accentColor,
                start = Offset(w * 0.58f, h * 0.19f),
                end = Offset(w * 0.65f, h * 0.08f),
                strokeWidth = 3.dp.toPx()
            )
            drawCircle(config.accentColor, radius = w * 0.035f, center = Offset(w * 0.65f, h * 0.08f))
        }
        AccessoryType.NEON_HALO -> {
            drawOval(
                brush = Brush.linearGradient(listOf(config.accentColor, Color.White, config.accentColor)),
                topLeft = Offset(w * 0.25f, h * 0.06f),
                size = Size(w * 0.50f, h * 0.10f),
                style = Stroke(width = 3.dp.toPx())
            )
        }
        AccessoryType.NONE -> {
            // Cool cyber tuft / hair spikes
            val hairPath = Path().apply {
                moveTo(w * 0.40f, h * 0.18f)
                lineTo(w * 0.45f, h * 0.10f)
                lineTo(w * 0.50f, h * 0.17f)
                lineTo(w * 0.55f, h * 0.08f)
                lineTo(w * 0.60f, h * 0.18f)
            }
            drawPath(hairPath, config.furColor)
        }
    }
}

private fun DrawScope.drawCollarArmor(w: Float, h: Float, config: MonkeyVisualConfig) {
    val armorPath = Path().apply {
        moveTo(w * 0.22f, h * 0.98f)
        lineTo(w * 0.32f, h * 0.85f)
        lineTo(w * 0.50f, h * 0.90f)
        lineTo(w * 0.68f, h * 0.85f)
        lineTo(w * 0.78f, h * 0.98f)
        close()
    }
    drawPath(armorPath, Color(0xFF1E293B))
    drawPath(armorPath, config.accentColor, style = Stroke(width = 2.dp.toPx()))
    // Energy core in center
    drawCircle(config.accentColor, radius = w * 0.035f, center = Offset(w * 0.5f, h * 0.92f))
}

data class MonkeyVisualConfig(
    val furColor: Color,
    val skinColor: Color,
    val accentColor: Color,
    val bgBrush: Brush,
    val eyeStyle: EyeStyle,
    val mouthStyle: MouthStyle,
    val accessory: AccessoryType
)

enum class EyeStyle {
    GLOWING_OPTICS,
    CYBER_VISOR,
    LASER_SIGHT
}

enum class MouthStyle {
    CONFIDENT_SMIRK,
    GOLD_TOOTH_SMILE,
    CYBER_RESPIRATOR
}

enum class AccessoryType {
    NONE,
    IMPERIAL_CROWN,
    CYBER_ANTENNAE,
    NEON_HALO
}

fun getMonkeyConfig(level: Int): MonkeyVisualConfig {
    return when (level) {
        1 -> MonkeyVisualConfig(
            furColor = Color(0xFF1E1B4B),
            skinColor = Color(0xFFD4A373),
            accentColor = Color(0xFFA855F7),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF2E0854), Color(0xFF130424))),
            eyeStyle = EyeStyle.CYBER_VISOR,
            mouthStyle = MouthStyle.CONFIDENT_SMIRK,
            accessory = AccessoryType.NONE
        )
        2 -> MonkeyVisualConfig(
            furColor = Color(0xFF155E75),
            skinColor = Color(0xFFE29578),
            accentColor = Color(0xFF00F0FF),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF083344), Color(0xFF041822))),
            eyeStyle = EyeStyle.CYBER_VISOR,
            mouthStyle = MouthStyle.CONFIDENT_SMIRK,
            accessory = AccessoryType.CYBER_ANTENNAE
        )
        3 -> MonkeyVisualConfig(
            furColor = Color(0xFF5B3406),
            skinColor = Color(0xFFDDA15E),
            accentColor = Color(0xFFFFD700),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF452B04), Color(0xFF1E1201))),
            eyeStyle = EyeStyle.CYBER_VISOR,
            mouthStyle = MouthStyle.GOLD_TOOTH_SMILE,
            accessory = AccessoryType.IMPERIAL_CROWN
        )
        4 -> MonkeyVisualConfig(
            furColor = Color(0xFF064E3B),
            skinColor = Color(0xFFBC6C25),
            accentColor = Color(0xFF34D399),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF022C22), Color(0xFF065F46))),
            eyeStyle = EyeStyle.CYBER_VISOR,
            mouthStyle = MouthStyle.CYBER_RESPIRATOR,
            accessory = AccessoryType.NEON_HALO
        )
        5 -> MonkeyVisualConfig(
            furColor = Color(0xFF78350F),
            skinColor = Color(0xFFE9C46A),
            accentColor = Color(0xFFFFB800),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF331800), Color(0xFF5E2D00))),
            eyeStyle = EyeStyle.LASER_SIGHT,
            mouthStyle = MouthStyle.GOLD_TOOTH_SMILE,
            accessory = AccessoryType.IMPERIAL_CROWN
        )
        6 -> MonkeyVisualConfig(
            furColor = Color(0xFF312E81),
            skinColor = Color(0xFF93C5FD),
            accentColor = Color(0xFF818CF8),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF3730A3))),
            eyeStyle = EyeStyle.CYBER_VISOR,
            mouthStyle = MouthStyle.CYBER_RESPIRATOR,
            accessory = AccessoryType.CYBER_ANTENNAE
        )
        7 -> MonkeyVisualConfig(
            furColor = Color(0xFF881337),
            skinColor = Color(0xFFFDA4AF),
            accentColor = Color(0xFFFB7185),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF4C0519), Color(0xFF831843))),
            eyeStyle = EyeStyle.LASER_SIGHT,
            mouthStyle = MouthStyle.CONFIDENT_SMIRK,
            accessory = AccessoryType.NEON_HALO
        )
        8 -> MonkeyVisualConfig(
            furColor = Color(0xFF0891B2),
            skinColor = Color(0xFFA5F3FC),
            accentColor = Color(0xFF22D3EE),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF164E63), Color(0xFF0E7490))),
            eyeStyle = EyeStyle.GLOWING_OPTICS,
            mouthStyle = MouthStyle.GOLD_TOOTH_SMILE,
            accessory = AccessoryType.IMPERIAL_CROWN
        )
        else -> MonkeyVisualConfig(
            furColor = Color(0xFF4338CA),
            skinColor = Color(0xFFFDE68A),
            accentColor = Color(0xFFFFD700),
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF180E38), Color(0xFF2D1B69))),
            eyeStyle = EyeStyle.CYBER_VISOR,
            mouthStyle = MouthStyle.GOLD_TOOTH_SMILE,
            accessory = AccessoryType.IMPERIAL_CROWN
        )
    }
}
