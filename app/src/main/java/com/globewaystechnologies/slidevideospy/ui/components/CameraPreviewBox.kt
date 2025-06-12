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
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.lifecycle.LifecycleOwner

class CameraPreviewController2 {

    private var cameraProvider: ProcessCameraProvider? = null
    private var boundCamera: Camera? = null
    private var preview: Preview? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var surfaceProvider: Preview.SurfaceProvider? = null
    private var cameraSelector: CameraSelector? = null

    fun bindProvider(provider: ProcessCameraProvider) {
        this.cameraProvider = provider
    }

    fun setBoundCamera(camera: Camera) {
        this.boundCamera = camera
    }

    fun setPreviewComponents(
        preview: Preview,
        surfaceProvider: Preview.SurfaceProvider,
        lifecycleOwner: LifecycleOwner,
        selector: CameraSelector
    ) {
        this.preview = preview
        this.surfaceProvider = surfaceProvider
        this.lifecycleOwner = lifecycleOwner
        this.cameraSelector = selector
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
            Log.e("CameraPreviewController", "Failed to restart preview: ${e.localizedMessage}", e)
        }
    }
}


//@OptIn(ExperimentalCamera2Interop::class)
//@Composable
//fun CameraPreviewBox2(
//    cameraId: String,
//    modifier: Modifier = Modifier,
//    controller: CameraPreviewController
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    AndroidView(
//        modifier = modifier,
//        factory = { ctx ->
//            val previewView = PreviewView(ctx)
//            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
//
//            cameraProviderFuture.addListener({
//                val cameraProvider = cameraProviderFuture.get()
//                controller.bindProvider(cameraProvider)
//
//                val preview = Preview.Builder().build().also {
//                    it.setSurfaceProvider(previewView.surfaceProvider)
//                }
//
//                val cameraSelector = CameraSelector.Builder()
//                    .addCameraFilter { cameraInfos ->
//                        cameraInfos.filter { cameraInfo ->
//                            val camera2CameraInfo = Camera2CameraInfo.from(cameraInfo)
//                            camera2CameraInfo.cameraId == cameraId
//                        }
//                    }
//                    .build()
//
//                try {
//                    cameraProvider.unbindAll()
//                    val camera = cameraProvider.bindToLifecycle(
//                        lifecycleOwner,
//                        cameraSelector,
//                        preview
//                    )
//                    controller.setBoundCamera(camera)
//                    controller.setPreviewComponents(
//                        preview,
//                        previewView.surfaceProvider,
//                        lifecycleOwner,
//                        cameraSelector
//                    )
//                } catch (e: Exception) {
//                    Log.e("CameraPreview", "Failed to bind camera use cases", e)
//                }
//
//            }, ContextCompat.getMainExecutor(ctx))
//
//            previewView
//        }
//    )
//}
