/*
 * @file pos_session.dart
 * @description POS 工作階段模型 / POS session model
 * @description_en Represents an authenticated cashier session on a POS terminal.
 * @description_zh 表示 POS 終端上已驗證的收銀員工作階段。
 */

class PosSession {
  const PosSession({
    required this.sessionId,
    required this.storeId,
    required this.terminalId,
    required this.cashierId,
    required this.cashierName,
    required this.accessToken,
    required this.startedAt,
    required this.expiresAt,
  });

  final String sessionId;
  final String storeId;
  final String terminalId;
  final String cashierId;
  final String cashierName;
  final String accessToken;
  final DateTime startedAt;
  final DateTime expiresAt;

  bool isExpired(DateTime now) {
    return !expiresAt.isAfter(now);
  }
}
