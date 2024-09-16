package com.ojasvi.memify.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ojasvi.memify.PhotoReasoningUiState
import com.ojasvi.memify.PhotoReasoningViewModel
import com.ojasvi.memify.R

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

@Composable
fun PhotoReasoningContents(
    uiState: PhotoReasoningUiState = PhotoReasoningUiState.Loading,
    onReasonClicked: (String, Uri) -> Unit = { _, _ -> }
) {
    var userQuestion by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { selectedImageUri ->
        selectedImageUri?.let {
            imageUri = selectedImageUri
        }
    }

    Column(
        modifier = Modifier
            .padding(all = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterHorizontally)
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_image),
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            pickMedia.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                )
                OutlinedTextField(
                    value = userQuestion,
                    label = { Text(stringResource(R.string.reason_label)) },
                    placeholder = { Text(stringResource(R.string.reason_hint)) },
                    onValueChange = { userQuestion = it },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                )
                Text(text = stringResource(R.string.action_go),
                    modifier = Modifier
                        .padding(all = 4.dp)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            if (userQuestion.isNotBlank()) {
                                imageUri?.let { onReasonClicked(userQuestion, it) }
                            }
                        }
                )
            }

        }

        when (uiState) {
            PhotoReasoningUiState.Initial -> {
                // Nothing is shown
            }

            PhotoReasoningUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }

            is PhotoReasoningUiState.Success -> {
                Card(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "Person Icon",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .requiredSize(36.dp)
                                .drawBehind {
                                    drawCircle(color = Color.White)
                                }
                        )
                        Text(
                            text = uiState.outputText, // TODO(thatfiredev): Figure out Markdown support
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .fillMaxWidth()
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