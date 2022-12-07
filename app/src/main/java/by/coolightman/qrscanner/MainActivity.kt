package by.coolightman.qrscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.coolightman.qrscanner.component.AppCameraView
import by.coolightman.qrscanner.ui.theme.QRScannerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            QRScannerTheme(darkTheme = true) {
                val systemUiController = rememberSystemUiController()
                val barsColor = MaterialTheme.colors.background

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = barsColor, darkIcons = false
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    val context = LocalContext.current
                    val clipboardManager = LocalClipboardManager.current

                    val cameraProviderFuture = remember {
                        ProcessCameraProvider.getInstance(context)
                    }

                    val cameraPermissionState =
                        rememberPermissionState(permission = android.Manifest.permission.CAMERA)
                    LaunchedEffect(Unit) {
                        cameraPermissionState.launchPermissionRequest()
                    }

                    var code by remember {
                        mutableStateOf("")
                    }
                    var isCopyToClipboard by remember {
                        mutableStateOf(false)
                    }
                    LaunchedEffect(isCopyToClipboard) {
                        if (isCopyToClipboard) {
                            clipboardManager.setText(AnnotatedString(code))
                            Toast.makeText(context, "Сopied to clipboard", Toast.LENGTH_SHORT)
                                .show()
                            isCopyToClipboard = false
                        }
                    }

                    var isShowFindDialog by remember {
                        mutableStateOf(false)
                    }

                    if (isShowFindDialog) {
                        AlertDialog(title = {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "QR-code found")
                                Icon(
                                    painter = painterResource(R.drawable.ic_baseline_content_copy_24),
                                    contentDescription = "copy",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { isCopyToClipboard = true }
                                )
                            }
                        }, text = {
                            Text(text = code)
                        }, onDismissRequest = {
                            isShowFindDialog = false
                        }, confirmButton = {
                            TextButton(onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(code))
                                startActivity(browserIntent)
                            }) {
                                Text(text = "Open in browser")
                            }
                        }, dismissButton = {
                            TextButton(onClick = { isShowFindDialog = false }) {
                                Text(text = "Cancel")
                            }
                        }, shape = MaterialTheme.shapes.small
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (cameraPermissionState.status.isGranted) {
                            AppCameraView(cameraProviderFuture = cameraProviderFuture,
                                onGetResult = {
                                    code = it
                                    isShowFindDialog = true
                                })
                        } else {
                            if (cameraPermissionState.status.shouldShowRationale) {
                                Text(
                                    text = "QR-code can't work without this permission.\n" + "Please grant the permission",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )

                                Button(
                                    shape = CircleShape,
                                    onClick = { cameraPermissionState.launchPermissionRequest() },
                                ) {
                                    Text(text = "Request permission")
                                }

                            } else {
                                Text(
                                    text = "Sorry, but QR-code can't work without this permission.\n" + "You can change the permission in settings",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )

                                Button(
                                    shape = CircleShape,
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                                            )
                                        )
                                    },
                                ) {
                                    Text(text = "Settings")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}