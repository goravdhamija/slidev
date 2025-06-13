package com.globewaystechnologies.slidevideospy.screens

import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Gallery_Sample_Grid(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val videoDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MediaSync")
    val videos = remember { videoDir.listFiles()?.filter { it.extension == "mp4" }?.sortedByDescending { it.lastModified() } ?: emptyList() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(videos) { file ->
            val thumb = remember(file) {
                ThumbnailUtils.createVideoThumbnail(file.toString(), MediaStore.Video.Thumbnails.MINI_KIND)
            }
            val fileSize = readableFileSize1(file.length())
            val dateTime = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(file.lastModified()))

            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                if (thumb != null) {
                    Image(
                        bitmap = thumb.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
                Text(text = file.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = dateTime, fontSize = 12.sp, color = Color.Gray)
                Text(text = fileSize, fontSize = 12.sp, color = Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "video/*")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Play")
                    }
                    TextButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share video via"))
                    }) {
                        Text("Share")
                    }
                    TextButton(onClick = {
                        file.delete()
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                }
            }
        }
    }
}

fun readableFileSize1(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
