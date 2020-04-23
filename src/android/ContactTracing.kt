package org.tcncoalition.contacttracing

import java.util.*
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.*
import android.Manifest.permission.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.util.Base64
import org.apache.cordova.*
import org.json.*
import org.tcncoalition.tcnclient.*
import org.tcncoalition.tcnclient.cen.*

class ContactTracing : CordovaPlugin() {

  private var inMemoryCache: MutableList<JSONObject>? = mutableListOf()
  private var ble: BluetoothService? = null
  private var timer: Timer? = null
  private val intent get() = Intent( cordova.activity.applicationContext, BluetoothService::class.java )
  private val cenGenerator = DefaultCenGenerator()
  private val cenVisitor = DefaultCenVisitor()

  private val serviceConnection: ServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      ble = (service as BluetoothService.LocalBinder).service.apply {
        configure( BluetoothService.ServiceConfiguration( cenGenerator, cenVisitor, foregroundNotification() ) )
        start()
      }
      timer?.cancel()
      timer = Timer()
      timer?.scheduleAtFixedRate( object : TimerTask() { override fun run() { ble?.updateCen() } }, 15 * 60000, 15 * 60000 )
    }
    override fun onServiceDisconnected(name: ComponentName?) = Unit
  }

  private fun foregroundNotification(): Notification {
    val app = cordova.activity.applicationContext
    val notify = Intent( app, cordova.activity::class.java )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val serviceChannel = NotificationChannel( "CTPlugin", "Contact Tracing",  NotificationManager.IMPORTANCE_DEFAULT )
      ContextCompat.getSystemService( app, NotificationManager::class.java )?.createNotificationChannel( serviceChannel )
    }
    return NotificationCompat.Builder( app, "CTPlugin" )
      .setContentTitle( app.applicationInfo.name + " is logging" ).setSmallIcon( app.applicationInfo.icon )
      .setContentIntent( PendingIntent.getActivity( app, 0, notify, PendingIntent.FLAG_UPDATE_CURRENT ) )
      .setCategory( Notification.CATEGORY_SERVICE ).build()
  }

  @Throws(JSONException::class)
  override fun execute(action: String, args: JSONArray, context: CallbackContext): Boolean {
    if ( !BluetoothAdapter.getDefaultAdapter().isEnabled ) {
      cordova.startActivityForResult( this, Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ), 1 )
    }
    if ( !PermissionHelper.hasPermission( this, ACCESS_COARSE_LOCATION ) ) {
      PermissionHelper.requestPermissions( this, 2, arrayOf( ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION ) )
    }
    try {
      when ( action ) {
        "start" -> {
          cordova.activity.applicationContext.bindService( intent, serviceConnection, Context.BIND_AUTO_CREATE )
          cordova.activity.applicationContext.startService( intent )
          context.success()
        }
        "stop" -> {
          cordova.activity.applicationContext.stopService( intent )
          context.success()
        }
        "getCENs" -> {
          context.success( JSONArray( inMemoryCache ) )
        }
        else -> throw Exception( "Invalid Action" )
      }
      return true
    } catch (e: Exception) {
      context.error( e.toString() )
      return false
    }
  }

  inner class DefaultCenVisitor : CenVisitor {
    private fun handleCen(data: ByteArray, type: String) {
      val contactEvent = JSONObject()
      contactEvent.put( "number", Base64.encodeToString( data, Base64.DEFAULT ) )
      contactEvent.put( "ts", System.currentTimeMillis() )
      contactEvent.put( "type", type )
      inMemoryCache?.add(contactEvent)
    }
    override fun visit(cen: GeneratedCen) { handleCen( cen.data, "advertise" ) }
    override fun visit(cen: ObservedCen) { handleCen( cen.data, "scan" ) }
  }

  inner class DefaultCenGenerator : CenGenerator {
    override fun generate(): GeneratedCen {
      return GeneratedCen( UUID.randomUUID().toBytes() )
    }
  }

}
