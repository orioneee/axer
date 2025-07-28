package io.github.orioneee.presentation.screens.exceptions

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.WebAssetOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.exceptions
import io.github.orioneee.axer.generated.resources.no_exceptions_desc
import io.github.orioneee.domain.exceptions.AxerException
import io.github.orioneee.domain.other.DataState
import io.github.orioneee.logger.formateAsTime
import io.github.orioneee.extentions.clickableWithoutRipple
import io.github.orioneee.presentation.LocalAxerDataProvider
import io.github.orioneee.presentation.components.AxerLogoDialog
import io.github.orioneee.presentation.components.LoadingDialog
import io.github.orioneee.presentation.components.ScreenLayout
import io.github.orioneee.presentation.screens.requests.EmptyScreen
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
        val animatedContainerColor by animateColorAsState(
            targetValue = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            label = "RequestCardColorAnimation"
        )
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = animatedContainerColor,
                headlineColor = if (exception.isFatal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickableWithoutRipple {
                    onClick()
                },
            headlineContent = {
                Text(
                    exception.error.name,
                    color = if (exception.isFatal) MaterialTheme.colorScheme.error else Color.Unspecified,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    exception.time.formateAsTime() + " ${exception.error.message}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
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
                AxerLogoDialog()
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