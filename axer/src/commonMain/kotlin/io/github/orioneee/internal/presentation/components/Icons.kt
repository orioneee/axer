package io.github.orioneee.internal.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

private var _ic_dialog_theme: ImageVector? = null

val Icons.Outlined.DarkLightTheme: ImageVector
    get() {
        if (_ic_dialog_theme != null) {
            return _ic_dialog_theme!!
        }
        _ic_dialog_theme = materialIcon(name = "Ic_dialog_theme") {
            materialPath {
                moveTo(7.5F, 2.0F)
                curveTo(5.71F, 3.15F, 4.5F, 5.18F, 4.5F, 7.5F)
                curveTo(4.5F, 9.82F, 5.71F, 11.85F, 7.53F, 13.0F)
                curveTo(4.46F, 13.0F, 2.0F, 10.54F, 2.0F, 7.5F)
                arcTo(5.5F, 5.5F, 0.0F, false, true, 7.5F, 2.0F)
                moveTo(19.07F, 3.5F)
                lineTo(20.5F, 4.93F)
                lineTo(4.93F, 20.5F)
                lineTo(3.5F, 19.07F)
                lineTo(19.07F, 3.5F)
                moveTo(12.89F, 5.93F)
                lineTo(11.41F, 5.0F)
                lineTo(9.97F, 6.0F)
                lineTo(10.39F, 4.3F)
                lineTo(9.0F, 3.24F)
                lineTo(10.75F, 3.12F)
                lineTo(11.33F, 1.47F)
                lineTo(12.0F, 3.1F)
                lineTo(13.73F, 3.13F)
                lineTo(12.38F, 4.26F)
                lineTo(12.89F, 5.93F)
                moveTo(9.59F, 9.54F)
                lineTo(8.43F, 8.81F)
                lineTo(7.31F, 9.59F)
                lineTo(7.65F, 8.27F)
                lineTo(6.56F, 7.44F)
                lineTo(7.92F, 7.35F)
                lineTo(8.37F, 6.06F)
                lineTo(8.88F, 7.33F)
                lineTo(10.24F, 7.36F)
                lineTo(9.19F, 8.23F)
                lineTo(9.59F, 9.54F)
                moveTo(19.0F, 13.5F)
                arcTo(5.5F, 5.5F, 0.0F, false, true, 13.5F, 19.0F)
                curveTo(12.28F, 19.0F, 11.15F, 18.6F, 10.24F, 17.93F)
                lineTo(17.93F, 10.24F)
                curveTo(18.6F, 11.15F, 19.0F, 12.28F, 19.0F, 13.5F)
                moveTo(14.6F, 20.08F)
                lineTo(17.37F, 18.93F)
                lineTo(17.13F, 22.28F)
                lineTo(14.6F, 20.08F)
                moveTo(18.93F, 17.38F)
                lineTo(20.08F, 14.61F)
                lineTo(22.28F, 17.15F)
                lineTo(18.93F, 17.38F)
                moveTo(20.08F, 12.42F)
                lineTo(18.94F, 9.64F)
                lineTo(22.28F, 9.88F)
                lineTo(20.08F, 12.42F)
                moveTo(9.63F, 18.93F)
                lineTo(12.4F, 20.08F)
                lineTo(9.87F, 22.27F)
                lineTo(9.63F, 18.93F)
                close()
            }
        }
        return _ic_dialog_theme!!
    }