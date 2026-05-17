/*
 * @file demo_product_repository.dart
 * @description Demo 商品 Repository / Demo product repository
 * @description_en Provides offline demo products through the product repository contract.
 * @description_zh 透過商品 Repository 合約提供離線 Demo 商品資料。
 */
import '../models/product.dart';
import '../services/demo_catalog.dart';
import 'product_repository.dart';

class DemoProductRepository implements ProductRepository {
  const DemoProductRepository();

  @override
  Future<List<Product>> listProducts() async {
    return demoCatalog;
  }
}
