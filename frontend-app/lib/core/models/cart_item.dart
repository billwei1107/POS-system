/*
 * @file cart_item.dart
 * @description 購物車項目模型 / Cart item model
 * @description_en Tracks quantity and subtotal for a product in the cart.
 * @description_zh 追蹤購物車內商品數量與小計。
 */
import 'product.dart';

class CartItem {
  const CartItem({required this.product, required this.quantity});

  final Product product;
  final int quantity;

  int get subtotal => product.price * quantity;

  CartItem copyWith({int? quantity}) {
    return CartItem(product: product, quantity: quantity ?? this.quantity);
  }
}
