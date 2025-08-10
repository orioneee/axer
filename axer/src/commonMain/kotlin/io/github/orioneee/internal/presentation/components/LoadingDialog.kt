package io.github.orioneee.internal.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.cancel
import io.github.orioneee.axer.generated.resources.processing_your_request
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoadingDialog(
    isShow: Boolean,
    onCancel: () -> Unit,
) {
    MultiplatformAlertDialog(
        canDismissByClickOutside = false,
        isShowDialog = isShow,
        onDismiss = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.processing_your_request),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

            }
        },
        confirmButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}