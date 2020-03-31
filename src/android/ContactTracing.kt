package org.covidwatch.libcontacttracing

import java.util.*
import org.apache.cordova.*
import org.json.*


class ContactTracing : CordovaPlugin() {

    @Throws(JSONException::class)
    override fun execute(action: String, args: JSONArray, context: CallbackContext): Boolean {

        try {
            when ( action ) {
                "startScanner" -> {

                }
                "stopScanner" -> {

                }
                "startAdvertiser" -> {

                }
                "stopAdvertiser" -> {

                }
                "updateCEN" -> {

                }
                else -> throw Exception( "Invalid Action" )
                context.success()
            }
            return true
        } catch (e: Exception) {
            context.error( e.toString() )
            return false
        }

    }

}