# flutter_secure_keystore

A secure keystore protected by biometric authentication.
For Android, the library uses the native Keystore to save a key to encrypt the data that will be saved using sharedPreferences.
For IOS, the library uses the native Keychain to save data

## Getting Started

Add this library to your pubspec.yaml file
```
dependencies:
  flutter_secure_keystore:
    git:
      url: https://github.com/gabrielinaciodemello/flutter_secure_keystore.git
      ref: 1.0.0
```


Use it 
```
import 'package:flutter_secure_keystore/flutter_secure_keystore.dart';

class UtSecureStorage {

  var store = FlutterSecureKeystore();

  Future<String?> read({ required String key }) async {
    return await store.get(key);
  }

  Future<void> write({required String key, required String value }) async {
    return await store.save(key, value);    
  }

  Future<bool?> delete({required String key}) async {
    return await store.delete(key);
  }
}
```

