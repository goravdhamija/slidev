package com.globewaystechnologies.slidevideospy


import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.globewaystechnologies.slidevideospy.screens.MainScreen
import com.globewaystechnologies.slidevideospy.services.*
import com.globewaystechnologies.slidevideospy.ui.theme.SlideVideoSPYTheme
import com.globewaystechnologies.slidevideospy.utils.PermissionUtils
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings") // "settings" is the filename


class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {

            SlideVideoSPYTheme {
                MainScreen(modifier = Modifier,
                    sharedViewModel = sharedViewModel
                )

            }
        }


        if (!PermissionUtils.hasAllPermissions(this)) {
            PermissionUtils.requestAllPermissions(this)
        }

        // Check for overlay permission
        PermissionUtils.checkOverlayPermission(this)


    }

    companion object {
        private lateinit var instance: MainActivity
        fun getAppContext(): Context = instance.applicationContext
    }


}


@Preview(showBackground = true, name = "Greeting Preview")
@Composable
fun MainActivityPreview() {

    val fakeViewModel = object : SharedViewModel() {
        override val text = MutableStateFlow("Preview Text")
    }

    SlideVideoSPYTheme {
        MainScreen(modifier = Modifier,fakeViewModel)
    }
}



