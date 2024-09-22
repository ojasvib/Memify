package com.ojasvi.memify.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ojasvi.memify.R
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.CaptureController
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PhotoReasoningScreen(
    modifier: Modifier = Modifier.navigationBarsPadding(),
    viewModel: PhotoReasoningViewModel = viewModel()
) {

    val photoReasoningUiState by viewModel.uiState.collectAsState()

    PhotoReasoningContents(
        uiState = photoReasoningUiState,
        onReasonClicked = viewModel::reason
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
@Composable
fun PhotoReasoningContents(
    uiState: PhotoReasoningUiState = PhotoReasoningUiState.Loading,
    onReasonClicked: (String, Uri) -> Unit = { _, _ -> }
) {
    var imageUri by rememberSaveable { mutableStateOf<Uri>(Uri.EMPTY) }
    var prompt by rememberSaveable { mutableStateOf("") }
    val captureController = rememberCaptureController()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .capturable(captureController)
        ) {
            sourceImage(imageUri, captureController)
            overlayTextCard(uiState = uiState)
        }

        InputField(
            prompt = prompt,
            onPromptChanged = { prompt = it },
            onSendClicked = { onReasonClicked(prompt, imageUri) },
            onImageSelected = { imageUri = it }
        )
    }
}

@OptIn(ExperimentalComposeApi::class)
@Composable
private fun sourceImage(imageUri: Uri, captureController: CaptureController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AsyncImage(
        model = imageUri,
        contentDescription = "",
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                scope.launch {
                    val bitmapAsync = captureController.captureAsync()
                    try {
                        val bitmap = bitmapAsync.await()
                        val bitmapPath = MediaStore.Images.Media.insertImage(
                            context.contentResolver,
                            bitmap.asAndroidBitmap(),
                            "palette",
                            "share palette"
                        )

                        val bitmapUri = Uri.parse(bitmapPath)

                        // Create the share intent
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, bitmapUri)
                        }

                        // Start the chooser activity
                        context.startActivity(Intent.createChooser(shareIntent, "Share"))
                    } catch (error: Throwable) {
                        // Error occurred, do something.
                    }
                }
            },
        contentScale = ContentScale.Crop
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun overlayTextCard(uiState: PhotoReasoningUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {

            PhotoReasoningUiState.Initial -> {}

            PhotoReasoningUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is PhotoReasoningUiState.Success -> {
                var scale by remember { mutableFloatStateOf(1f) }
                var rotationAngle by remember { mutableFloatStateOf(0f) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset {
                            IntOffset(
                                offsetX.roundToInt(),
                                offsetY.roundToInt()
                            )
                        } // Move the button
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = rotationAngle
                        ) //rotate and zoom
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, rotation ->
                                // Handle one-finger drag (by checking pan for drag amount)
                                if (pan != androidx.compose.ui.geometry.Offset.Zero) {
                                    offsetX += scale * pan.x
                                    offsetY += scale * pan.y
                                }

                                // Handle two-finger zoom
                                scale *= zoom

                                // Handle two-finger rotation
                                rotationAngle += rotation
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .fillMaxWidth()
                    ) {
                        var textFieldValue by remember { mutableStateOf(uiState.outputText) }

                        LaunchedEffect(uiState.outputText) {
                            textFieldValue = uiState.outputText
                        }

                        BasicTextField(value = textFieldValue,
                            onValueChange = { newText ->
                                textFieldValue = newText
                            },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                innerTextField()
                            }
                        )
                    }
                }
            }

            is PhotoReasoningUiState.Error -> {
                Card(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(all = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InputField(
    prompt: String,
    onPromptChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onImageSelected: (Uri) -> Unit,
) {
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { selectedImageUri ->
        selectedImageUri?.let { onImageSelected(it) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 14.dp)
                .navigationBarsPadding()
        ) {
            IconButton(
                onClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_image),
                )
            }
            OutlinedTextField(
                value = prompt,
                label = { Text(stringResource(R.string.reason_label)) },
                placeholder = { Text(stringResource(R.string.reason_hint)) },
                onValueChange = { onPromptChanged(it) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            )
            TextButton(
                onClick = { onSendClicked() },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(stringResource(R.string.action_go))
            }
        }
    }
}