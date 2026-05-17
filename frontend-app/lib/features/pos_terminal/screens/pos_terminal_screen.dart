/*
 * @file pos_terminal_screen.dart
 * @description POS 終端主畫面 / POS terminal main screen
 * @description_en Provides the first Android tablet cashier workflow shell.
 * @description_zh 提供第一版 Android 平板收銀工作流骨架。
 */
import 'package:flutter/material.dart';

import '../../../core/models/cart_item.dart';
import '../../../core/models/product.dart';
import '../../../core/services/demo_catalog.dart';

class PosTerminalScreen extends StatefulWidget {
  const PosTerminalScreen({super.key});

  @override
  State<PosTerminalScreen> createState() => _PosTerminalScreenState();
}

class _PosTerminalScreenState extends State<PosTerminalScreen> {
  final List<CartItem> _cart = <CartItem>[];
  String _selectedCategory = '全部商品';

  int get _subtotal {
    return _cart.fold<int>(0, (sum, item) => sum + item.subtotal);
  }

  int get _discount {
    return _subtotal >= 200 ? (_subtotal * 0.1).round() : 0;
  }

  int get _total {
    return _subtotal - _discount;
  }

  List<String> get _categories {
    return <String>{
      '全部商品',
      ...demoCatalog.map((product) => product.category),
    }.toList();
  }

  List<Product> get _products {
    if (_selectedCategory == '全部商品') {
      return demoCatalog;
    }

    return demoCatalog
        .where((product) => product.category == _selectedCategory)
        .toList();
  }

  // ========================================
  // 購物車操作 / Cart Actions
  // ========================================
  void _addProduct(Product product) {
    final index = _cart.indexWhere((item) => item.product.id == product.id);
    setState(() {
      if (index == -1) {
        _cart.add(CartItem(product: product, quantity: 1));
      } else {
        final item = _cart[index];
        _cart[index] = item.copyWith(quantity: item.quantity + 1);
      }
    });
  }

  void _clearCart() {
    setState(_cart.clear);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            final isCompact = constraints.maxWidth < 900;
            return isCompact ? _buildCompactLayout() : _buildTabletLayout();
          },
        ),
      ),
    );
  }

  Widget _buildTabletLayout() {
    return Row(
      children: [
        const _SideRail(),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const _TerminalHeader(),
                const SizedBox(height: 24),
                _CategoryBar(
                  categories: _categories,
                  selectedCategory: _selectedCategory,
                  onChanged: (category) {
                    setState(() => _selectedCategory = category);
                  },
                ),
                const SizedBox(height: 20),
                Expanded(
                  child: _ProductGrid(
                    products: _products,
                    onAddProduct: _addProduct,
                  ),
                ),
              ],
            ),
          ),
        ),
        _CartPanel(
          items: _cart,
          subtotal: _subtotal,
          discount: _discount,
          total: _total,
          onClear: _clearCart,
        ),
      ],
    );
  }

  Widget _buildCompactLayout() {
    return Column(
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: _TerminalHeader(),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: _CategoryBar(
            categories: _categories,
            selectedCategory: _selectedCategory,
            onChanged: (category) {
              setState(() => _selectedCategory = category);
            },
          ),
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: _ProductGrid(products: _products, onAddProduct: _addProduct),
          ),
        ),
        SizedBox(
          height: 300,
          child: _CartPanel(
            items: _cart,
            subtotal: _subtotal,
            discount: _discount,
            total: _total,
            onClear: _clearCart,
          ),
        ),
      ],
    );
  }
}

class _SideRail extends StatelessWidget {
  const _SideRail();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 88,
      color: const Color(0xFF202230),
      child: const Column(
        children: [
          SizedBox(height: 24),
          Icon(Icons.point_of_sale, size: 36, color: Color(0xFFFF7A1A)),
          SizedBox(height: 28),
          _RailIcon(icon: Icons.shopping_cart_checkout, selected: true),
          _RailIcon(icon: Icons.receipt_long),
          _RailIcon(icon: Icons.inventory_2),
          Spacer(),
          _RailIcon(icon: Icons.lock_clock),
          SizedBox(height: 18),
        ],
      ),
    );
  }
}

