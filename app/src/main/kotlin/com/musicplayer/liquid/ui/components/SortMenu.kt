package com.musicplayer.liquid.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.musicplayer.liquid.data.model.SortOption
import com.musicplayer.liquid.ui.theme.AnimationConstants

@Composable
fun SortMenu(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) AnimationConstants.PRESS_SCALE else 1f,
        animationSpec = tween(AnimationConstants.PRESS_DURATION, easing = AnimationConstants.EASE_OUT),
        label = "sort_button_scale"
    )
    
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            interactionSource = interactionSource,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "排序",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortMenuItem(
                text = "标题 (A-Z)",
                icon = Icons.Default.SortByAlpha,
                selected = currentSort == SortOption.TITLE_ASC,
                onClick = {
                    onSortSelected(SortOption.TITLE_ASC)
                    expanded = false
                }
            )
            
            SortMenuItem(
                text = "标题 (Z-A)",
                icon = Icons.Default.SortByAlpha,
                selected = currentSort == SortOption.TITLE_DESC,
                onClick = {
                    onSortSelected(SortOption.TITLE_DESC)
                    expanded = false
                }
            )
            
            Divider()
            
            SortMenuItem(
                text = "艺术家 (A-Z)",
                icon = Icons.Default.Person,
                selected = currentSort == SortOption.ARTIST_ASC,
                onClick = {
                    onSortSelected(SortOption.ARTIST_ASC)
                    expanded = false
                }
            )
            
            SortMenuItem(
                text = "艺术家 (Z-A)",
                icon = Icons.Default.Person,
                selected = currentSort == SortOption.ARTIST_DESC,
                onClick = {
                    onSortSelected(SortOption.ARTIST_DESC)
                    expanded = false
                }
            )
            
            Divider()
            
            SortMenuItem(
                text = "时长 (短→长)",
                icon = Icons.Default.AccessTime,
                selected = currentSort == SortOption.DURATION_ASC,
                onClick = {
                    onSortSelected(SortOption.DURATION_ASC)
                    expanded = false
                }
            )
            
            SortMenuItem(
                text = "时长 (长→短)",
                icon = Icons.Default.AccessTime,
                selected = currentSort == SortOption.DURATION_DESC,
                onClick = {
                    onSortSelected(SortOption.DURATION_DESC)
                    expanded = false
                }
            )
            
            Divider()
            
            SortMenuItem(
                text = "添加顺序",
                icon = Icons.Default.DateRange,
                selected = currentSort == SortOption.DATE_ADDED,
                onClick = {
                    onSortSelected(SortOption.DATE_ADDED)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SortMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        onClick = onClick
    )
}
