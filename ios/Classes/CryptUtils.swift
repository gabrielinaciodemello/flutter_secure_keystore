import Security
import LocalAuthentication

public class CryptUtils {
    
    private let service: String
    
    init(service: String = "secure_keystore") {
        self.service = service
    }
    
    func save(key: String, data: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data.data(using: .utf8)!,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly
        ]
        
        SecItemDelete(query as CFDictionary)
        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }
    
    func get(key: String, completion: @escaping (String?) -> Void) {
        let context = LAContext()
        var error: NSError?
        
        if context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error) {
            context.evaluatePolicy(.deviceOwnerAuthentication, localizedReason: "To access secure data") { (success, error) in
                if success {
                    let query: [String: Any] = [
                        kSecClass as String: kSecClassGenericPassword,
                        kSecAttrService as String: self.service,
                        kSecAttrAccount as String: key,
                        kSecReturnData as String: kCFBooleanTrue!,
                        kSecMatchLimit as String: kSecMatchLimitOne
                    ]
                    
                    var dataTypeRef: AnyObject?
                    let status = SecItemCopyMatching(query as CFDictionary, &dataTypeRef)
                    
                    if status == errSecSuccess, let data = dataTypeRef as? Data, let result = String(data: data, encoding: .utf8) {
                        completion(result)
                    } else {
                        completion(nil)
                    }
                } else {
                    completion(nil)
                }
            }
        } else {
            completion(nil)
        }
    }
}
