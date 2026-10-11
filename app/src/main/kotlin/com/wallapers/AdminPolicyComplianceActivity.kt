package com.wallapers

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle

/** Applies only the visible profile name and confirms managed-profile setup to Android. */
class AdminPolicyComplianceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val admin = ComponentName(this, WallapersDeviceAdminReceiver::class.java)
        val policyManager = getSystemService(DevicePolicyManager::class.java)

        try {
            if (!policyManager.isProfileOwnerApp(packageName)) {
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            policyManager.setProfileName(admin, "Wallapers — отдельный профиль")
            policyManager.setProfileEnabled(admin)
            setResult(RESULT_OK)
        } catch (_: SecurityException) {
            setResult(RESULT_CANCELED)
        }
        finish()
    }
}
