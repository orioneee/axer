package io.github.orioneee.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
internal fun BodySection(
    title: String = "Body",
    modifier: Modifier = Modifier,
    separator: String = ": ",
    isExandable: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(true) }
    val animatedRotation by animateFloatAsState(if (isExpanded) 180f else 0f)
    Card(
        modifier = modifier
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (isExandable) {
                            isExpanded = !isExpanded
                        } else {
                            onClick?.invoke()
                        }
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    buildStringSection(
                        title = title,
                        content = "",
                        separator = separator
                    )
                )
                if (isExandable) {
                    Image(
                        modifier = Modifier.rotate(animatedRotation),
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(
                visible = isExpanded
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    content()
                }
            }
        }
    }
}