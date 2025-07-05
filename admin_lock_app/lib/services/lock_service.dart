import 'package:flutter/services.dart';

class LockService {
  static const MethodChannel _channel = MethodChannel('admin_lock_app/lock');

  Future<void> startLock(int durationMinutes, String password) async {
    if (durationMinutes <= 0) {
      throw Exception('Duration must be greater than 0');
    }
    if (password.isEmpty) {
      throw Exception('Password cannot be empty');
    }
    
    try {
      await _channel.invokeMethod('startLock', {
        'duration': durationMinutes,
        'password': password,
      });
    } on PlatformException catch (e) {
      throw Exception('Failed to start lock: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error starting lock: $e');
    }
  }

  Future<void> enableKioskMode() async {
    try {
      await _channel.invokeMethod('enableKioskMode');
    } on PlatformException catch (e) {
      throw Exception('Failed to enable kiosk mode: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error enabling kiosk mode: $e');
    }
  }

  Future<void> disableKioskMode() async {
    try {
      await _channel.invokeMethod('disableKioskMode');
    } on PlatformException catch (e) {
      throw Exception('Failed to disable kiosk mode: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error disabling kiosk mode: $e');
    }
  }

  Future<bool> isDeviceAdmin() async {
    try {
      final result = await _channel.invokeMethod('isDeviceAdmin');
      return result as bool? ?? false;
    } on PlatformException catch (e) {
      throw Exception('Failed to check device admin status: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error checking device admin status: $e');
    }
  }

  Future<void> requestDeviceAdmin() async {
    try {
      await _channel.invokeMethod('requestDeviceAdmin');
    } on PlatformException catch (e) {
      throw Exception('Failed to request device admin: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error requesting device admin: $e');
    }
  }

  Future<void> lockDevice() async {
    try {
      await _channel.invokeMethod('lockDevice');
    } on PlatformException catch (e) {
      throw Exception('Failed to lock device: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error locking device: $e');
    }
  }

  Future<void> startForegroundService() async {
    try {
      await _channel.invokeMethod('startForegroundService');
    } on PlatformException catch (e) {
      throw Exception('Failed to start foreground service: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error starting foreground service: $e');
    }
  }

  Future<void> stopForegroundService() async {
    try {
      await _channel.invokeMethod('stopForegroundService');
    } on PlatformException catch (e) {
      throw Exception('Failed to stop foreground service: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error stopping foreground service: $e');
    }
  }

  Future<void> enableScreenPinning() async {
    try {
      await _channel.invokeMethod('enableScreenPinning');
    } on PlatformException catch (e) {
      throw Exception('Failed to enable screen pinning: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error enabling screen pinning: $e');
    }
  }

  Future<void> disableScreenPinning() async {
    try {
      await _channel.invokeMethod('disableScreenPinning');
    } on PlatformException catch (e) {
      throw Exception('Failed to disable screen pinning: ${e.message ?? e.code}');
    } catch (e) {
      throw Exception('Unexpected error disabling screen pinning: $e');
    }
  }

  // Additional utility methods
  Future<bool> checkDeviceAdminAndRequest() async {
    try {
      bool isAdmin = await isDeviceAdmin();
      if (!isAdmin) {
        await requestDeviceAdmin();
        // Wait a bit and check again
        await Future.delayed(Duration(seconds: 1));
        isAdmin = await isDeviceAdmin();
      }
      return isAdmin;
    } catch (e) {
      throw Exception('Failed to ensure device admin privileges: $e');
    }
  }

  Future<void> initializeLockSystem() async {
    try {
      // Ensure device admin is enabled
      bool isAdmin = await checkDeviceAdminAndRequest();
      if (!isAdmin) {
        throw Exception('Device admin privileges are required for this app to function');
      }
      
      // Start foreground service
      await startForegroundService();
    } catch (e) {
      throw Exception('Failed to initialize lock system: $e');
    }
  }
}

