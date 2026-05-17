/*
 * @file pos_session_state_test.dart
 * @description POS 工作階段狀態測試 / POS session state tests
 * @description_en Verifies login, logout, and session activity state transitions.
 * @description_zh 驗證登入、登出與工作階段有效性的狀態轉換。
 */
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/core/models/pin_login_request.dart';
import 'package:xinyi_pos_app/core/repositories/demo_auth_repository.dart';
import 'package:xinyi_pos_app/features/auth/state/pos_session_state.dart';

void main() {
  group('PosSessionState', () {
    test('starts signed out', () {
      final state = PosSessionState.signedOut();

      expect(state.session, isNull);
      expect(state.isSignedIn, isFalse);
      expect(state.isLoading, isFalse);
      expect(state.errorCode, isNull);
    });

    test('signs in with repository session and reports activity', () async {
      final repository = DemoAuthRepository(
        now: () => DateTime.utc(2026, 5, 17, 8),
      );
      const request = PinLoginRequest(pin: '1234', terminalCode: 'T-001');

      final state = await PosSessionState.signedOut().loginWithPin(
        repository: repository,
        request: request,
      );

      expect(state.isSignedIn, isTrue);
      expect(state.errorCode, isNull);
      expect(state.isActive(DateTime.utc(2026, 5, 17, 15, 59)), isTrue);
      expect(state.isActive(DateTime.utc(2026, 5, 17, 16)), isFalse);
    });

    test('captures login failures without keeping a session', () async {
      const repository = DemoAuthRepository();
      const request = PinLoginRequest(pin: '0000', terminalCode: 'T-001');

      final state = await PosSessionState.signedOut().loginWithPin(
        repository: repository,
        request: request,
      );

      expect(state.session, isNull);
      expect(state.isSignedIn, isFalse);
      expect(state.errorCode, contains('invalid_pin'));
    });

    test('logout clears the active session', () async {
      final repository = DemoAuthRepository(
        now: () => DateTime.utc(2026, 5, 17, 8),
      );
      const request = PinLoginRequest(pin: '1234', terminalCode: 'T-001');
      final signedIn = await PosSessionState.signedOut().loginWithPin(
        repository: repository,
        request: request,
      );

      final signedOut = signedIn.logout();

      expect(signedOut.session, isNull);
      expect(signedOut.isSignedIn, isFalse);
    });
  });
}
