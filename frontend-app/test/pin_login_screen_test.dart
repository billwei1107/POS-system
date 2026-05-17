/*
 * @file pin_login_screen_test.dart
 * @description PIN 登入畫面測試 / PIN login screen tests
 * @description_en Verifies demo PIN success and failure behavior.
 * @description_zh 驗證 demo PIN 登入成功與失敗行為。
 */
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/core/models/pos_session.dart';
import 'package:xinyi_pos_app/core/repositories/demo_auth_repository.dart';
import 'package:xinyi_pos_app/features/auth/screens/pin_login_screen.dart';

void main() {
  Future<PosSession?> pumpLoginScreen(
    WidgetTester tester, {
    DemoAuthRepository repository = const DemoAuthRepository(),
  }) async {
    PosSession? capturedSession;

    await tester.pumpWidget(
      MaterialApp(
        theme: ThemeData(
          brightness: Brightness.dark,
          scaffoldBackgroundColor: const Color(0xFF11141B),
          useMaterial3: true,
        ),
        home: PinLoginScreen(
          authRepository: repository,
          onSignedIn: (session) => capturedSession = session,
        ),
      ),
    );
    await tester.pumpAndSettle();

    return capturedSession;
  }

  Future<void> enterPin(WidgetTester tester, String pin) async {
    for (final digit in pin.split('')) {
      await tester.tap(find.byKey(ValueKey('pin-digit-$digit')));
      await tester.pump();
    }
  }

  testWidgets('submits the accepted demo PIN', (tester) async {
    PosSession? capturedSession;

    await tester.pumpWidget(
      MaterialApp(
        theme: ThemeData(
          brightness: Brightness.dark,
          scaffoldBackgroundColor: const Color(0xFF11141B),
          useMaterial3: true,
        ),
        home: PinLoginScreen(
          authRepository: DemoAuthRepository(
            now: () => DateTime.utc(2026, 5, 17, 8),
          ),
          onSignedIn: (session) => capturedSession = session,
        ),
      ),
    );
    await tester.pumpAndSettle();

    await enterPin(tester, '1234');
    await tester.tap(find.byKey(const ValueKey('pin-submit-button')));
    await tester.pumpAndSettle();

    expect(capturedSession, isNotNull);
    expect(capturedSession!.cashierName, 'Demo Cashier');
    expect(capturedSession!.startedAt, DateTime.utc(2026, 5, 17, 8));
  });

  testWidgets('shows an error when PIN is rejected', (tester) async {
    await pumpLoginScreen(tester);

    await enterPin(tester, '0000');
    await tester.tap(find.byKey(const ValueKey('pin-submit-button')));
    await tester.pumpAndSettle();

    expect(find.byKey(const ValueKey('pin-error-message')), findsOneWidget);
    expect(find.text('PIN 驗證失敗'), findsOneWidget);
  });

  testWidgets('does not submit before four digits', (tester) async {
    PosSession? capturedSession;

    await tester.pumpWidget(
      MaterialApp(
        theme: ThemeData(
          brightness: Brightness.dark,
          scaffoldBackgroundColor: const Color(0xFF11141B),
          useMaterial3: true,
        ),
        home: PinLoginScreen(
          authRepository: const DemoAuthRepository(),
          onSignedIn: (session) => capturedSession = session,
        ),
      ),
    );
    await tester.pumpAndSettle();

    await enterPin(tester, '123');
    await tester.tap(find.byKey(const ValueKey('pin-submit-button')));
    await tester.pumpAndSettle();

    expect(capturedSession, isNull);
    expect(find.byKey(const ValueKey('pin-error-message')), findsNothing);
  });
}
