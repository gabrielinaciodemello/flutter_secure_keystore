
import 'flutter_secure_keystore_platform_interface.dart';

class FlutterSecureKeystore {
  Future<String?> getPlatformVersion() {
    return FlutterSecureKeystorePlatform.instance.getPlatformVersion();
  }
}
