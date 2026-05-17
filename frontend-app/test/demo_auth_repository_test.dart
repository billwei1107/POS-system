/*
 * @file demo_auth_repository_test.dart
 * @description Demo 認證 Repository 測試 / Demo auth repository tests
 * @description_en Verifies deterministic PIN login behavior without network calls.
 * @description_zh 驗證不發出網路請求的可預期 PIN 登入行為。
 */
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/core/models/pin_login_request.dart';
import 'package:xinyi_pos_app/core/repositories/demo_auth_repository.dart';

void main() {
  group('DemoAuthRepository', () {
    test('returns a POS session for the accepted PIN', () async {
      final repository = DemoAuthRepository(
        now: () => DateTime.utc(2026, 5, 17, 8),
      );
      const request = PinLoginRequest(pin: '1234', terminalCode: 'T-001');

      final session = await repository.loginWithPin(request);

      expect(
        session.sessionId,
        'demo-session-00000000-0000-0000-0000-000000000001-00000000-0000-0000-0000-000000000101',
      );
      expect(session.storeId, '00000000-0000-0000-0000-000000000001');
      expect(session.terminalId, '00000000-0000-0000-0000-000000000101');
      expect(session.cashierId, 'demo-cashier-001');
      expect(session.accessToken, isNotEmpty);
      expect(session.startedAt, DateTime.utc(2026, 5, 17, 8));
      expect(session.expiresAt, DateTime.utc(2026, 5, 17, 16));
    });

    test('rejects invalid PIN requests', () {
      const repository = DemoAuthRepository();
      const request = PinLoginRequest(pin: '0000', terminalCode: 'T-001');

      expect(
        () => repository.loginWithPin(request),
        throwsA(isA<AuthException>()),
      );
    });
  });
}
