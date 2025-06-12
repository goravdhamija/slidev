import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.util.Log
import android.view.Gravity
import android.view.Surface
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry

class DualPreviewServiceWithIds(
    context: Context,
    private val frontCameraId: String?,
    private val backCameraId: String?,
    private val frontSurface: Surface,
    private val backSurface: Surface
) {

    companion object {
        var currentInstance: DualPreviewServiceWithIds? = null

        fun releaseCurrent() {
            currentInstance?.stop()
            currentInstance = null
        }
    }

    init {
        currentInstance = this
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var frontDevice: CameraDevice? = null
    private var backDevice: CameraDevice? = null
    private var frontSession: CameraCaptureSession? = null
    private var backSession: CameraCaptureSession? = null

    private val cameraCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(device: CameraDevice) {
            if (device.id == frontCameraId) {
                frontDevice = device
                startSession(device, frontSurface) { frontSession = it }
            } else if (device.id == backCameraId) {
                backDevice = device
                startSession(device, backSurface) { backSession = it }
            }
        }

        override fun onDisconnected(device: CameraDevice) = device.close()
        override fun onError(device: CameraDevice, error: Int) = device.close()
    }

    private fun startSession(
        device: CameraDevice,
        surface: Surface,
        onConfigured: (CameraCaptureSession) -> Unit
    ) {
        device.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                val request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    .apply { addTarget(surface) }
                    .build()
                session.setRepeatingRequest(request, null, null)
                onConfigured(session)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("DualPreviewService", "Capture session configuration failed.")
            }
        }, null)
    }

    @SuppressLint("MissingPermission")
    fun start() {
        frontCameraId?.let { cameraManager.openCamera(it, cameraCallback, null) }
        backCameraId?.let { cameraManager.openCamera(it, cameraCallback, null) }
    }

    fun stop() {
        frontSession?.close(); frontSession = null
        backSession?.close(); backSession = null
        frontDevice?.close(); frontDevice = null
        backDevice?.close(); backDevice = null
    }
}




@Composable
fun DualCameraPreviewScreenWithParams(
    frontCameraId: String?,
    backCameraId: String?,
    frontModifier: Modifier = Modifier,
    backModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSetup by remember { mutableStateOf(false) }
    var service: DualPreviewServiceWithIds? by remember { mutableStateOf(null) }

    val frontView = remember { SurfaceView(context) }
    val backView = remember { SurfaceView(context) }

    // Launch preview setup based on available IDs
    LaunchedEffect(Unit) {
        if (!isSetup && !frontCameraId.isNullOrBlank() || !backCameraId.isNullOrBlank()) {
            service = DualPreviewServiceWithIds(
                context = context,
                frontCameraId = frontCameraId,
                backCameraId = backCameraId,
                frontSurface = frontView.holder.surface,
                backSurface = backView.holder.surface
            )
            service?.start()
            isSetup = true
        }
    }

    // Layout dynamically based on available camera views
    Row(modifier = Modifier.fillMaxSize()) {
        if (!frontCameraId.isNullOrBlank()) {
            AndroidView(
                factory = { frontView },
                modifier = frontModifier
            )
        }
        if (!backCameraId.isNullOrBlank()) {
            AndroidView(
                factory = { backView },
                modifier = backModifier
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            service?.stop()
        }
    }
}
