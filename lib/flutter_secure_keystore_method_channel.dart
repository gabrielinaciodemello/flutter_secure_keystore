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
  Future<void> save(String key, String value) async {
    await methodChannel.invokeMethod('save', { 'key': key, 'value': value });
  }

  @override
  Future<String> get(String key) async {
    return await methodChannel.invokeMethod('get', {'key': key});
  }

  @override
  Future<bool?> delete(String key) async {
    return await methodChannel.invokeMethod('delete', {'key': key});
  }

}