class _RailIcon extends StatelessWidget {
  const _RailIcon({required this.icon, this.selected = false});

  final IconData icon;
  final bool selected;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 56,
      height: 56,
      margin: const EdgeInsets.only(bottom: 14),
      decoration: BoxDecoration(
        color: selected ? const Color(0xFFFF6B00) : Colors.transparent,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Icon(
        icon,
        color: selected ? Colors.white : const Color(0xFFB5B8C8),
      ),
    );
  }
}

class _TerminalHeader extends StatelessWidget {
  const _TerminalHeader();

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        const Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Xinyi Flagship Store',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.w800),
              ),
              SizedBox(height: 6),
              Text(
                'Android Tablet POS · Demo Terminal 01',
                style: TextStyle(color: Color(0xFFAEB2C3)),
              ),
            ],
          ),
        ),
        _StatusPill(
          icon: Icons.wifi,
          label: '線上',
          color: const Color(0xFF2BD675),
          background: const Color(0xFF173F2B),
        ),
        const SizedBox(width: 12),
        _StatusPill(
          icon: Icons.sync,
          label: '待同步 0',
          color: Color(0xFFB8C7FF),
          background: Color(0xFF242C49),
        ),
      ],
    );
  }
}

class _StatusPill extends StatelessWidget {
  const _StatusPill({
    required this.icon,
    required this.label,
    required this.color,
    required this.background,
  });

  final IconData icon;
  final String label;
  final Color color;
  final Color background;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 44,
      padding: const EdgeInsets.symmetric(horizontal: 14),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(width: 8),
          Text(
            label,
            style: TextStyle(color: color, fontWeight: FontWeight.w700),
          ),
        ],
      ),
    );
  }
}

class _CategoryBar extends StatelessWidget {
  const _CategoryBar({
    required this.categories,
    required this.selectedCategory,
    required this.onChanged,
  });

  final List<String> categories;
  final String selectedCategory;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 54,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemBuilder: (context, index) {
          final category = categories[index];
          final selected = category == selectedCategory;
          return ChoiceChip(
            selected: selected,
            label: Text(category),
            onSelected: (_) => onChanged(category),
            selectedColor: const Color(0xFFA9BEFF),
            backgroundColor: const Color(0xFF262938),
            labelStyle: TextStyle(
              color: selected ? const Color(0xFF10131B) : Colors.white,
              fontWeight: FontWeight.w800,
            ),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(8),
              side: BorderSide(
                color: selected
                    ? const Color(0xFFA9BEFF)
                    : const Color(0xFF383C4D),
              ),
            ),
          );
        },
        separatorBuilder: (context, index) => const SizedBox(width: 12),
        itemCount: categories.length,
      ),
    );
  }
}

class _ProductGrid extends StatelessWidget {
  const _ProductGrid({required this.products, required this.onAddProduct});

  final List<Product> products;
  final ValueChanged<Product> onAddProduct;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth <= 0 || constraints.maxHeight <= 0) {
          return const SizedBox.shrink();
        }

        return GridView.builder(
          gridDelegate: const SliverGridDelegateWithMaxCrossAxisExtent(
            maxCrossAxisExtent: 280,
            mainAxisExtent: 178,
            crossAxisSpacing: 14,
            mainAxisSpacing: 14,
          ),
          itemCount: products.length,
          itemBuilder: (context, index) {
            final product = products[index];
            return _ProductTile(
              product: product,
              onAdd: () => onAddProduct(product),
            );
          },
        );
      },
    );
  }
}

class _ProductTile extends StatelessWidget {
  const _ProductTile({required this.product, required this.onAdd});

