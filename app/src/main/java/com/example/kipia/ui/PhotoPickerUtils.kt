package com.example.kipia.utils

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberPhotoPickerLauncher(
    onPhotosSelected: (List<Uri>) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(),
    onResult = { uris ->
        Log.d("PhotoPicker", "Photo picker result: ${uris?.size ?: 0} photos selected")
        try {
            onPhotosSelected(uris ?: emptyList())
        } catch (e: Exception) {
            Log.e("PhotoPicker", "Error in photo picker result", e)
            onPhotosSelected(emptyList())
        }
    }
)