package us.zoom.uitoolkitsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import us.zoom.sdk.ZoomVideoSDK
import us.zoom.sdk.ZoomVideoSDKAudioOption
import us.zoom.sdk.ZoomVideoSDKSessionContext
import us.zoom.sdk.ZoomVideoSDKVideoOption
import us.zoom.uitoolkit.manager.ZMUIToolKitManager

class JoinActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val MODE_CREATE = "create"
        const val MODE_JOIN = "join"

        private const val PREFS_NAME = "join_prefs"
        private const val KEY_SESSION_NAME = "session_name"
        private const val KEY_DISPLAY_NAME = "display_name"
    }

    private lateinit var joinBtn: MaterialButton
    private lateinit var tvTitle: TextView
    private lateinit var sessionNameInput: TextInputEditText
    private lateinit var displayNameInput: TextInputEditText
    private lateinit var sessionPasswordInput: TextInputEditText
    private var isCreateMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)
        joinBtn = findViewById(R.id.btnJoin)
        tvTitle = findViewById(R.id.tvTitle)
        sessionNameInput = findViewById(R.id.sessionName)
        displayNameInput = findViewById(R.id.displayName)
        sessionPasswordInput = findViewById(R.id.sessionPassword)

        isCreateMode = intent.getStringExtra(EXTRA_MODE) == MODE_CREATE
        tvTitle.text = if (isCreateMode) "Create a Session" else "Join a Session"

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sessionNameInput.setText(prefs.getString(KEY_SESSION_NAME, ""))
        displayNameInput.setText(prefs.getString(KEY_DISPLAY_NAME, ""))
        // Wipe any password persisted by older builds.
        if (prefs.contains("session_password")) {
            prefs.edit().remove("session_password").apply()
        }


        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        updateJoinBtn()
    }

    override fun onResume() {
        super.onResume()
        updateJoinBtn()
    }

    private fun updateJoinBtn() {
        val isInSession = ZMUIToolKitManager.sharedInstance().isInSession()
        joinBtn.text = when {
            isInSession -> "Return to session"
            isCreateMode -> "Create"
            else -> "Join"
        }
        joinBtn.setOnClickListener {
            if (isInSession) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                doJoin()
            }

        }
    }

    private fun doJoin() {
        val sessionName = sessionNameInput.text?.toString()?.trim().orEmpty()
        val displayName = displayNameInput.text?.toString()?.trim().orEmpty()
        val sessionPassword = sessionPasswordInput.text?.toString()?.trim().orEmpty()

        val session = sessionName.ifEmpty { "monument-valley-770" }
        val user = displayName.ifEmpty { "Android User" }

        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_SESSION_NAME, session)
            putString(KEY_DISPLAY_NAME, user)
            apply()
        }
        val token = JwtTokenHelper.generateZoomVideoSdkToken(
            sdkKey = Constants.SDK_KEY,
            sdkSecret = Constants.SDK_SECRET,
            sessionName = session,
            userIdentity = user
        )

        val sessionContext = ZoomVideoSDKSessionContext().apply {
            this.sessionName = session
            this.userName = user
            if (sessionPassword.isNotEmpty()) this.sessionPassword = sessionPassword
            this.token = token
            videoOption = ZoomVideoSDKVideoOption().apply { localVideoOn = true }
            audioOption = ZoomVideoSDKAudioOption().apply { connect = true; mute = false }
        }
        ZoomVideoSDK.getInstance().joinSession(sessionContext)
        startActivity(Intent(this, MainActivity::class.java))
    }
}
