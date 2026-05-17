/*
 * @file pos_terminal_screen.dart
 * @description POS 終端主畫面 / POS terminal main screen
 * @description_en Provides the first Android tablet cashier workflow shell.
 * @description_zh 提供第一版 Android 平板收銀工作流骨架。
 */
import 'package:flutter/material.dart';

import '../../../core/models/cart_item.dart';
import '../../../core/models/pos_session.dart';
import '../../../core/models/product.dart';
import '../../../core/repositories/demo_product_repository.dart';
import '../../../core/repositories/product_repository.dart';
import '../state/pos_cart_state.dart';

class PosTerminalScreen extends StatefulWidget {
  const PosTerminalScreen({
    super.key,
    this.productRepository = const DemoProductRepository(),
    this.session,
    this.onLogout,
  });

  final ProductRepository productRepository;
  final PosSession? session;
  final VoidCallback? onLogout;

  @override
  State<PosTerminalScreen> createState() => _PosTerminalScreenState();
}

class _PosTerminalScreenState extends State<PosTerminalScreen> {
  PosCartState _cart = PosCartState.empty();
  String _selectedCategory = '全部商品';
  List<Product> _catalog = const <Product>[];
  bool _isLoadingCatalog = true;

  @override
  void initState() {
    super.initState();
    _loadCatalog();
  }

  List<String> get _categories {
    return <String>{
      '全部商品',
      ..._catalog.map((product) => product.category),
    }.toList();
  }

  List<Product> get _products {
    if (_selectedCategory == '全部商品') {
      return _catalog;
    }

    return _catalog
        .where((product) => product.category == _selectedCategory)
        .toList();
  }

  // ========================================
  // 商品目錄載入 / Product Catalog Loading
  // ========================================
  Future<void> _loadCatalog() async {
    final catalog = await widget.productRepository.listProducts();

    if (!mounted) {
      return;
    }

    setState(() {
      _catalog = List<Product>.unmodifiable(catalog);
      _isLoadingCatalog = false;

      if (!_categories.contains(_selectedCategory)) {
        _selectedCategory = '全部商品';
      }
    });
  }

  // ========================================
  // 購物車操作 / Cart Actions
  // ========================================
  void _addProduct(Product product) {
    setState(() => _cart = _cart.addProduct(product));
  }

  void _clearCart() {
    setState(() => _cart = _cart.clear());
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
        _SideRail(onLogout: widget.onLogout),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _TerminalHeader(
                  session: widget.session,
                  onLogout: widget.onLogout,
                ),
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
                  child: _CatalogContent(
                    isLoading: _isLoadingCatalog,
                    products: _products,
                    onAddProduct: _addProduct,
                  ),
                ),
              ],
            ),
          ),
        ),
        _CartPanel(
          items: _cart.items,
          subtotal: _cart.subtotal,
          discount: _cart.discount,
          total: _cart.total,
          onClear: _clearCart,
        ),
      ],
    );
  }

  Widget _buildCompactLayout() {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: _TerminalHeader(
            session: widget.session,
            onLogout: widget.onLogout,
          ),
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
            child: _CatalogContent(
              isLoading: _isLoadingCatalog,
              products: _products,
              onAddProduct: _addProduct,
            ),
          ),
        ),
        SizedBox(
          height: 300,
          child: _CartPanel(
            items: _cart.items,
            subtotal: _cart.subtotal,
            discount: _cart.discount,
            total: _cart.total,
            onClear: _clearCart,
          ),
        ),
      ],
    );
  }
}

class _CatalogContent extends StatelessWidget {
  const _CatalogContent({
    required this.isLoading,
    required this.products,
    required this.onAddProduct,
  });

  final bool isLoading;
  final List<Product> products;
  final ValueChanged<Product> onAddProduct;

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (products.isEmpty) {
      return const Center(
        child: Text('目前沒有可銷售商品', style: TextStyle(color: Color(0xFFAEB2C3))),
      );
    }

    return _ProductGrid(products: products, onAddProduct: onAddProduct);
  }
}

class _SideRail extends StatelessWidget {
  const _SideRail({this.onLogout});

  final VoidCallback? onLogout;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 88,
      color: const Color(0xFF202230),
      child: Column(
        children: [
          const SizedBox(height: 24),
          const Icon(Icons.point_of_sale, size: 36, color: Color(0xFFFF7A1A)),
          const SizedBox(height: 28),
          const _RailIcon(icon: Icons.shopping_cart_checkout, selected: true),
          const _RailIcon(icon: Icons.receipt_long),
          const _RailIcon(icon: Icons.inventory_2),
          const Spacer(),
          _RailIcon(
            key: const ValueKey('logout-terminal-button'),
            icon: Icons.lock_clock,
            onPressed: onLogout,
          ),
          const SizedBox(height: 18),
        ],
      ),
    );
  }
}

class _RailIcon extends StatelessWidget {
  const _RailIcon({
    super.key,
    required this.icon,
    this.selected = false,
    this.onPressed,
  });

  final IconData icon;
  final bool selected;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) {
    final iconColor = selected ? Colors.white : const Color(0xFFB5B8C8);

    return Container(
      width: 56,
      height: 56,
      margin: const EdgeInsets.only(bottom: 14),
      child: IconButton(
        tooltip: selected ? '收銀台' : '鎖定終端',
        onPressed: onPressed,
        style: IconButton.styleFrom(
          backgroundColor: selected
              ? const Color(0xFFFF6B00)
              : Colors.transparent,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        ),
        icon: Icon(icon, color: iconColor),
      ),
    );
  }
}

class _TerminalHeader extends StatelessWidget {
  const _TerminalHeader({this.session, this.onLogout});

  final PosSession? session;
  final VoidCallback? onLogout;

  @override
  Widget build(BuildContext context) {
    final cashierName = session?.cashierName ?? 'Demo Cashier';
    final titleBlock = Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Xinyi Flagship Store',
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.w800),
        ),
        const SizedBox(height: 6),
        Text(
          'Android Tablet POS · Demo Terminal 01 · $cashierName',
          style: const TextStyle(color: Color(0xFFAEB2C3)),
        ),
      ],
    );

    List<Widget> statusActions() {
      return [
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
        if (onLogout != null) ...[
          const SizedBox(width: 12),
          IconButton.filledTonal(
            key: const ValueKey('header-logout-terminal-button'),
            tooltip: '鎖定終端',
            onPressed: onLogout,
            icon: const Icon(Icons.lock_clock),
          ),
        ],
      ];
    }

    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth < 620) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              titleBlock,
              const SizedBox(height: 12),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(children: statusActions()),
              ),
            ],
          );
        }

        return Row(
          children: [
            Expanded(child: titleBlock),
            ...statusActions(),
          ],
        );
      },
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
              key: ValueKey('product-tile-${product.id}'),
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
  const _ProductTile({super.key, required this.product, required this.onAdd});

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
                    key: ValueKey('add-product-${product.id}'),
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
                key: const ValueKey('clear-cart-button'),
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
              key: const ValueKey('cash-checkout-button'),
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
