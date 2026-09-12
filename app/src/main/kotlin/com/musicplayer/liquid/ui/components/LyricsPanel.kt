package com.musicplayer.liquid.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * LRC歌词数据类
 */
data class LyricLine(
    val timeMs: Long,
    val text: String
)

/**
 * 歌词显示组件（带滚动同步）
 * 
 * 已实现功能：
 * - ✅ 根据currentPosition自动滚动到当前歌词
 * - ✅ 高亮当前歌词行（主色 + 动画过渡）
 * - ✅ 点击歌词行跳转到对应时间
 * - ✅ .lrc 格式解析器（LrcParser）
 * 
 * TODO 功能扩展：
 * - 支持翻译歌词（双语显示）
 */
@Composable
fun LyricsPanel(
    lyrics: List<LyricLine>,
    currentPosition: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (lyrics.isEmpty()) {
        // 无歌词占位
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无歌词",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    // 找到当前播放位置对应的歌词索引
    val currentIndex = remember(currentPosition, lyrics) {
        lyrics.indexOfLast { it.timeMs <= currentPosition }.coerceAtLeast(0)
    }
    
    val listState = rememberLazyListState()
    
    // 自动滚动到当前歌词（居中显示）
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(
                index = currentIndex,
                scrollOffset = -100 // 居中偏移
            )
        }
    }
    
    GlassCard(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = lyrics,
                key = { _, line -> line.timeMs }
            ) { index, line ->
                val isActive = index == currentIndex
                
                AnimatedContent(
                    targetState = isActive,
                    transitionSpec = {
                        if (targetState) {
                            (fadeIn() + slideInVertically { -it / 2 })
                                .togetherWith(fadeOut())
                        } else {
                            fadeIn().togetherWith(fadeOut() + slideOutVertically { it / 2 })
                        }
                    },
                    label = "lyric_highlight"
                ) { active ->
                    Text(
                        text = line.text,
                        style = if (active) {
                            MaterialTheme.typography.bodyLarge
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        color = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeek(line.timeMs) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 简单的LRC解析器（示例实现）
 * 
 * LRC格式示例：
 * [00:12.00]第一句歌词
 * [00:17.20]第二句歌词
 */
object LrcParser {
    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val regex = """\[(\d{2}):(\d{2})\.(\d{2})\](.*)""".toRegex()
        
        lrcContent.lines().forEach { line ->
            regex.matchEntire(line.trim())?.let { match ->
                val (min, sec, ms, text) = match.destructured
                val timeMs = min.toLong() * 60000 + sec.toLong() * 1000 + ms.toLong() * 10
                if (text.isNotBlank()) {
                    lines.add(LyricLine(timeMs, text.trim()))
                }
            }
        }
        
        return lines.sortedBy { it.timeMs }
    }
}
