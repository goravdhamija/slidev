package com.globewaystechnologies.slidevideospy.screens

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel

@Composable
fun Gallery_Grid_List_Sample(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MediaSync")
    val videoFiles = remember { mutableStateListOf<File>() }
    var isGrid by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (publicDir.exists()) {
            videoFiles.clear()
            videoFiles.addAll(publicDir.listFiles()?.filter { it.extension == "mp4" }?.sortedByDescending { it.lastModified() } ?: emptyList())
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { isGrid = !isGrid }) {
                Icon(imageVector = if (isGrid) Icons.Default.List else Icons.Default.GridView, contentDescription = "Toggle View")
            }
        }

        if (videoFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No videos found.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            if (isGrid) {
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
                    items(videoFiles) { file ->
                        VideoCard_Grid_List_Sample(file = file, context = context, isGrid = true) {
                            file.delete()
                            videoFiles.remove(file)
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(videoFiles) { file ->
                        VideoCard_Grid_List_Sample(file = file, context = context, isGrid = false) {
                            file.delete()
                            videoFiles.remove(file)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCard_Grid_List_Sample(file: File, context: android.content.Context, isGrid: Boolean, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val fileSize = formatFileSize_Grid_List_Sample(file.length())
    val thumbnail by produceState<ImageBitmap?>(initialValue = null, file) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val bitmap = retriever.frameAtTime
            value = bitmap?.asImageBitmap()
        } catch (e: Exception) {
            value = null
        } finally {
            retriever.release()
        }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        if (isGrid) {
            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                thumbnail?.let {
                    Image(bitmap = it, contentDescription = null, modifier = Modifier.size(150.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = file.name, maxLines = 1)
                Text(text = "Date: ${dateFormat.format(Date(file.lastModified()))}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Size: $fileSize", style = MaterialTheme.typography.bodySmall)
                Row {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(FileProvider.getUriForFile(context, context.packageName + ".provider", file), "video/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                    IconButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/mp4"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { onDelete() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        } else {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                thumbnail?.let {
                    Image(bitmap = it, contentDescription = null, modifier = Modifier.size(100.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = file.name, maxLines = 1)
                    Text(text = "Date: ${dateFormat.format(Date(file.lastModified()))}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Size: $fileSize", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(FileProvider.getUriForFile(context, context.packageName + ".provider", file), "video/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                    IconButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/mp4"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { onDelete() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

fun formatFileSize_Grid_List_Sample(bytes: Long): String {
    val df = DecimalFormat("#.##")
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0

    return when {
        tb >= 1 -> df.format(tb) + " TB"
        gb >= 1 -> df.format(gb) + " GB"
        mb >= 1 -> df.format(mb) + " MB"
        kb >= 1 -> df.format(kb) + " KB"
        else -> "$bytes B"
    }
}
