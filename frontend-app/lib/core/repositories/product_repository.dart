/*
 * @file product_repository.dart
 * @description 商品 Repository 介面 / Product repository interface
 * @description_en Defines the product catalog contract before HTTP sync is wired.
 * @description_zh 在 HTTP 同步接上前定義商品目錄資料來源合約。
 */
import '../models/product.dart';

abstract interface class ProductRepository {
  Future<List<Product>> listProducts();
}
