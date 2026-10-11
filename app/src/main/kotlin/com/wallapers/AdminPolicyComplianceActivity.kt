package com.wallapers

import android.app.Activity
import android.os.Bundle

/** Applies the visible profile name and narrowly allows clone requests from the parent profile. */
class AdminPolicyComplianceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setResult(if (configureManagedProfile(this)) RESULT_OK else RESULT_CANCELED)
        } catch (_: SecurityException) {
            setResult(RESULT_CANCELED)
        }
        finish()
    }
}
