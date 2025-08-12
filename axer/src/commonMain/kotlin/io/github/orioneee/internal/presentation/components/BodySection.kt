package io.github.orioneee.internal.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun BodySection(
    title: String = "Body",
    titleContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    separator: String = ": ",
    defaultExpanded: Boolean = true,
    isExpandable: Boolean = true,
    onClick: (() -> Unit)? = null,
    colors: androidx.compose.material3.CardColors = CardDefaults.cardColors(),

    thickness: Dp = 8.dp,
    innerRadius: Dp = 8.dp,
    outerElevation: Dp = 2.dp,
    content: @Composable () -> Unit,
) {
    val outerRadius = innerRadius + thickness

    var isExpanded by remember { mutableStateOf(defaultExpanded) }
    val animatedRotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    val outerShape = RoundedCornerShape(outerRadius)
    val innerShape = RoundedCornerShape(innerRadius)

    Card(
        modifier = modifier,
        shape = outerShape,
        colors = colors,
        elevation = CardDefaults.cardElevation(outerElevation)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(outerRadius)
                    )
                    .clickable {
                        if (isExpandable) {
                            isExpanded = !isExpanded
                        } else {
                            onClick?.invoke()
                        }
                    }
                    .padding(horizontal = thickness, vertical = thickness / 2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (titleContent != null) {
                    titleContent()
                } else {
                    Text(
                        buildStringSection(
                            title = title,
                            content = "",
                            separator = separator
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                if (isExpandable) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        modifier = Modifier.rotate(animatedRotation),
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = thickness),
                    shape = innerShape,
                ) {
                    content()
                }
            }
        }
    }
}
