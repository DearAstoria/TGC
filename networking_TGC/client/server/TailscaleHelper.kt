package vpn

import android.content.Context
import android.widget.Toast

fun openTailscaleApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage("com.tailscale.ipn")
    if (intent != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Tailscale app not installed", Toast.LENGTH_LONG).show()
    }
}
