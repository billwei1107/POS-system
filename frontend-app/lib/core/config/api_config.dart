/*
 * @file api_config.dart
 * @description API 設定 / API configuration
 * @description_en Defines backend API base URL and resource endpoint helpers.
 * @description_zh 定義後端 API 基礎網址與資源端點輔助方法。
 */

class ApiConfig {
  const ApiConfig({required this.baseUrl});

  factory ApiConfig.fromEnvironment() {
    return const ApiConfig(
      baseUrl: String.fromEnvironment(
        'POS_API_BASE_URL',
        defaultValue: 'http://10.0.2.2:38080',
      ),
    );
  }

  final String baseUrl;

  Uri endpoint(String path, [Map<String, String>? queryParameters]) {
    final normalizedBaseUrl = baseUrl.endsWith('/')
        ? baseUrl.substring(0, baseUrl.length - 1)
        : baseUrl;
    final normalizedPath = path.startsWith('/') ? path : '/$path';

    return Uri.parse(
      '$normalizedBaseUrl$normalizedPath',
    ).replace(queryParameters: queryParameters);
  }

  Uri get pinLoginEndpoint {
    return endpoint('/api/v1/pos/auth/pin-login');
  }

  Uri get productsEndpoint {
    return endpoint('/api/v1/pos/products');
  }
}
