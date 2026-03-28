package io.github.orioneee.internal.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun <T> FilterRow(
    items: List<T>,
    selectedItems: List<T>,
    onItemClicked: (T) -> Unit,
    onClear: () -> Unit,
    getItemString: (T) -> String,
    withClearButton: Boolean = true,
    scrolable: Boolean = true,
) {
    val axerColors = LocalAxerColors.current

    @Composable
    fun ChipItem(item: T) {
        val isSelected = selectedItems.contains(item)
        val itemString = getItemString(item)
        InputChip(
            label = {
                Text(itemString)
            },
            selected = isSelected,
            onClick = {
                onItemClicked(item)
            },
            shape = RoundedCornerShape(10.dp),
            border = InputChipDefaults.inputChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = axerColors.cardBorder,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.dp,
            ),
        )
    }

    if (scrolable) {
        LazyRow(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (withClearButton) {
                item {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "Clear Filters"
                        )
                    }
                }
            } else {
                item {
                    Spacer(Modifier.width(8.dp))
                }
            }
            items(items) {
                ChipItem(it)
            }
        }
    } else {
        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (withClearButton) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = "Clear Filters"
                    )
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            items.forEach {
                ChipItem(it)
            }
        }
    }
}
