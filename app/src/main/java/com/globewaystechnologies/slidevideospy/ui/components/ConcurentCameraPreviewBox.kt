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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry

class CameraPreviewController {

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var cameraSelector: CameraSelector? = null
    private var boundCamera: Camera? = null

    fun bindProvider(provider: ProcessCameraProvider) {
        cameraProvider = provider
    }

    fun setPreviewComponents(
        preview: Preview,
        surfaceProvider: Preview.SurfaceProvider,
        lifecycleOwner: LifecycleOwner,
        selector: CameraSelector,
        camera: Camera
    ) {
        this.preview = preview
        this.surfaceProvider = surfaceProvider
        this.lifecycleOwner = lifecycleOwner
        this.cameraSelector = selector
        this.boundCamera = camera
    }

    fun stopPreview() {
        cameraProvider?.unbindAll()
    }

    fun startPreview() {
        try {
            cameraProvider?.unbindAll()
            preview?.setSurfaceProvider(surfaceProvider)
            boundCamera = cameraProvider?.bindToLifecycle(
                lifecycleOwner!!,
                cameraSelector!!,
                preview!!
            )
        } catch (e: Exception) {
            Log.e("CameraPreviewController", "Failed to restart preview", e)
        }
    }

    fun setBoundCamera(camera: Camera) {
        this.boundCamera = camera
    }
}


class CameraPreviewLifecycleOwner : LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}


class StaticLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}


@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraPreviewBox(
    cameraId: String,
    modifier: Modifier = Modifier,
    controller: CameraPreviewController,
    lifecycleOwner: LifecycleOwner
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                controller.bindProvider(cameraProvider)

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Strict filter with Camera2Interop
                val cameraSelector = CameraSelector.Builder()
                    .addCameraFilter { cameraInfos ->
                        cameraInfos.filter { cameraInfo ->
                            val camera2CameraInfo = Camera2CameraInfo.from(cameraInfo)
                            Log.d("CameraPreview", "Available cameraId: ${camera2CameraInfo.cameraId}")
                            camera2CameraInfo.cameraId == cameraId
                        }
                    }
                    .build()

                try {
                    // ❗️DO NOT call unbindAll() blindly — only if this controller bound the camera
                    controller.stopPreview()

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )

                    controller.setBoundCamera(camera)
                    controller.setPreviewComponents(
                        preview,
                        previewView.surfaceProvider,
                        lifecycleOwner,
                        cameraSelector,
                        camera
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Failed to bind camera $cameraId: ${e.message}", e)
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}


@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun DualCameraPreviewView(
    frontCameraId: String,
    backCameraId: String,
    frontController: CameraPreviewController,
    backController: CameraPreviewController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val root = FrameLayout(ctx)

            val frontPreviewView = PreviewView(ctx)
            val backPreviewView = PreviewView(ctx)

            root.addView(frontPreviewView)
            root.addView(backPreviewView)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val frontPreview = Preview.Builder().build().also {
                    it.setSurfaceProvider(frontPreviewView.surfaceProvider)
                }
                val backPreview = Preview.Builder().build().also {
                    it.setSurfaceProvider(backPreviewView.surfaceProvider)
                }

                val frontSelector = CameraSelector.Builder()
                    .addCameraFilter { cameraInfos ->
                        cameraInfos.filter {
                            Camera2CameraInfo.from(it).cameraId == frontCameraId
                        }
                    }.build()

                val backSelector = CameraSelector.Builder()
                    .addCameraFilter { cameraInfos ->
                        cameraInfos.filter {
                            Camera2CameraInfo.from(it).cameraId == backCameraId
                        }
                    }.build()

                try {
                    cameraProvider.unbindAll() // unbind just once here

                    val frontCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, frontSelector, frontPreview
                    )

                    val backCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, backSelector, backPreview
                    )

                    frontController.bindProvider(cameraProvider)
                    frontController.setPreviewComponents(
                        frontPreview,
                        frontPreviewView.surfaceProvider,
                        lifecycleOwner,
                        frontSelector,
                        frontCamera
                    )

                    backController.bindProvider(cameraProvider)
                    backController.setPreviewComponents(
                        backPreview,
                        backPreviewView.surfaceProvider,
                        lifecycleOwner,
                        backSelector,
                        backCamera
                    )

                } catch (e: Exception) {
                    Log.e("DualCamera", "Failed to bind both previews", e)
                }

            }, ContextCompat.getMainExecutor(ctx))

            root
        },
        modifier = modifier
    )
}



class DualPreviewServiceWithIds(
    context: Context,
    private val frontCameraId: String,
    private val backCameraId: String,
    private val frontSurface: Surface,
    private val backSurface: Surface
) {
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
                val request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(surface)
                }.build()
                session.setRepeatingRequest(request, null, null)
                onConfigured(session)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("DualPreviewService", "Camera capture session configuration failed.")
            }
        }, null)
    }

    @SuppressLint("MissingPermission")
    fun start() {
        cameraManager.openCamera(frontCameraId, cameraCallback, null)
        cameraManager.openCamera(backCameraId, cameraCallback, null)
    }

    fun stop() {
        frontSession?.close(); backSession?.close()
        frontDevice?.close(); backDevice?.close()
    }
}





@Composable
fun DualCameraPreviewScreenWithParams(
    frontCameraId: String,
    backCameraId: String,
    frontModifier: Modifier = Modifier,
    backModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val layout = remember { FrameLayout(context) }
    var isSetup by remember { mutableStateOf(false) }
    var service: DualPreviewServiceWithIds? by remember { mutableStateOf(null) }

    AndroidView(factory = {
        layout.apply {
            val frontView = SurfaceView(context).apply {
                holder.setFixedSize(320, 180)
            }
            val backView = SurfaceView(context).apply {
                holder.setFixedSize(320, 180)
            }
            addView(frontView, FrameLayout.LayoutParams(320, 180).apply {
                gravity = Gravity.TOP or Gravity.START
                marginStart = 16
                topMargin = 16
            })
            addView(backView, FrameLayout.LayoutParams(320, 180).apply {
                gravity = Gravity.TOP or Gravity.END
                marginEnd = 16
                topMargin = 16
            })

            if (!isSetup) {
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
    }, modifier = Modifier.fillMaxSize())

    DisposableEffect(Unit) {
        onDispose { service?.stop() }
    }
}


