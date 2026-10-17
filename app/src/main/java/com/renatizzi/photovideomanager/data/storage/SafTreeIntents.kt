package com.renatizzi.photovideomanager.data.storage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract

/**
 * OpenDocumentTree with advanced roots (SD / USB / network providers such as CIFS),
 * matching BoxManager StorageFolderPicker behaviour.
 */
object SafTreeIntents {
    fun createOpenTreeIntent(initialUri: Uri? = null): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION,
            )
            putExtra("android.provider.extra.SHOW_ADVANCED", true)
            putExtra("android.content.extra.SHOW_ADVANCED", true)
            if (initialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }
    }
}

class OpenDocumentTreeAdvanced : ActivityResultContract<Uri?, Uri?>() {
    override fun createIntent(context: Context, input: Uri?): Intent =
        SafTreeIntents.createOpenTreeIntent(input)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) intent?.data else null
    }
}
