import 'package:flutter/material.dart';
import 'screens/admin_screen.dart';
import 'screens/lock_screen.dart';

void main() {
  runApp(AdminLockApp());
}

class AdminLockApp extends StatelessWidget {
  const AdminLockApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Admin Lock App',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: AdminScreen(),
      routes: {
        '/admin': (context) => AdminScreen(),
        '/lock': (context) => LockScreen(),
      },
    );
  }
}

