package com.example.myapplication.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.myapplication.data.model.AppInfo
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    appInfo: AppInfo,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    listMembershipCount: Int = 0,
    onIconClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onInfoClick,
                    onLongClick = onLongClick
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox or App Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        enabled = !isSelectionMode,
                        onClick = onIconClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelectionMode) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                } else {
                    AppIcon(
                        icon = appInfo.icon,
                        contentDescription = appInfo.title,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status badges
                    AppStatusBadge(appInfo = appInfo)

                    if (listMembershipCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        ListMembershipBadge(count = listMembershipCount)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "v${appInfo.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatSize(appInfo.apkSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "SDK ${appInfo.targetSdk}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AppIcon(
    icon: Drawable?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (icon != null) {
        val bitmap = remember(icon) {
            icon.toBitmap().asImageBitmap()
        }
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun AppStatusBadge(
    appInfo: AppInfo,
    modifier: Modifier = Modifier
) {
    when (appInfo.status) {
        AppInfo.AppStatus.SYSTEM -> {
            Badge(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = modifier
            ) {
                Text(
                    text = "System",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        AppInfo.AppStatus.MISSING -> {
            Badge(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = modifier
            ) {
                Text(
                    text = "Missing",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        AppInfo.AppStatus.INSTALLED -> {
            // No badge for installed user apps
        }
    }
}

@Composable
fun ListMembershipBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Badge(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun MissingAppBadge(
    modifier: Modifier = Modifier
) {
    Badge(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier
    ) {
        Text(
            text = "Missing",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
