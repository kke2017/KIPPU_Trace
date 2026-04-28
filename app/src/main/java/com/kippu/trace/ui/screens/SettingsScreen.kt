package com.kippu.trace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var showDevelopingDialog by remember { mutableStateOf(false) }
    var developingFeatureName by remember { mutableStateOf("") }

    if (showDevelopingDialog) {
        AlertDialog(
            onDismissRequest = { showDevelopingDialog = false },
            title = { Text("提示") },
            text = { Text("“$developingFeatureName”功能正在开发中，敬请期待！") },
            confirmButton = {
                TextButton(onClick = { showDevelopingDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "我的", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                SettingsSection(title = "通用设置") {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "深色模式",
                        subtitle = "跟随系统",
                        onClick = { 
                            developingFeatureName = "深色模式"
                            showDevelopingDialog = true 
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "主题配色",
                        subtitle = "时痕经典",
                        onClick = { 
                            developingFeatureName = "主题配色"
                            showDevelopingDialog = true 
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "数据安全") {
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = "数据备份与恢复",
                        subtitle = "本地导入/导出",
                        onClick = { 
                            developingFeatureName = "数据备份与恢复"
                            showDevelopingDialog = true 
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Default.ChevronRight,
                        title = "关于时痕",
                        subtitle = "Version 1.0.0",
                        onClick = { 
                            developingFeatureName = "关于时痕"
                            showDevelopingDialog = true 
                        }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) } // Space for bottom bar
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
