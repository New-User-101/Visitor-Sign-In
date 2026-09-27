package stu.gpt.signing.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import stu.gpt.signing.data.Repository
import kotlin.random.Random

@Composable
fun ScreenSaver(onDismiss: () -> Unit) {
    val settings = Repository.settings
    val title = settings.screenSaverHeader.ifBlank { 
        if (Repository.stageMembers.isNotEmpty()) Repository.playTitle else settings.headerText 
    }
    val message = settings.screenSaverMessage
    
    // NICE DEEP BLUEY PURPLE (Static for CPU efficiency)
    val backgroundColor = Color(0xFF2A1B60)

    // State for the box position
    var posX by remember { mutableFloatStateOf(0f) }
    var posY by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            }
    ) {
        val density = LocalDensity.current
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        
        val boxWidth = with(density) { 450.dp.toPx() }
        val boxHeight = with(density) { 250.dp.toPx() }
        
        val maxX = (screenWidth - boxWidth).coerceAtLeast(0f)
        val maxY = (screenHeight - boxHeight).coerceAtLeast(0f)

        // EFFICIENCY: "Teleporting" behavior instead of continuous animation.
        // We update the position only once every 15 seconds.
        // Between jumps, the CPU usage drops to 0%.
        LaunchedEffect(maxX, maxY) {
            while (true) {
                posX = if (maxX > 0) Random.nextFloat() * maxX else 0f
                posY = if (maxY > 0) Random.nextFloat() * maxY else 0f
                delay(15000)
            }
        }

        // The Bouncing Box (Optimized to a Static Jump)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = posX
                    translationY = posY
                }
                .width(450.dp)
                .heightIn(min = 200.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 40.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
