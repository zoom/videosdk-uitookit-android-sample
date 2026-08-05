package us.zoom.uitoolkitsample

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Helper for Zoom Video SDK JWT tokens.
 *
 * **Production:** Generate the JWT on your backend and fetch it from the app (see [fetchTokenFromServer]).
 * **Development only:** Use [generateZoomVideoSdkToken] with your SDK secret. Never ship the secret in release builds.
 */
object JwtTokenHelper {

    private const val TAG = "JwtTokenHelper"

    /**
     * Generate a Zoom Video SDK JWT on device. **Development / testing only.**
     * Do not embed your SDK secret in production; use a backend to issue tokens.
     *
     * @param sdkKey Zoom Video SDK key (from Zoom Marketplace)
     * @param sdkSecret Zoom Video SDK secret (keep out of version control; use BuildConfig or local config)
     * @param sessionName Topic / session name (e.g. "monument-valley-770")
     * @param sessionKey Optional session key (leave empty if not used)
     * @param userIdentity User identifier (e.g. "13579" or display name)
     * @param roleType 1 = host, 0 = participant
     * @param validSeconds Token lifetime in seconds (e.g. 86400 = 24 hours)
     */
    @JvmStatic
    fun generateZoomVideoSdkToken(
        sdkKey: String,
        sdkSecret: String,
        sessionName: String,
        sessionKey: String = "",
        userIdentity: String = "123456",
        roleType: Int = 1,
        validSeconds: Int = 86400
    ): String {
        val now = System.currentTimeMillis() / 1000
        val payload = JSONObject().apply {
            put("app_key", sdkKey)
            put("version", 1)
            put("iat", now)
            put("exp", now + validSeconds)
            put("tpc", sessionName)
            put("session_key", sessionKey)
            put("role_type", roleType)
            put("user_identity", userIdentity)
        }
        return signJwtHs256(payload.toString(), sdkSecret)
    }

    /**
     * Sign a JWT with HS256 (HMAC-SHA256). Header is standard: {"alg":"HS256","typ":"JWT"}.
     */
    private fun signJwtHs256(payloadJson: String, secret: String): String {
        val header = """{"alg":"HS256","typ":"JWT"}"""
        val headerB64 = base64UrlEncode(header.toByteArray(Charsets.UTF_8))
        val payloadB64 = base64UrlEncode(payloadJson.toByteArray(Charsets.UTF_8))
        val signingInput = "$headerB64.$payloadB64"
        val signature = hmacSha256(signingInput.toByteArray(Charsets.UTF_8), secret.toByteArray(Charsets.UTF_8))
        val signatureB64 = base64UrlEncode(signature)
        return "$signingInput.$signatureB64"
    }

    private fun base64UrlEncode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }
}
