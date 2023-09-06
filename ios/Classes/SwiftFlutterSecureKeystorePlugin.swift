import Flutter
import UIKit 

public class SwiftFlutterSecureKeystorePlugin: NSObject, FlutterPlugin {

  let cryptUtils = CryptUtils()

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_secure_keystore", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterSecureKeystorePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
      case "getPlatformVersion":
        result("iOS " + UIDevice.current.systemVersion)
      case "save":
        if let args = call.arguments as? [String: Any],
          let key = args["key"] as? String,
          let value = args["value"] as? String {
            let saveResult = cryptUtils.save(key: key, data: value)
            result(saveResult)
        } 
        else {
          result(
            FlutterError(
              code: "INVALID_INPUT", 
              message: "Key or Value was not provided.", 
              details: nil
            )
          )
        }
      case "get":
        if let args = call.arguments as? [String: Any],
           let key = args["key"] as? String {
            cryptUtils.get(key: key){ data in
                result(data)
            }
        } 
        else {
          result(
            FlutterError(
              code: "INVALID_INPUT", 
              message: "Key was not provided.", 
              details: nil
            )
          )
        }
      case "delete":
        if let args = call.arguments as? [String: Any],
           let key = args["key"] as? String {
           let res = cryptUtils.delete(key: key)
           result(res)
        } 
        else {
          result(
            FlutterError(
              code: "INVALID_INPUT", 
              message: "Key was not provided.", 
              details: nil
            )
          )
        }
      default:
        result(FlutterMethodNotImplemented)
    }
  }

}
