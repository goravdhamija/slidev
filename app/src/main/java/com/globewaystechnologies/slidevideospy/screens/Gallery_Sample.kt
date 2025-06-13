package com.globewaystechnologies.slidevideospy.screens

import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import java.io.File

@Composable
fun Gallery_Sample(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val videoDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MediaSync")

    var videos by remember { mutableStateOf(videoDir.listFiles()?.filter { it.extension == "mp4" }?.sortedByDescending { it.lastModified() } ?: emptyList()) }

    if (videos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No videos found.", color = Color.Gray)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(videos) { video ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(video.name, style = MaterialTheme.typography.titleSmall)
                        Text("Size: ${video.length() / 1024} KB", style = MaterialTheme.typography.bodySmall)
                    }

                    IconButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", video)
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "video/mp4")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }

                    IconButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", video)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share video via"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = {
                        if (video.delete()) {
                            Toast.makeText(context, "Deleted ${video.name}", Toast.LENGTH_SHORT).show()
                            videos = videoDir.listFiles()?.filter { it.extension == "mp4" }?.sortedByDescending { it.lastModified() } ?: emptyList()
                        } else {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}
