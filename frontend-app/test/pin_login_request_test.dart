/*
 * @file pin_login_request_test.dart
 * @description PIN 登入請求測試 / PIN login request tests
 * @description_en Verifies request validation and JSON payload shape.
 * @description_zh 驗證請求驗證邏輯與 JSON payload 結構。
 */
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/core/models/pin_login_request.dart';

void main() {
  group('PinLoginRequest', () {
    test('is valid when terminal code and PIN are present', () {
      const request = PinLoginRequest(pin: '1234', terminalCode: 'T-001');

      expect(request.isValid, isTrue);
      expect(request.toJson(), <String, Object?>{
        'pin': '1234',
        'terminalCode': 'T-001',
      });
    });

    test('is invalid when PIN is shorter than four digits', () {
      const request = PinLoginRequest(pin: '123', terminalCode: 'T-001');

      expect(request.isValid, isFalse);
    });

    test('is invalid without a terminal identifier', () {
      const request = PinLoginRequest(pin: '1234');

      expect(request.isValid, isFalse);
      expect(request.toJson(), <String, Object?>{'pin': '1234'});
    });
  });
}
