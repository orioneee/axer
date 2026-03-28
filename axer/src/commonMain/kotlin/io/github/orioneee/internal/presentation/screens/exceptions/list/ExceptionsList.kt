package io.github.orioneee.internal.presentation.screens.exceptions.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.WebAssetOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.orioneee.LocalAxerDataProvider
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.exceptions
import io.github.orioneee.axer.generated.resources.no_exceptions_desc
import io.github.orioneee.internal.domain.exceptions.AxerException
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.logger.formateAsDate
import io.github.orioneee.internal.presentation.components.AxerLogoDialog
import io.github.orioneee.internal.presentation.components.LocalAxerColors
import io.github.orioneee.internal.presentation.components.LoadingDialog
import io.github.orioneee.internal.presentation.components.ScreenLayout
import io.github.orioneee.internal.presentation.components.ServerRunStatus
import io.github.orioneee.internal.presentation.screens.requests.EmptyScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal class ExceptionsList {
    @Composable
    fun ExceptionItem(
        isSelected: Boolean,
        exception: AxerException,
        onClick: () -> Unit,
    ) {
        val axerColors = LocalAxerColors.current
        val accentColor = if (exception.isFatal) axerColors.logError else axerColors.statusPending

        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                1.dp,
                if (isSelected) axerColors.cardBorderSelected else axerColors.cardBorder
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor.copy(alpha = 0.7f))
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exception.error.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (exception.isFatal) axerColors.logError else MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${exception.time.formateAsDate()} | ${exception.error.message}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        selectedExceptionID: Long? = null,
        onClickToException: (AxerException) -> Unit,
        onClear: () -> Unit,
    ) {
        val provider = LocalAxerDataProvider.current
        val viewModel: ExceptionListViewModel = koinViewModel {
            parametersOf(provider, null)
        }

        val exceptions by viewModel.exceptionsState.collectAsState(DataState.Loading())
        val isShowLoadingDialog by viewModel.isShowLoadingDialog.collectAsState(false)

        LoadingDialog(
            isShow = isShowLoadingDialog,
            onCancel = viewModel::cancelCurrentJob
        )

        ScreenLayout(
            state = exceptions,
            isEmpty = {
                it.isEmpty()
            },
            topAppBarTitle = stringResource(Res.string.exceptions),
            navigationIcon = {
                Row {
                    AxerLogoDialog()
                    ServerRunStatus(provider)
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        onClear()
                        viewModel.deleteAll()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear Requests"
                    )
                }
            },
            emptyContent = {
                EmptyScreen().Screen(
                    image = rememberVectorPainter(Icons.Outlined.WebAssetOff),
                    description = stringResource(Res.string.no_exceptions_desc)
                )
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(it) { item ->
                    ExceptionItem(
                        isSelected = item.id == selectedExceptionID,
                        exception = item,
                        onClick = {
                            onClickToException(item)
                        }
                    )
                }
            }
        }
    }
}