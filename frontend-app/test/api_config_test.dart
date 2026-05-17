/*
 * @file api_config_test.dart
 * @description API 設定測試 / API configuration tests
 * @description_en Verifies POS backend endpoint generation without making network calls.
 * @description_zh 驗證 POS 後端端點產生邏輯，且不發出網路請求。
 */
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/core/config/api_config.dart';

void main() {
  group('ApiConfig', () {
    test('builds endpoint with normalized slash handling', () {
      const config = ApiConfig(baseUrl: 'http://10.0.2.2:38080/');

      final endpoint = config.endpoint('/api/v1/pos/products');

      expect(endpoint.toString(), 'http://10.0.2.2:38080/api/v1/pos/products');
    });

    test('preserves query parameters for future sync requests', () {
      const config = ApiConfig(baseUrl: 'http://localhost:38080');

      final endpoint = config.endpoint('/api/v1/pos/products', {
        'updatedAfter': '2026-05-17T12:00:00Z',
        'storeId': 'xinyi',
      });

      expect(endpoint.scheme, 'http');
      expect(endpoint.host, 'localhost');
      expect(endpoint.port, 38080);
      expect(endpoint.path, '/api/v1/pos/products');
      expect(endpoint.queryParameters['storeId'], 'xinyi');
      expect(endpoint.queryParameters['updatedAfter'], '2026-05-17T12:00:00Z');
    });

    test('exposes planned POS resource endpoints', () {
      const config = ApiConfig(baseUrl: 'http://10.0.2.2:38080');

      expect(
        config.pinLoginEndpoint.toString(),
        'http://10.0.2.2:38080/api/v1/pos/sessions/pin-login',
      );
      expect(
        config.productsEndpoint.toString(),
        'http://10.0.2.2:38080/api/v1/pos/products',
      );
    });
  });
}
