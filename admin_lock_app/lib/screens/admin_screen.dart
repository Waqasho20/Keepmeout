import 'package:flutter/material.dart';
import '../services/lock_service.dart';

class AdminScreen extends StatefulWidget {
  const AdminScreen({super.key});

  @override
  State<AdminScreen> createState() => _AdminScreenState();
}

class _AdminScreenState extends State<AdminScreen> {
  final TextEditingController _timeController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final LockService _lockService = LockService();
  
  int _selectedMinutes = 5;
  bool _isLocked = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Admin Lock Control'),
        backgroundColor: Colors.blue[800],
        foregroundColor: Colors.white,
      ),
      body: Container(
        padding: EdgeInsets.all(20),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Colors.blue[50]!, Colors.white],
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              elevation: 4,
              child: Padding(
                padding: EdgeInsets.all(20),
                child: Column(
                  children: [
                    Icon(
                      Icons.admin_panel_settings,
                      size: 60,
                      color: Colors.blue[800],
                    ),
                    SizedBox(height: 10),
                    Text(
                      'Device Lock Control',
                      style: TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        color: Colors.blue[800],
                      ),
                    ),
                    SizedBox(height: 20),
                    Text(
                      'Set Admin Password:',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
                    ),
                    SizedBox(height: 10),
                    TextField(
                      controller: _passwordController,
                      obscureText: true,
                      decoration: InputDecoration(
                        border: OutlineInputBorder(),
                        hintText: 'Enter admin password',
                        prefixIcon: Icon(Icons.lock),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            SizedBox(height: 20),
            Card(
              elevation: 4,
              child: Padding(
                padding: EdgeInsets.all(20),
                child: Column(
                  children: [
                    Text(
                      'Lock Duration',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 20),
                    Row(
                      children: [
                        Expanded(
                          child: Text('Minutes: $_selectedMinutes'),
                        ),
                        Expanded(
                          flex: 2,
                          child: Slider(
                            value: _selectedMinutes.toDouble(),
                            min: 1,
                            max: 60,
                            divisions: 59,
                            label: '$_selectedMinutes minutes',
                            onChanged: (value) {
                              setState(() {
                                _selectedMinutes = value.round();
                              });
                            },
                          ),
                        ),
                      ],
                    ),
                    SizedBox(height: 10),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        _buildQuickTimeButton(5),
                        _buildQuickTimeButton(10),
                        _buildQuickTimeButton(15),
                        _buildQuickTimeButton(30),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            SizedBox(height: 30),
            ElevatedButton(
              onPressed: _isLocked ? null : _startLock,
              style: ElevatedButton.styleFrom(
                backgroundColor: _isLocked ? Colors.grey : Colors.red[600],
                foregroundColor: Colors.white,
                padding: EdgeInsets.symmetric(vertical: 15),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(_isLocked ? Icons.lock : Icons.lock_open),
                  SizedBox(width: 10),
                  Text(
                    _isLocked ? 'Device Locked' : 'START LOCK',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ),
            if (_isLocked) ...[
              SizedBox(height: 20),
              Card(
                color: Colors.red[50],
                child: Padding(
                  padding: EdgeInsets.all(15),
                  child: Row(
                    children: [
                      Icon(Icons.warning, color: Colors.red),
                      SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          'Device is currently locked. Use admin password to unlock early.',
                          style: TextStyle(color: Colors.red[800]),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildQuickTimeButton(int minutes) {
    return ElevatedButton(
      onPressed: () {
        setState(() {
          _selectedMinutes = minutes;
        });
      },
      style: ElevatedButton.styleFrom(
        backgroundColor: _selectedMinutes == minutes ? Colors.blue[600] : Colors.grey[300],
        foregroundColor: _selectedMinutes == minutes ? Colors.white : Colors.black,
      ),
      child: Text('${minutes}m'),
    );
  }

  void _startLock() async {
    if (_passwordController.text.isEmpty) {
      _showErrorDialog('Please set an admin password first');
      return;
    }

    if (_passwordController.text.length < 4) {
      _showErrorDialog('Password must be at least 4 characters');
      return;
    }

    try {
      await _lockService.startLock(_selectedMinutes, _passwordController.text);
      setState(() {
        _isLocked = true;
      });
      
      if (mounted) {
        Navigator.pushReplacementNamed(
          context,
          '/lock',
          arguments: {
            'duration': _selectedMinutes,
            'password': _passwordController.text,
          },
        );
      }
    } catch (e) {
      _showErrorDialog('Failed to start lock: $e');
    }
  }

  void _showErrorDialog(String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Error'),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('OK'),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _timeController.dispose();
    _passwordController.dispose();
    super.dispose();
  }
}

