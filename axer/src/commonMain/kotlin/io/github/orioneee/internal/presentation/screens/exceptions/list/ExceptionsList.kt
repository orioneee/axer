package io.github.orioneee.internal.presentation.screens.exceptions.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import io.github.orioneee.internal.domain.exceptions.AxerException
import io.github.orioneee.internal.domain.other.DataState
import io.github.orioneee.internal.logger.formateAsTime
import io.github.orioneee.internal.extentions.clickableWithoutRipple
import io.github.orioneee.LocalAxerDataProvider
import io.github.orioneee.internal.logger.formateAsDate
import io.github.orioneee.internal.presentation.components.AxerLogoDialog
import io.github.orioneee.internal.presentation.components.LoadingDialog
import io.github.orioneee.internal.presentation.components.ScreenLayout
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
        val animatedContainerColor by animateColorAsState(
            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            label = "ExceptionCardColor"
        )

        val animatedElevation by animateDpAsState(
            targetValue = if (isSelected) 6.dp else 2.dp,
            label = "CardElevation"
        )

        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
            ,
            colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = exception.error.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (exception.isFatal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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