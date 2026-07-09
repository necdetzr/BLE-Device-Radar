package com.necdetzr.radar

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
internal fun RadarScreen(
    modifier:Modifier = Modifier
){

}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun RadarScreen(

){


}

@Composable
private fun RadarAnimation(){
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500,easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .alpha(alpha)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}
