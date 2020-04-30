import TCNClient

@objc(ContactTracing)
class ContactTracing : CDVPlugin {

    var contactTracingBluetoothService : TCNBluetoothService?
    var inMemoryCache : [ [ AnyHashable : Any ] ]?
    var discoveredList : [ Data ]?

    @objc(start:)
    func start( _ command : CDVInvokedUrlCommand ) {
        self.configureContactTracingService( callbackId : command.callbackId )
        self.contactTracingBluetoothService?.start()
        self.commandDelegate.send(
            CDVPluginResult( status : CDVCommandStatus_OK ),
            callbackId : command.callbackId )
    }
    
    @objc(stop:)
    func stop( _ command : CDVInvokedUrlCommand ) {
        self.contactTracingBluetoothService?.stop()
        self.commandDelegate.send(
            CDVPluginResult( status : CDVCommandStatus_OK ),
            callbackId : command.callbackId )
    }
    
    @objc(getCENs:)
    func getCENs( _ command : CDVInvokedUrlCommand ) {
        self.commandDelegate.send(
            CDVPluginResult( status : CDVCommandStatus_OK, messageAs : self.inMemoryCache ?? [] ),
            callbackId : command.callbackId )
    }
 
    func configureContactTracingService( callbackId : String ) {
        guard contactTracingBluetoothService == nil else { return }
        self.inMemoryCache = []
        contactTracingBluetoothService = TCNBluetoothService(
            tcnGenerator: { () -> Data in
                NSLog("Bluetooth service asked to generate a temporary contact number to share it")
                let data = withUnsafeBytes(of: UUID().uuid, { Data($0) })
                self.inMemoryCache?.append( [
                    "number" : data.base64EncodedString(),
                    "ts" : Date().timeIntervalSince1970 * 1000, // * 1000 to convert sec to ms
                    "type" : "advertise"
                ] )
                self.discoveredList = []
                return data
            }, tcnFinder: { ( data, estimatedDistance ) in
                guard let dl = self.discoveredList, !dl.contains( data ) else { return }
                NSLog("Bluetooth service found a temporary contact number from a nearby device" )
                self.inMemoryCache?.append( [
                    "number" : data.base64EncodedString(),
                    "ts" : Date().timeIntervalSince1970 * 1000, // * 1000 to convert sec to ms
                    "type" : "scan"
                ] )
                self.discoveredList?.append( data )
            }, errorHandler: { ( error ) in
                self.commandDelegate.send(
                    CDVPluginResult(
                        status : CDVCommandStatus_ERROR,
                        messageAs : "ContactTracing service failed. Check BLE & Permissions settings." ),
                    callbackId : callbackId )
            }
        )
    }
        
}
