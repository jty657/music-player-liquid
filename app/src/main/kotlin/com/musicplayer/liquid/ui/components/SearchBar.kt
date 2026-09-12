package com.musicplayer.liquid.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * 搜索栏组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索歌曲、艺术家..."
) {
    var isExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(tween(150)) + expandHorizontally(tween(200, easing = LinearOutSlowInEasing)))
                        .togetherWith(fadeOut(tween(100)) + shrinkHorizontally(tween(150)))
                } else {
                    (fadeIn(tween(150)) + expandHorizontally(tween(200)))
                        .togetherWith(fadeOut(tween(100)) + shrinkHorizontally(tween(150)))
                }
            },
            label = "search_bar_animation"
        ) { expanded ->
            if (expanded) {
                // 展开状态：完整搜索框
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清除",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            IconButton(onClick = { isExpanded = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            } else {
                // 收起状态：搜索图标按钮（带按压反馈）
                val searchInteraction = remember { MutableInteractionSource() }
                val isSearchPressed by searchInteraction.collectIsPressedAsState()
                
                val searchScale by animateFloatAsState(
                    targetValue = if (isSearchPressed) 0.97f else 1f,
                    animationSpec = tween(100, easing = LinearOutSlowInEasing),
                    label = "search_icon_scale"
                )
                
                IconButton(
                    onClick = { isExpanded = true },
                    interactionSource = searchInteraction,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .graphicsLayer { scaleX = searchScale; scaleY = searchScale }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
