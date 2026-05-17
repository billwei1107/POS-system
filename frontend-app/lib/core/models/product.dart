/*
 * @file product.dart
 * @description 商品模型 / Product model
 * @description_en Represents a sellable POS product cached on the terminal.
 * @description_zh 表示 POS 終端可快取與銷售的商品資料。
 */
class Product {
  const Product({
    required this.id,
    required this.sku,
    required this.name,
    required this.category,
    required this.price,
  });

  final String id;
  final String sku;
  final String name;
  final String category;
  final int price;
}
