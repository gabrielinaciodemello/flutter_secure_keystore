import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_secure_keystore/flutter_secure_keystore.dart';
import 'package:flutter_secure_keystore/flutter_secure_keystore_platform_interface.dart';
import 'package:flutter_secure_keystore/flutter_secure_keystore_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterSecureKeystorePlatform
    with MockPlatformInterfaceMixin
    implements FlutterSecureKeystorePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
  
  @override
  Future<String> get(String key) {
    throw UnimplementedError();
  }
  
  @override
  Future<void> save(String key, String value) {
    throw UnimplementedError();
  }
  
  @override
  Future<bool?> delete(String key) {
    throw UnimplementedError();
  }
}

void main() {
  final FlutterSecureKeystorePlatform initialPlatform = FlutterSecureKeystorePlatform.instance;

  test('$MethodChannelFlutterSecureKeystore is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterSecureKeystore>());
  });

  test('getPlatformVersion', () async {
    FlutterSecureKeystore flutterSecureKeystorePlugin = FlutterSecureKeystore();
    MockFlutterSecureKeystorePlatform fakePlatform = MockFlutterSecureKeystorePlatform();
    FlutterSecureKeystorePlatform.instance = fakePlatform;

    expect(await flutterSecureKeystorePlugin.getPlatformVersion(), '42');
  });
}
