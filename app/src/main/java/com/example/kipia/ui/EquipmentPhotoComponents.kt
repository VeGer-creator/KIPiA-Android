// app/src/main/java/com/example/kipia/ui/EquipmentPhotoComponents.kt
package com.example.kipia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.net.Uri

@Composable
fun PhotoPreviewRow(
    photoPaths: List<String>,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier // ДОБАВЛЕНО: параметр modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Фото:",
            style = MaterialTheme.typography.caption
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photoPaths.take(3)) { path ->
                AsyncImage(
                    model = Uri.parse("file://$path"),
                    contentDescription = "Превью фото",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPhotoClick() },
                    contentScale = ContentScale.Crop
                )
            }

            if (photoPaths.size > 3) {
                item {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.1F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${photoPaths.size - 3}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}