/*
 * @file demo_catalog.dart
 * @description Demo 商品目錄 / Demo product catalog
 * @description_en Provides temporary offline-friendly products before API sync is wired.
 * @description_zh 在 API 同步接上前提供可離線展示的暫時商品資料。
 */
import '../models/product.dart';

const demoCatalog = <Product>[
  Product(
    id: 'demo-americano-12oz',
    sku: '4710000000012',
    name: '美式咖啡 12oz',
    category: '咖啡飲品',
    price: 90,
  ),
  Product(
    id: 'demo-latte-12oz',
    sku: '4710000000011',
    name: '拿鐵 12oz',
    category: '咖啡飲品',
    price: 120,
  ),
  Product(
    id: 'demo-oat-latte-12oz',
    sku: '4710000000013',
    name: '燕麥拿鐵 12oz',
    category: '咖啡飲品',
    price: 145,
  ),
  Product(
    id: 'demo-croissant',
    sku: '4710000000021',
    name: '奶油可頌',
    category: '烘焙點心',
    price: 75,
  ),
];
