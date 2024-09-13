package com.ojasvi.memify

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ojasvi.memify.ui.theme.MemifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemifyTheme {
                MainScreen()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Memify")})},
        content = {ImagePicker()},
    )
}

@Composable
fun ImagePicker(modifier: Modifier = Modifier){
    var imageUri by remember { mutableStateOf<Uri?>(null)}
    
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent())
    { uri: Uri? ->
        imageUri = uri
    }
    
    LaunchedEffect(Unit) {
        imagePickerLauncher.launch("image/*")
    }
    
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ){
        imageUri?.let { uri ->
            Image(painter = rememberAsyncImagePainter(uri), contentDescription = null,
                modifier = Modifier.size(200.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MemifyTheme {
        MainScreen()
    }
}

