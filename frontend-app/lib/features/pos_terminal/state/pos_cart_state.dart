/*
 * @file pos_cart_state.dart
 * @description POS 購物車狀態 / POS cart state
 * @description_en Keeps cart mutations and amount calculations testable outside widgets.
 * @description_zh 將購物車異動與金額計算集中管理，方便脫離 Widget 測試。
 */
import '../../../core/models/cart_item.dart';
import '../../../core/models/product.dart';

class PosCartState {
  PosCartState({required List<CartItem> items})
    : items = List.unmodifiable(items);

  factory PosCartState.empty() {
    return PosCartState(items: const <CartItem>[]);
  }

  final List<CartItem> items;

  bool get isEmpty => items.isEmpty;

  int get subtotal {
    return items.fold<int>(0, (sum, item) => sum + item.subtotal);
  }

  int get discount {
    return subtotal >= 200 ? (subtotal * 0.1).round() : 0;
  }

  int get total {
    return subtotal - discount;
  }

  // ========================================
  // 加入商品 / Add Product
  // ========================================
  PosCartState addProduct(Product product) {
    final nextItems = List<CartItem>.of(items);
    final index = nextItems.indexWhere((item) => item.product.id == product.id);

    if (index == -1) {
      nextItems.add(CartItem(product: product, quantity: 1));
    } else {
      final item = nextItems[index];
      nextItems[index] = item.copyWith(quantity: item.quantity + 1);
    }

    return PosCartState(items: nextItems);
  }

  // ========================================
  // 調整商品數量 / Adjust Product Quantity
  // ========================================
  PosCartState increaseProduct(String productId) {
    return _updateQuantity(productId, 1);
  }

  PosCartState decreaseProduct(String productId) {
    return _updateQuantity(productId, -1);
  }

  PosCartState removeProduct(String productId) {
    final nextItems = items
        .where((item) => item.product.id != productId)
        .toList(growable: false);

    if (nextItems.length == items.length) {
      return this;
    }

    return PosCartState(items: nextItems);
  }

  PosCartState _updateQuantity(String productId, int delta) {
    final nextItems = <CartItem>[];
    var changed = false;

    for (final item in items) {
      if (item.product.id != productId) {
        nextItems.add(item);
        continue;
      }

      changed = true;
      final nextQuantity = item.quantity + delta;
      if (nextQuantity > 0) {
        nextItems.add(item.copyWith(quantity: nextQuantity));
      }
    }

    if (!changed) {
      return this;
    }

    return PosCartState(items: nextItems);
  }

  // ========================================
  // 清空購物車 / Clear Cart
  // ========================================
  PosCartState clear() {
    if (items.isEmpty) {
      return this;
    }

    return PosCartState.empty();
  }
}