  final Product product;
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: const Color(0xFF242736),
      borderRadius: BorderRadius.circular(8),
      child: InkWell(
        borderRadius: BorderRadius.circular(8),
        onTap: onAdd,
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                product.category,
                style: const TextStyle(
                  color: Color(0xFFAEB2C3),
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                product.name,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const Spacer(),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      '\$${product.price}',
                      style: const TextStyle(
                        color: Color(0xFFFF7A1A),
                        fontSize: 24,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ),
                  IconButton.filled(
                    tooltip: '加入購物車',
                    onPressed: onAdd,
                    icon: const Icon(Icons.add_shopping_cart),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _CartPanel extends StatelessWidget {
  const _CartPanel({
    required this.items,
    required this.subtotal,
    required this.discount,
    required this.total,
    required this.onClear,
  });

  final List<CartItem> items;
  final int subtotal;
  final int discount;
  final int total;
  final VoidCallback onClear;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 360,
      padding: const EdgeInsets.all(22),
      decoration: const BoxDecoration(
        color: Color(0xFF252837),
        border: Border(left: BorderSide(color: Color(0xFF3A3E4F))),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Expanded(
                child: Text(
                  '目前訂單',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.w900),
                ),
              ),
              IconButton(
                tooltip: '清空購物車',
                onPressed: items.isEmpty ? null : onClear,
                icon: const Icon(Icons.delete_outline),
              ),
            ],
          ),
          const SizedBox(height: 12),
          const Text(
            'A7 桌 · 2 位 · 內用',
            style: TextStyle(
              color: Color(0xFFAEB2C3),
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 18),
          Expanded(
            child: items.isEmpty
                ? const Center(
                    child: Text(
                      '尚未加入商品',
                      style: TextStyle(color: Color(0xFFAEB2C3)),
                    ),
                  )
                : ListView.separated(
                    itemBuilder: (context, index) {
                      final item = items[index];
                      return _CartItemRow(item: item);
                    },
                    separatorBuilder: (context, index) =>
                        const Divider(color: Color(0xFF3A3E4F)),
                    itemCount: items.length,
                  ),
          ),
          const Divider(color: Color(0xFF3A3E4F)),
          _AmountRow(label: '小計', amount: subtotal),
          if (discount > 0)
            _AmountRow(label: '咖啡滿 200 享 9 折', amount: -discount, accent: true),
          const SizedBox(height: 12),
          Row(
            children: [
              const Expanded(
                child: Text(
                  '總計',
                  style: TextStyle(fontSize: 26, fontWeight: FontWeight.w900),
                ),
              ),
              Text(
                '\$$total',
                style: const TextStyle(
                  color: Color(0xFFFF7A1A),
                  fontSize: 34,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ],
          ),
          const SizedBox(height: 18),
          SizedBox(
            width: double.infinity,
            height: 64,
            child: FilledButton.icon(
              onPressed: items.isEmpty ? null : () {},
              icon: const Icon(Icons.payments),
              label: const Text('現金結帳'),
            ),
          ),
        ],
      ),
    );
  }
}

class _CartItemRow extends StatelessWidget {
  const _CartItemRow({required this.item});

  final CartItem item;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.product.name,
                  style: const TextStyle(fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 6),
                Text(
                  'x ${item.quantity}',
                  style: const TextStyle(color: Color(0xFFAEB2C3)),
                ),
              ],
            ),
          ),
          Text(
            '\$${item.subtotal}',
            style: const TextStyle(fontWeight: FontWeight.w900),
          ),
        ],
      ),
    );
  }
}

class _AmountRow extends StatelessWidget {
  const _AmountRow({
    required this.label,
    required this.amount,
    this.accent = false,
  });

  final String label;
  final int amount;
  final bool accent;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 5),
      child: Row(
        children: [
          Expanded(
            child: Text(
              label,
              style: TextStyle(
                color: accent
                    ? const Color(0xFF2BD675)
                    : const Color(0xFFAEB2C3),
              ),
            ),
          ),
          Text(
            amount < 0 ? '-\$${amount.abs()}' : '\$$amount',
            style: TextStyle(
              color: accent ? const Color(0xFF2BD675) : const Color(0xFFAEB2C3),
              fontWeight: FontWeight.w800,
            ),
          ),
        ],
      ),
    );
  }
}
