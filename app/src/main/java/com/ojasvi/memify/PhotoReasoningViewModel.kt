package com.ojasvi.memify

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.ojasvi.memify.BuildConfig.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotoReasoningViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    private val _uiState: MutableStateFlow<PhotoReasoningUiState> =
        MutableStateFlow(PhotoReasoningUiState.Initial)
    val uiState: StateFlow<PhotoReasoningUiState> =
        _uiState.asStateFlow()

    private val config = generationConfig {
        temperature = 0.7f
    }
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = API_KEY,
        generationConfig = config
    )

    fun reason(
        userInput: String,
        selectedImage: Uri?
    ) {

        _uiState.value = PhotoReasoningUiState.Loading
        val prompt = "Look at the image(s), and then answer the following question: $userInput"

        viewModelScope.launch(Dispatchers.IO) {

            val imageRequestBuilder = ImageRequest.Builder(app)
            val imageLoader = ImageLoader.Builder(app).build()

            val bitmap = try {
                val imageReq = imageRequestBuilder.data(selectedImage)
                    .size(768)
                    .precision(Precision.EXACT)
                    .build()

                val result = imageLoader.execute(imageReq)
                if (result is SuccessResult) {
                    (result.drawable as BitmapDrawable).bitmap
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
            if (bitmap == null) return@launch

            try {
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                var outputContent = ""

                generativeModel.generateContentStream(inputContent)
                    .collect { response ->
                        outputContent += response.text
                        _uiState.value = PhotoReasoningUiState.Success(outputContent)
                    }
            } catch (e: Exception) {
                _uiState.value = PhotoReasoningUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}
