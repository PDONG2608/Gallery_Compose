package com.example.multimediaapp

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.example.multimediaapp.ui.theme.MultimediaAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

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
                    MediaStoreScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MediaStoreScreen() {
    Log.i("dongdong","MediaStoreScreen")
    val readPermissionState = rememberPermissionState( Manifest.permission.READ_EXTERNAL_STORAGE)
    val hasReadPermission = readPermissionState.status.isGranted

    LaunchedEffect(Unit) {
        readPermissionState.launchPermissionRequest()
    }
    Log.i("dongdong","MediaStoreScreen - checkPermission")
    if (hasReadPermission) {
        Log.i("dongdong","MediaStoreScreen - hasPermission")
        val context = LocalContext.current
        val mediaItems by remember { mutableStateOf(loadMediaItems(context)) }
        MediaList(mediaItems)
    } else {
        Log.i("dongdong","MediaStoreScreen - not hasPermission")
        // Handle permission denied state
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
    val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    Log.i("dongdong","loadMediaItems: "+ imageUri+" videoUri"+ videoUri)

    val projection = arrayOf(
        MediaStore.MediaColumns._ID
    )

    context.contentResolver.query(imageUri, projection, null, null, null)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)

        Log.i("dongdong","loadCursor: "+ cursor.moveToFirst())
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val uri = ContentUris.withAppendedId(imageUri, id)
            Log.i("dongdong","id: "+ id+" uri: "+ uri)
            mediaItems.add(MediaItem(uri.toString()))
        }
    }

    context.contentResolver.query(videoUri, projection, null, null, null)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val uri = ContentUris.withAppendedId(videoUri, id)
            mediaItems.add(MediaItem(uri.toString()))
        }
    }

    return mediaItems
}

data class MediaItem(val uri: String)
