package com.example.multimediaapp

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.multimediaapp.ui.theme.MultimediaAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultimediaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Log.i("dongdong","onCreate")
                    RequestPermission()
                }
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermission() {
    val videoPermissionState  = rememberPermissionState( Manifest.permission.READ_MEDIA_VIDEO)
    if(videoPermissionState.status.isGranted){
        Log.i("dongdong","Permission is Granted")
        val context = LocalContext.current
        val mediaItems by remember { mutableStateOf(loadMediaItems(context)) }
        createLazyView(mediaItems)
        MediaList(mediaItems)
    }else{
        Column {
            val textToShow = if (videoPermissionState.status.shouldShowRationale) {
                "The camera is important for this app. Please grant the permission."
            } else {
                "External Storage permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { videoPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}
@Composable
fun createLazyView(mediaItems: List<MediaItem>) {
    val numbers = (0..20).toList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Changed to 3 columns as per your request
        modifier = Modifier.padding(16.dp)
    ) {
        items(mediaItems) { mediaItems ->
            Log.i("dongdong", "uri build: "+ mediaItems.uri)
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .background(Color.Red)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(mediaItems.uri)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(5.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


@Composable
fun MediaList(mediaItems: List<MediaItem>) {
    LazyColumn {
        items(mediaItems) { item ->
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberAsyncImagePainter(item.uri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

fun loadMediaItems(context: Context): List<MediaItem> {
    val mediaItems = mutableListOf<MediaItem>()
    val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI


    val projection = arrayOf(MediaStore.MediaColumns._ID)
    context.contentResolver.query(videoUri, projection, null, null, null)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        Log.i("dongdong","cursor.moveToFirst() : "+  cursor.moveToFirst())
        cursor.moveToFirst()
        do {
            val id = cursor.getLong(idColumn)
            val uri = ContentUris.withAppendedId(videoUri, id)
            Log.i("dongdong","uri : "+ uri)
            mediaItems.add(MediaItem(uri.toString()))
        } while (cursor.moveToNext())
    }

    return mediaItems
}

data class MediaItem(val uri: String)
