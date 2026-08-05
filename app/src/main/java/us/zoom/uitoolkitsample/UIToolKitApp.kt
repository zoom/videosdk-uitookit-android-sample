package us.zoom.uitoolkitsample

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import us.zoom.sdk.ZoomVideoSDK
import us.zoom.sdk.ZoomVideoSDKInitParams
import us.zoom.uitoolkit.manager.ZMUIToolKitManager

class UIToolKitApp : Application() {
    companion object {
        lateinit var instance: UIToolKitApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val sdkInitParams = ZoomVideoSDKInitParams().apply {
            domain = "zoom.us"
            enableLog = true
        }
        ZoomVideoSDK.getInstance().initialize(this, sdkInitParams)
        ZMUIToolKitManager.sharedInstance().attach(this, ZoomVideoSDK.getInstance())


    }

    fun getAppContext(): UIToolKitApp {
        return instance
    }
}