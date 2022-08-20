package app.utils

import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.Intent
import android.app.Activity
import android.content.Context
import android.net.Uri

class SAFFileResultContract : ActivityResultContract<String?, Uri?>() {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun createIntent(context: Context, input: String?): Intent {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
        i.type = "*/*"
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return i
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            intent.data
        } else null
    }
}