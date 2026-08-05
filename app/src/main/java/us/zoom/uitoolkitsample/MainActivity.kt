package us.zoom.uitoolkitsample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import us.zoom.sdk.IncomingLiveStreamStatus
import us.zoom.sdk.RealTimeMediaStreamsFailReason
import us.zoom.sdk.RealTimeMediaStreamsStatus
import us.zoom.sdk.SubSessionKit
import us.zoom.sdk.SubSessionUserHelpRequestHandler
import us.zoom.sdk.UVCCameraStatus
import us.zoom.sdk.ZoomVideoSDK
import us.zoom.sdk.ZoomVideoSDKAnnotationHelper
import us.zoom.sdk.ZoomVideoSDKAnnotationToolType
import us.zoom.sdk.ZoomVideoSDKAudioHelper
import us.zoom.sdk.ZoomVideoSDKAudioRawData
import us.zoom.sdk.ZoomVideoSDKBroadcastControlStatus
import us.zoom.sdk.ZoomVideoSDKCRCCallStatus
import us.zoom.sdk.ZoomVideoSDKCameraControlRequestHandler
import us.zoom.sdk.ZoomVideoSDKCameraControlRequestType
import us.zoom.sdk.ZoomVideoSDKChatHelper
import us.zoom.sdk.ZoomVideoSDKChatMessage
import us.zoom.sdk.ZoomVideoSDKChatMessageDeleteType
import us.zoom.sdk.ZoomVideoSDKChatPrivilegeType
import us.zoom.sdk.ZoomVideoSDKDataType
import us.zoom.sdk.ZoomVideoSDKDelegate
import us.zoom.sdk.ZoomVideoSDKEmojiReactionType
import us.zoom.sdk.ZoomVideoSDKExportFormat
import us.zoom.sdk.ZoomVideoSDKFileTransferStatus
import us.zoom.sdk.ZoomVideoSDKLiveStreamHelper
import us.zoom.sdk.ZoomVideoSDKLiveStreamStatus
import us.zoom.sdk.ZoomVideoSDKLiveTranscriptionHelper
import us.zoom.sdk.ZoomVideoSDKMultiCameraStreamStatus
import us.zoom.sdk.ZoomVideoSDKNetworkStatus
import us.zoom.sdk.ZoomVideoSDKPasswordHandler
import us.zoom.sdk.ZoomVideoSDKPhoneFailedReason
import us.zoom.sdk.ZoomVideoSDKPhoneStatus
import us.zoom.sdk.ZoomVideoSDKProxySettingHandler
import us.zoom.sdk.ZoomVideoSDKQOSStatistics
import us.zoom.sdk.ZoomVideoSDKRawDataPipe
import us.zoom.sdk.ZoomVideoSDKReceiveFile
import us.zoom.sdk.ZoomVideoSDKRecordingConsentHandler
import us.zoom.sdk.ZoomVideoSDKRecordingStatus
import us.zoom.sdk.ZoomVideoSDKSSLCertificateInfo
import us.zoom.sdk.ZoomVideoSDKSendFile
import us.zoom.sdk.ZoomVideoSDKSessionLeaveReason
import us.zoom.sdk.ZoomVideoSDKShareAction
import us.zoom.sdk.ZoomVideoSDKShareHelper
import us.zoom.sdk.ZoomVideoSDKShareSetting
import us.zoom.sdk.ZoomVideoSDKShareStatus
import us.zoom.sdk.ZoomVideoSDKStreamingJoinStatus
import us.zoom.sdk.ZoomVideoSDKSubSessionManager
import us.zoom.sdk.ZoomVideoSDKSubSessionParticipant
import us.zoom.sdk.ZoomVideoSDKSubSessionStatus
import us.zoom.sdk.ZoomVideoSDKTestMicStatus
import us.zoom.sdk.ZoomVideoSDKUser
import us.zoom.sdk.ZoomVideoSDKUserHelpRequestResult
import us.zoom.sdk.ZoomVideoSDKUserHelper
import us.zoom.sdk.ZoomVideoSDKVideoCanvas
import us.zoom.sdk.ZoomVideoSDKVideoHelper
import us.zoom.sdk.ZoomVideoSDKVideoSubscribeFailReason
import us.zoom.sdk.ZoomVideoSDKVideoView
import us.zoom.sdk.ZoomVideoSDKWhiteboardHelper
import us.zoom.uitoolkit.ZMUIToolKitAudioBtn
import us.zoom.uitoolkit.ZMUIToolKitLeaveBtn
import us.zoom.uitoolkit.ZMUIToolKitMoreBtn
import us.zoom.uitoolkit.ZMUIToolKitParticipantsBtn
import us.zoom.uitoolkit.ZMUIToolKitShareBtn
import us.zoom.uitoolkit.ZMUIToolKitVideoBtn
import us.zoom.uitoolkit.ZMUIToolkitVideoPage
import us.zoom.uitoolkit.manager.ZMUIToolKitManager
import us.zoom.uitoolkit.manager.ZMUIToolKitPiPHelper
import us.zoom.uitoolkitsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ZoomVideoSDKDelegate {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var videoBtn: ZMUIToolKitVideoBtn
    private lateinit var audioBtn: ZMUIToolKitAudioBtn
    private lateinit var shareBtn: ZMUIToolKitShareBtn
    private lateinit var leaveBtn: ZMUIToolKitLeaveBtn
    private lateinit var backBtn: ImageButton
    private lateinit var participantsBtn: ZMUIToolKitParticipantsBtn
    private lateinit var moreBtn: ZMUIToolKitMoreBtn
    private lateinit var sessionNameTextView: TextView
    private lateinit var videoPage: ZMUIToolkitVideoPage
    private lateinit var connectingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must be before setContentView so SurfaceViews position correctly in edge-to-edge mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        ZoomVideoSDK.getInstance().addListener(this)
        videoBtn = binding.videoBtn
        audioBtn = binding.audioBtn
        shareBtn = binding.shareBtn
        leaveBtn = binding.leaveBtn
        backBtn = binding.backBtn
        sessionNameTextView = binding.sessionNameText
        participantsBtn = binding.participantsBtn
        moreBtn = binding.moreBtn
        videoPage = binding.videoPage
        connectingText = binding.connectingText
        val inSession = ZMUIToolKitManager.sharedInstance().isInSession()
        videoBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        audioBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        shareBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        leaveBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        participantsBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        moreBtn.visibility = if (inSession) View.VISIBLE else View.GONE
        connectingText.visibility = if (inSession) View.GONE else View.VISIBLE
        if (inSession) {
            sessionNameTextView.text = ZoomVideoSDK.getInstance().session?.sessionName
        }
        backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            applyWindowInsets(windowInsets)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            ViewCompat.getRootWindowInsets(binding.root)?.let { applyWindowInsets(it) }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Hide chrome and force a synchronous layout so the PiP snapshot doesn't
        // include the top/bottom bars. onResume restores them.
        binding.topBar.visibility = View.GONE
        binding.bottomControlBar.visibility = View.GONE
        val root = binding.root
        if (root.width > 0 && root.height > 0) {
            root.measure(
                View.MeasureSpec.makeMeasureSpec(root.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(root.height, View.MeasureSpec.EXACTLY),
            )
            root.layout(root.left, root.top, root.right, root.bottom)
        }
        ZMUIToolKitPiPHelper.onUserLeaveHint(this)
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isInPictureInPictureMode) {
            binding.topBar.visibility = View.VISIBLE
            binding.bottomControlBar.visibility = View.VISIBLE
        }
    }

    private fun applyWindowInsets(windowInsets: WindowInsetsCompat) {
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        binding.topBar.updatePadding(top = insets.top)

        var leftPad = insets.left
        var rightPad = insets.right
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            windowInsets.toWindowInsets()?.run {
                leftPad = maxOf(leftPad,
                    (getRoundedCorner(android.view.RoundedCorner.POSITION_BOTTOM_LEFT)?.radius ?: 0) / 2)
                rightPad = maxOf(rightPad,
                    (getRoundedCorner(android.view.RoundedCorner.POSITION_BOTTOM_RIGHT)?.radius ?: 0) / 2)
            }
        }
        binding.bottomControlBar.updatePadding(left = leftPad, right = rightPad, bottom = insets.bottom)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in permissions.indices) {
            if (Manifest.permission.CAMERA == permissions[i] &&
                grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                videoBtn.visibility = View.VISIBLE
            }
            if (Manifest.permission.RECORD_AUDIO == permissions[i] &&
                grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                audioBtn.visibility = View.VISIBLE
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ZMUIToolKitManager.REQUEST_SHARE_SCREEN_PERMISSION) {
            Log.d(TAG, "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION")
            if (resultCode != RESULT_OK) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION no ok ")
                return
            }
            ZMUIToolKitManager.sharedInstance().startShareScreen(this, data!!, MainActivity::class.java)
        }
    }

    private fun requestPermission(): Boolean {
        var permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= 34) {
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_PHONE_STATE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_PHONE_STATE
            )
        } else if (Build.VERSION.SDK_INT >= 31) {
            permissions = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            )
        }
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1010)
                return false
            } else {
                when (permission) {
                    Manifest.permission.CAMERA -> videoBtn.visibility = View.VISIBLE
                    Manifest.permission.RECORD_AUDIO -> audioBtn.visibility = View.VISIBLE
                }
            }
        }
        return true
    }

    // region ZoomVideoSDKDelegate

    override fun onSessionJoin() {
        connectingText.visibility = View.GONE
        requestPermission()
        shareBtn.visibility = View.VISIBLE
        leaveBtn.visibility = View.VISIBLE
        participantsBtn.visibility = View.VISIBLE
        sessionNameTextView.text = ZoomVideoSDK.getInstance().session?.sessionName
        moreBtn.visibility = View.VISIBLE
    }

    override fun onSessionLeave() {}

    override fun onSessionLeave(reason: ZoomVideoSDKSessionLeaveReason?) {
        val intent = Intent(this, JoinActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onError(errorCode: Int) {}
    override fun onUserJoin(userHelper: ZoomVideoSDKUserHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onUserLeave(userHelper: ZoomVideoSDKUserHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onUserVideoStatusChanged(videoHelper: ZoomVideoSDKVideoHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onShareNetworkStatusChanged(shareNetworkStatus: ZoomVideoSDKNetworkStatus?, isSendingShare: Boolean) {}
    override fun onUserAudioStatusChanged(audioHelper: ZoomVideoSDKAudioHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onUserShareStatusChanged(shareHelper: ZoomVideoSDKShareHelper?, userInfo: ZoomVideoSDKUser?, status: ZoomVideoSDKShareStatus?) {}
    override fun onUserShareStatusChanged(shareHelper: ZoomVideoSDKShareHelper?, userInfo: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onShareContentChanged(shareHelper: ZoomVideoSDKShareHelper?, userInfo: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onLiveStreamStatusChanged(liveStreamHelper: ZoomVideoSDKLiveStreamHelper?, status: ZoomVideoSDKLiveStreamStatus?) {}
    override fun onChatNewMessageNotify(chatHelper: ZoomVideoSDKChatHelper?, messageItem: ZoomVideoSDKChatMessage?) {}
    override fun onChatDeleteMessageNotify(chatHelper: ZoomVideoSDKChatHelper?, msgID: String?, deleteBy: ZoomVideoSDKChatMessageDeleteType?) {}
    override fun onChatPrivilegeChanged(chatHelper: ZoomVideoSDKChatHelper?, currentPrivilege: ZoomVideoSDKChatPrivilegeType?) {}
    override fun onUserHostChanged(userHelper: ZoomVideoSDKUserHelper?, userInfo: ZoomVideoSDKUser?) {}
    override fun onUserManagerChanged(user: ZoomVideoSDKUser?) {}
    override fun onUserNameChanged(user: ZoomVideoSDKUser?) {}
    override fun onUserFailoverStatusChanged(user: ZoomVideoSDKUser?, isInFailover: Boolean) {}
    override fun onUserActiveAudioChanged(audioHelper: ZoomVideoSDKAudioHelper?, list: List<ZoomVideoSDKUser?>?) {}
    override fun onSessionNeedPassword(handler: ZoomVideoSDKPasswordHandler?) {}
    override fun onSessionPasswordWrong(handler: ZoomVideoSDKPasswordHandler?) {}
    override fun onMixedAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {}
    override fun onOneWayAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?, user: ZoomVideoSDKUser?) {}
    override fun onShareAudioRawDataReceived(rawData: ZoomVideoSDKAudioRawData?) {}
    override fun onCommandReceived(sender: ZoomVideoSDKUser?, strCmd: String?) {}
    override fun onCommandChannelConnectResult(isSuccess: Boolean) {}
    override fun onCloudRecordingStatus(status: ZoomVideoSDKRecordingStatus?, handler: ZoomVideoSDKRecordingConsentHandler?) {}
    override fun onHostAskUnmute() {}
    override fun onInviteByPhoneStatus(status: ZoomVideoSDKPhoneStatus?, reason: ZoomVideoSDKPhoneFailedReason?) {}
    override fun onMultiCameraStreamStatusChanged(status: ZoomVideoSDKMultiCameraStreamStatus?, user: ZoomVideoSDKUser?, videoPipe: ZoomVideoSDKRawDataPipe?) {}
    override fun onMultiCameraStreamStatusChanged(status: ZoomVideoSDKMultiCameraStreamStatus?, user: ZoomVideoSDKUser?, canvas: ZoomVideoSDKVideoCanvas?) {}
    override fun onLiveTranscriptionStatus(status: ZoomVideoSDKLiveTranscriptionHelper.ZoomVideoSDKLiveTranscriptionStatus?) {}
    override fun onOriginalLanguageMsgReceived(messageInfo: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo?) {}
    override fun onLiveTranscriptionMsgInfoReceived(messageInfo: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionMessageInfo?) {}
    override fun onLiveTranscriptionMsgError(spokenLanguage: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage?, transcriptLanguage: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage?) {}
    override fun onSpokenLanguageChanged(spokenLanguage: ZoomVideoSDKLiveTranscriptionHelper.ILiveTranscriptionLanguage?) {}
    override fun onVoiceInterpretationReady() {}
    override fun onProxySettingNotification(handler: ZoomVideoSDKProxySettingHandler?) {}
    override fun onSSLCertVerifiedFailNotification(info: ZoomVideoSDKSSLCertificateInfo?) {}
    override fun onCameraControlRequestResult(user: ZoomVideoSDKUser?, isApproved: Boolean) {}
    override fun onCameraControlRequestReceived(user: ZoomVideoSDKUser?, requestType: ZoomVideoSDKCameraControlRequestType?, requestHandler: ZoomVideoSDKCameraControlRequestHandler?) {}
    override fun onUserVideoNetworkStatusChanged(status: ZoomVideoSDKNetworkStatus?, user: ZoomVideoSDKUser?) {}
    override fun onUserRecordingConsent(user: ZoomVideoSDKUser?) {}
    override fun onCallCRCDeviceStatusChanged(status: ZoomVideoSDKCRCCallStatus?) {}
    override fun onVideoCanvasSubscribeFail(fail_reason: ZoomVideoSDKVideoSubscribeFailReason?, pUser: ZoomVideoSDKUser?, view: ZoomVideoSDKVideoView?) {}
    override fun onShareCanvasSubscribeFail(fail_reason: ZoomVideoSDKVideoSubscribeFailReason?, pUser: ZoomVideoSDKUser?, view: ZoomVideoSDKVideoView?) {}
    override fun onShareCanvasSubscribeFail(pUser: ZoomVideoSDKUser?, view: ZoomVideoSDKVideoView?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onAnnotationHelperCleanUp(helper: ZoomVideoSDKAnnotationHelper?) {}
    override fun onAnnotationPrivilegeChange(shareOwner: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onAnnotationToolTypeChanged(helper: ZoomVideoSDKAnnotationHelper?, view: ZoomVideoSDKVideoView?, toolType: ZoomVideoSDKAnnotationToolType?) {}
    override fun onTestMicStatusChanged(status: ZoomVideoSDKTestMicStatus?) {}
    override fun onMicSpeakerVolumeChanged(micVolume: Int, speakerVolume: Int) {}
    override fun onCalloutJoinSuccess(user: ZoomVideoSDKUser?, phoneNumber: String?) {}
    override fun onSendFileStatus(file: ZoomVideoSDKSendFile?, status: ZoomVideoSDKFileTransferStatus?) {}
    override fun onReceiveFileStatus(file: ZoomVideoSDKReceiveFile?, status: ZoomVideoSDKFileTransferStatus?) {}
    override fun onUVCCameraStatusChange(cameraId: String?, status: UVCCameraStatus?) {}
    override fun onVideoAlphaChannelStatusChanged(isAlphaModeOn: Boolean) {}
    override fun onSpotlightVideoChanged(videoHelper: ZoomVideoSDKVideoHelper?, userList: List<ZoomVideoSDKUser?>?) {}
    override fun onFailedToStartShare(shareHelper: ZoomVideoSDKShareHelper?, user: ZoomVideoSDKUser?) {}
    override fun onBindIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onUnbindIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onIncomingLiveStreamStatusResponse(bSuccess: Boolean, streamsStatusList: List<IncomingLiveStreamStatus?>?) {}
    override fun onStartIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onStopIncomingLiveStreamResponse(bSuccess: Boolean, streamKeyID: String?) {}
    override fun onShareContentSizeChanged(shareHelper: ZoomVideoSDKShareHelper?, user: ZoomVideoSDKUser?, shareAction: ZoomVideoSDKShareAction?) {}
    override fun onSubSessionStatusChanged(status: ZoomVideoSDKSubSessionStatus?, subSessionKitList: List<SubSessionKit?>?) {}
    override fun onSubSessionManagerHandle(manager: ZoomVideoSDKSubSessionManager?) {}
    override fun onSubSessionParticipantHandle(participant: ZoomVideoSDKSubSessionParticipant?) {}
    override fun onSubSessionUsersUpdate(subSessionKit: SubSessionKit?) {}
    override fun onBroadcastMessageFromMainSession(message: String?, userName: String?) {}
    override fun onSubSessionUserHelpRequest(handler: SubSessionUserHelpRequestHandler?) {}
    override fun onSubSessionUserHelpRequestResult(eResult: ZoomVideoSDKUserHelpRequestResult?) {}
    override fun onShareSettingChanged(setting: ZoomVideoSDKShareSetting?) {}
    override fun onStartBroadcastResponse(bSuccess: Boolean, channelID: String?) {}
    override fun onStopBroadcastResponse(bSuccess: Boolean) {}
    override fun onGetBroadcastControlStatus(bSuccess: Boolean, status: ZoomVideoSDKBroadcastControlStatus?) {}
    override fun onStreamingJoinStatusChanged(status: ZoomVideoSDKStreamingJoinStatus?) {}
    override fun onEmojiReactionReceived(user: ZoomVideoSDKUser?, type: ZoomVideoSDKEmojiReactionType?) {}
    override fun onUserWhiteboardShareStatusChanged(user: ZoomVideoSDKUser?, helper: ZoomVideoSDKWhiteboardHelper?) {}
    override fun onWhiteboardExported(format: ZoomVideoSDKExportFormat?, data: ByteArray?) {}
    override fun onCanvasSnapshotTaken(user: ZoomVideoSDKUser?, isShare: Boolean) {}
    override fun onCanvasSnapshotIncompatible(user: ZoomVideoSDKUser?) {}
    override fun onQOSStatisticsReceived(statistics: ZoomVideoSDKQOSStatistics?, user: ZoomVideoSDKUser?) {}
    override fun onMyAudioSourceTypeChanged(device: ZoomVideoSDKAudioHelper.ZoomVideoSDKAudioDevice?) {}
    override fun onUserNetworkStatusChanged(type: ZoomVideoSDKDataType?, level: ZoomVideoSDKNetworkStatus?, user: ZoomVideoSDKUser?) {}
    override fun onUserOverallNetworkStatusChanged(level: ZoomVideoSDKNetworkStatus?, user: ZoomVideoSDKUser?) {}
    override fun onAudioLevelChanged(level: Int, audioSharing: Boolean, user: ZoomVideoSDKUser?) {}
    override fun onRealTimeMediaStreamsStatus(status: RealTimeMediaStreamsStatus?) {}
    override fun onRealTimeMediaStreamsFail(failReason: RealTimeMediaStreamsFailReason?) {}

    // endregion

    override fun onDestroy() {
        super.onDestroy()
        ZoomVideoSDK.getInstance().removeListener(this)
    }
}
