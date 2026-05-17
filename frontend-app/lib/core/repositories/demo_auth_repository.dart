/*
 * @file demo_auth_repository.dart
 * @description Demo 認證 Repository / Demo auth repository
 * @description_en Provides a deterministic PIN login session without network calls.
 * @description_zh 不發出網路請求，提供可預期的 PIN 登入工作階段。
 */
import '../models/pin_login_request.dart';
import '../models/pos_session.dart';
import 'auth_repository.dart';

class DemoAuthRepository implements AuthRepository {
  const DemoAuthRepository({
    this.acceptedPin = '1234',
    this.now = _defaultNow,
    this.storeId = '00000000-0000-0000-0000-000000000001',
    this.terminalId = '00000000-0000-0000-0000-000000000101',
  });

  final String acceptedPin;
  final DateTime Function() now;
  final String storeId;
  final String terminalId;

  @override
  Future<PosSession> loginWithPin(PinLoginRequest request) async {
    if (!request.isValid || request.pin != acceptedPin) {
      throw const AuthException('invalid_pin');
    }

    final startedAt = now().toUtc();
    final resolvedTerminalId = request.terminalId?.trim().isNotEmpty == true
        ? request.terminalId!.trim()
        : terminalId;

    return PosSession(
      sessionId: 'demo-session-$storeId-$resolvedTerminalId',
      storeId: storeId,
      terminalId: resolvedTerminalId,
      cashierId: 'demo-cashier-001',
      cashierName: 'Demo Cashier',
      accessToken: 'demo-access-token',
      startedAt: startedAt,
      expiresAt: startedAt.add(const Duration(hours: 8)),
    );
  }

  static DateTime _defaultNow() {
    return DateTime.now();
  }
}

class AuthException implements Exception {
  const AuthException(this.code);

  final String code;

  @override
  String toString() {
    return 'AuthException($code)';
  }
}
