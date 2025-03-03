package com.aireply.presentation.screens.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aireply.R
import com.aireply.ui.theme.AIReplyKotlinTheme

@Composable
fun ShimmerEffect() {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        repeat(11) {
            ArticleCardShimmerEffect(
                modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 5.dp)
            )
        }
    }
}

fun Modifier.shimmerEffect() = composed {
    val transition = rememberInfiniteTransition(label = "")
    val alpha = transition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000), repeatMode = RepeatMode.Reverse
        ), label = ""
    ).value
    background(color = colorResource(id = R.color.shimmer).copy(alpha = alpha), shape = RoundedCornerShape(12.dp))
}

@Composable
fun ArticleCardShimmerEffect(
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .height(74.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.large)
                    .padding(5.dp)
                    .shimmerEffect(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleCardShimmerEffectPreview() {
    AIReplyKotlinTheme {
        ArticleCardShimmerEffect()
    }
}