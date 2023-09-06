import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_secure_keystore_method_channel.dart';

abstract class FlutterSecureKeystorePlatform extends PlatformInterface {
  /// Constructs a FlutterSecureKeystorePlatform.
  FlutterSecureKeystorePlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterSecureKeystorePlatform _instance = MethodChannelFlutterSecureKeystore();

  /// The default instance of [FlutterSecureKeystorePlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterSecureKeystore].
  static FlutterSecureKeystorePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterSecureKeystorePlatform] when
  /// they register themselves.
  static set instance(FlutterSecureKeystorePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> save(String key, String value) async {
    throw UnimplementedError('save() has not been implemented.');
  }

  Future<String> get(String key) async {
    throw UnimplementedError('get() has not been implemented.');
  }

  Future<bool?> delete(String key) async {
    throw UnimplementedError('delete() has not been implemented.');
  }
}
