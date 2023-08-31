
import 'flutter_secure_keystore_platform_interface.dart';

class FlutterSecureKeystore {
  Future<String?> getPlatformVersion() {
    return FlutterSecureKeystorePlatform.instance.getPlatformVersion();
  }

  Future<void> createKey(String alias) async {
    return await FlutterSecureKeystorePlatform.instance.createKey(alias);
  }

  Future<String> encrypt(String alias, String data) async {
    return await FlutterSecureKeystorePlatform.instance.encrypt(alias, data);
  }

  Future<String> decrypt(String alias, String encryptedData) async {
    return await FlutterSecureKeystorePlatform.instance.decrypt(alias, encryptedData);
  }
}
