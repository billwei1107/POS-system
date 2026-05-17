/*
 * @file demo_product_repository_test.dart
 * @description Demo 商品 Repository 測試 / Demo product repository tests
 * @description_en Verifies the repository contract returns the offline demo catalog.
 * @description_zh 驗證 Repository 合約可回傳離線 Demo 商品目錄。
 */
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/core/repositories/demo_product_repository.dart';

void main() {
  group('DemoProductRepository', () {
    test('returns demo products through the repository contract', () async {
      const repository = DemoProductRepository();

      final products = await repository.listProducts();

      expect(products, isNotEmpty);
      expect(
        products.map((product) => product.id),
        contains('demo-latte-12oz'),
      );
      expect(products.every((product) => product.sku.isNotEmpty), isTrue);
    });
  });
}
