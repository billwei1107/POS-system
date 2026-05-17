/*
 * @file pos_cart_state_test.dart
 * @description POS 購物車狀態測試 / POS cart state tests
 * @description_en Verifies cart quantity, discount, total and clearing logic.
 * @description_zh 驗證購物車數量、折扣、總計與清空邏輯。
 */
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/core/services/demo_catalog.dart';
import 'package:xinyi_pos_app/features/pos_terminal/state/pos_cart_state.dart';

void main() {
  group('PosCartState', () {
    test('starts empty with zero amounts', () {
      final cart = PosCartState.empty();

      expect(cart.items, isEmpty);
      expect(cart.isEmpty, isTrue);
      expect(cart.subtotal, 0);
      expect(cart.discount, 0);
      expect(cart.total, 0);
    });

    test('adds products and increments quantity for matching product id', () {
      final americano = demoCatalog.firstWhere(
        (product) => product.id == 'demo-americano-12oz',
      );

      final cart = PosCartState.empty()
          .addProduct(americano)
          .addProduct(americano);

      expect(cart.items, hasLength(1));
      expect(cart.items.single.quantity, 2);
      expect(cart.subtotal, 180);
      expect(cart.discount, 0);
      expect(cart.total, 180);
    });

    test('rounds ten percent discount after subtotal reaches 200', () {
      final latte = demoCatalog.firstWhere(
        (product) => product.id == 'demo-latte-12oz',
      );
      final oatLatte = demoCatalog.firstWhere(
        (product) => product.id == 'demo-oat-latte-12oz',
      );

      final cart = PosCartState.empty().addProduct(latte).addProduct(oatLatte);

      expect(cart.subtotal, 265);
      expect(cart.discount, 27);
      expect(cart.total, 238);
    });

    test('clear returns an empty cart without mutating previous state', () {
      final croissant = demoCatalog.firstWhere(
        (product) => product.id == 'demo-croissant',
      );
      final cart = PosCartState.empty().addProduct(croissant);

      final cleared = cart.clear();

      expect(cart.items, hasLength(1));
      expect(cleared.items, isEmpty);
      expect(cleared.total, 0);
    });
  });
}
