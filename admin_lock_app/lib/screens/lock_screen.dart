import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';
import '../services/lock_service.dart';

class LockScreen extends StatefulWidget {
  const LockScreen({super.key});

  @override
  State<LockScreen> createState() => _LockScreenState();
}

class _LockScreenState extends State<LockScreen> with WidgetsBindingObserver {
  final TextEditingController _passwordController = TextEditingController();
  final LockService _lockService = LockService();
  
  Timer? _timer;
  int _remainingSeconds = 0;
  String _adminPassword = '';
  bool _showPasswordField = false;
  String _enteredPassword = '';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initializeLock();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // Prevent app from going to background
    if (state != AppLifecycleState.resumed) {
      SystemNavigator.pop();
    }
  }

  void _initializeLock() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final args = ModalRoute.of(context)?.settings.arguments as Map<String, dynamic>?;
      if (args != null) {
        final duration = args['duration'] as int;
        _adminPassword = args['password'] as String;
        _remainingSeconds = duration * 60;
        _startTimer();
        _lockService.enableKioskMode();
      }
    });
  }

  void _startTimer() {
    _timer = Timer.periodic(Duration(seconds: 1), (timer) {
      setState(() {
        if (_remainingSeconds > 0) {
          _remainingSeconds--;
        } else {
          _unlockDevice();
        }
      });
    });
  }

  void _unlockDevice() {
    _timer?.cancel();
    _lockService.disableKioskMode();
    Navigator.pushReplacementNamed(context, '/admin');
  }

  String _formatTime(int seconds) {
    final hours = seconds ~/ 3600;
    final minutes = (seconds % 3600) ~/ 60;
    final secs = seconds % 60;
    
    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
    } else {
      return '${minutes.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
    }
  }

  void _onNumberPressed(String number) {
    if (_enteredPassword.length < 10) {
      setState(() {
        _enteredPassword += number;
      });
    }
  }

  void _onBackspacePressed() {
    if (_enteredPassword.isNotEmpty) {
      setState(() {
        _enteredPassword = _enteredPassword.substring(0, _enteredPassword.length - 1);
      });
    }
  }

  void _onUnlockPressed() {
    if (_enteredPassword == _adminPassword) {
      _unlockDevice();
    } else {
      setState(() {
        _enteredPassword = '';
      });
      _showErrorMessage('Incorrect password');
    }
  }

  void _showErrorMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
        duration: Duration(seconds: 2),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false, // Prevent back navigation
      child: Scaffold(
        backgroundColor: Colors.black,
        body: Container(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [Colors.grey[900]!, Colors.black],
            ),
          ),
          child: SafeArea(
            child: Column(
              children: [
                // Header with lock icon and title
                Expanded(
                  flex: 2,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.lock,
                        size: 80,
                        color: Colors.red[400],
                      ),
                      SizedBox(height: 20),
                      Text(
                        'DEVICE LOCKED',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                          letterSpacing: 2,
                        ),
                      ),
                      SizedBox(height: 10),
                      Text(
                        'Access Restricted',
                        style: TextStyle(
                          color: Colors.grey[400],
                          fontSize: 16,
                        ),
                      ),
                    ],
                  ),
                ),
                
                // Timer display
                Expanded(
                  flex: 2,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'Time Remaining',
                        style: TextStyle(
                          color: Colors.grey[400],
                          fontSize: 16,
                        ),
                      ),
                      SizedBox(height: 10),
                      Container(
                        padding: EdgeInsets.symmetric(horizontal: 30, vertical: 15),
                        decoration: BoxDecoration(
                          border: Border.all(color: Colors.red[400]!, width: 2),
                          borderRadius: BorderRadius.circular(15),
                        ),
                        child: Text(
                          _formatTime(_remainingSeconds),
                          style: TextStyle(
                            color: Colors.red[400],
                            fontSize: 48,
                            fontWeight: FontWeight.bold,
                            fontFamily: 'monospace',
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                
                // Admin unlock section
                Expanded(
                  flex: 3,
                  child: Column(
                    children: [
                      if (!_showPasswordField) ...[
                        ElevatedButton(
                          onPressed: () {
                            setState(() {
                              _showPasswordField = true;
                            });
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.grey[800],
                            foregroundColor: Colors.white,
                            padding: EdgeInsets.symmetric(horizontal: 30, vertical: 12),
                          ),
                          child: Text('Admin Unlock'),
                        ),
                      ] else ...[
                        Text(
                          'Enter Admin Password',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        SizedBox(height: 20),
                        
                        // Password display
                        Container(
                          margin: EdgeInsets.symmetric(horizontal: 40),
                          padding: EdgeInsets.symmetric(vertical: 15),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey[600]!),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: List.generate(
                              10,
                              (index) => Container(
                                margin: EdgeInsets.symmetric(horizontal: 5),
                                width: 15,
                                height: 15,
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: index < _enteredPassword.length
                                      ? Colors.white
                                      : Colors.grey[600],
                                ),
                              ),
                            ),
                          ),
                        ),
                        
                        SizedBox(height: 30),
                        
                        // Number pad
                        Expanded(
                          child: Container(
                            margin: EdgeInsets.symmetric(horizontal: 40),
                            child: GridView.builder(
                              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                                crossAxisCount: 3,
                                childAspectRatio: 1.2,
                                crossAxisSpacing: 15,
                                mainAxisSpacing: 15,
                              ),
                              itemCount: 12,
                              itemBuilder: (context, index) {
                                if (index == 9) {
                                  return _buildNumberButton('');
                                } else if (index == 10) {
                                  return _buildNumberButton('0');
                                } else if (index == 11) {
                                  return _buildActionButton(
                                    Icons.backspace,
                                    _onBackspacePressed,
                                  );
                                } else {
                                  return _buildNumberButton('${index + 1}');
                                }
                              },
                            ),
                          ),
                        ),
                        
                        SizedBox(height: 20),
                        
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                          children: [
                            ElevatedButton(
                              onPressed: () {
                                setState(() {
                                  _showPasswordField = false;
                                  _enteredPassword = '';
                                });
                              },
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.grey[700],
                                foregroundColor: Colors.white,
                              ),
                              child: Text('Cancel'),
                            ),
                            ElevatedButton(
                              onPressed: _enteredPassword.isNotEmpty ? _onUnlockPressed : null,
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.green[600],
                                foregroundColor: Colors.white,
                              ),
                              child: Text('Unlock'),
                            ),
                          ],
                        ),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNumberButton(String number) {
    if (number.isEmpty) {
      return Container();
    }
    
    return ElevatedButton(
      onPressed: () => _onNumberPressed(number),
      style: ElevatedButton.styleFrom(
        backgroundColor: Colors.grey[800],
        foregroundColor: Colors.white,
        shape: CircleBorder(),
      ),
      child: Text(
        number,
        style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
      ),
    );
  }

  Widget _buildActionButton(IconData icon, VoidCallback onPressed) {
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        backgroundColor: Colors.grey[700],
        foregroundColor: Colors.white,
        shape: CircleBorder(),
      ),
      child: Icon(icon, size: 24),
    );
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _timer?.cancel();
    _passwordController.dispose();
    super.dispose();
  }
}

