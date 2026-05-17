/*
 * @file pin_login_request.dart
 * @description PIN 登入請求模型 / PIN login request model
 * @description_en Captures the POS terminal PIN login payload before HTTP wiring.
 * @description_zh 在 HTTP 串接前定義 POS 終端 PIN 登入請求資料。
 */

class PinLoginRequest {
  const PinLoginRequest({
    required this.pin,
    this.terminalId,
    this.terminalCode,
  });

  final String pin;
  final String? terminalId;
  final String? terminalCode;

  bool get isValid {
    final normalizedPin = pin.trim();
    final hasTerminalId = terminalId != null && terminalId!.trim().isNotEmpty;
    final hasTerminalCode =
        terminalCode != null && terminalCode!.trim().isNotEmpty;

    return RegExp(r'^\d{4,6}$').hasMatch(normalizedPin) &&
        (hasTerminalId || hasTerminalCode);
  }

  Map<String, Object?> toJson() {
    final payload = <String, Object?>{'pin': pin};

    if (terminalId != null && terminalId!.trim().isNotEmpty) {
      payload['terminalId'] = terminalId;
    }

    if (terminalCode != null && terminalCode!.trim().isNotEmpty) {
      payload['terminalCode'] = terminalCode;
    }

    return payload;
  }
}
