import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_secure_keystore_platform_interface.dart';

/// An implementation of [FlutterSecureKeystorePlatform] that uses method channels.
class MethodChannelFlutterSecureKeystore extends FlutterSecureKeystorePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_secure_keystore');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> createKey(String alias) async {
    await methodChannel.invokeMethod('createKey', {'alias': alias});
  }

  @override
  Future<String> encrypt(String alias, String data) async {
    return await methodChannel.invokeMethod('encrypt', {'alias': alias, 'data': data});
  }

  @override
  Future<String> decrypt(String alias, String encryptedData) async {
    return await methodChannel.invokeMethod('decrypt', {'alias': alias, 'data': encryptedData});
  }
}
