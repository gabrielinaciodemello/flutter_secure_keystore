
import 'flutter_secure_keystore_platform_interface.dart';

class FlutterSecureKeystore {
  Future<String?> getPlatformVersion() {
    return FlutterSecureKeystorePlatform.instance.getPlatformVersion();
  }

  Future<void> save(String key, String value) async {
    return await FlutterSecureKeystorePlatform.instance.save(key, value);
  }

  Future<String> get(String key) async {
    return await FlutterSecureKeystorePlatform.instance.get(key);
  }
}
