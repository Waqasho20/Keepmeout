// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';

import 'package:admin_lock_app/main.dart';

void main() {
  testWidgets('Admin Lock App smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(AdminLockApp());

    // Verify that the admin screen loads
    expect(find.text('Admin Lock Control'), findsOneWidget);
    expect(find.text('Device Lock Control'), findsOneWidget);
  });
}

