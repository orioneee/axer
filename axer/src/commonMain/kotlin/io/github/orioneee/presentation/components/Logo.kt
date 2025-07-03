package io.github.orioneee.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.orioneee.axer.generated.resources.Res
import io.github.orioneee.axer.generated.resources.ic_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun AxerLogo(){
    Icon(
        painter = painterResource(Res.drawable.ic_logo),
        contentDescription = "Logo",
        tint = Color.Unspecified,
        modifier = Modifier.padding(12.dp)
    )
}