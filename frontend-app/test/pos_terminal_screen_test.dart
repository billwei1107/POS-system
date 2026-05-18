/*
 * @file pos_terminal_screen_test.dart
 * @description POS 終端 Widget 測試 / POS terminal widget tests
 * @description_en Verifies the first cashier shell can add products and calculate totals.
 * @description_zh 驗證第一版收銀骨架可加入商品並計算金額。
 */
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:xinyi_pos_app/app/xinyi_pos_app.dart';

void main() {
  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
  });

  Future<void> pumpTabletApp(WidgetTester tester) async {
    tester.view.physicalSize = const Size(1280, 800);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(const XinyiPosApp());
    await tester.pumpAndSettle();
  }

  Future<void> loginWithDemoPin(WidgetTester tester) async {
    expect(find.byKey(const ValueKey('pin-login-screen')), findsOneWidget);

    for (final digit in <String>['1', '2', '3', '4']) {
      await tester.tap(find.byKey(ValueKey('pin-digit-$digit')));
      await tester.pump();
    }

    await tester.tap(find.byKey(const ValueKey('pin-submit-button')));
    await tester.pumpAndSettle();
  }

  testWidgets('shows Android POS terminal shell', (tester) async {
    await pumpTabletApp(tester);
    await loginWithDemoPin(tester);

    expect(find.text('Xinyi Flagship Store'), findsOneWidget);
    expect(find.text('Android Tablet POS · Demo Terminal 01'), findsOneWidget);
    expect(find.text('Demo Cashier'), findsOneWidget);
    expect(find.text('線上'), findsOneWidget);
    expect(find.text('待同步 0'), findsOneWidget);
    expect(find.text('現金結帳'), findsOneWidget);
  });

  testWidgets('adds product to cart and updates total', (tester) async {
    await pumpTabletApp(tester);
    await loginWithDemoPin(tester);

    await tester.tap(
      find.byKey(const ValueKey('product-tile-demo-americano-12oz')),
    );
    await tester.pump();

    expect(find.text('尚未加入商品'), findsNothing);
    expect(find.text('x 1'), findsOneWidget);
    expect(find.text('\$90'), findsWidgets);

    final checkoutButton = tester.widget<FilledButton>(
      find.byKey(const ValueKey('cash-checkout-button')),
    );
    expect(checkoutButton.onPressed, isNotNull);
  });

  testWidgets('increments duplicate product and clears the cart', (
    tester,
  ) async {
    await pumpTabletApp(tester);
    await loginWithDemoPin(tester);

    await tester.tap(
      find.byKey(const ValueKey('product-tile-demo-latte-12oz')),
    );
    await tester.pump();
    await tester.tap(
      find.byKey(const ValueKey('product-tile-demo-latte-12oz')),
    );
    await tester.pump();

    expect(find.text('x 2'), findsOneWidget);
    expect(find.text('咖啡滿 200 享 9 折'), findsOneWidget);
    expect(find.text('-\$24'), findsOneWidget);
    expect(find.text('\$216'), findsOneWidget);

    await tester.tap(find.byKey(const ValueKey('clear-cart-button')));
    await tester.pump();

    expect(find.text('尚未加入商品'), findsOneWidget);
    expect(find.text('x 2'), findsNothing);

    final checkoutButton = tester.widget<FilledButton>(
      find.byKey(const ValueKey('cash-checkout-button')),
    );
    expect(checkoutButton.onPressed, isNull);
  });

  testWidgets('adjusts cart quantities with touch controls', (tester) async {
    await pumpTabletApp(tester);
    await loginWithDemoPin(tester);

    await tester.tap(
      find.byKey(const ValueKey('product-tile-demo-americano-12oz')),
    );
    await tester.pump();

    await tester.tap(
      find.byKey(const ValueKey('increase-cart-demo-americano-12oz')),
    );
    await tester.pump();

    expect(find.text('x 2'), findsOneWidget);
    expect(find.text('\$180'), findsWidgets);

    await tester.tap(
      find.byKey(const ValueKey('decrease-cart-demo-americano-12oz')),
    );
    await tester.pump();

    expect(find.text('x 1'), findsOneWidget);
    expect(find.text('\$90'), findsWidgets);

    await tester.tap(
      find.byKey(const ValueKey('remove-cart-demo-americano-12oz')),
    );
    await tester.pump();

    expect(find.text('尚未加入商品'), findsOneWidget);
  });

  testWidgets('locks terminal and returns to PIN login', (tester) async {
    await pumpTabletApp(tester);
    await loginWithDemoPin(tester);

    await tester.tap(
      find.byKey(const ValueKey('header-logout-terminal-button')),
    );
    await tester.pumpAndSettle();

    expect(find.byKey(const ValueKey('pin-login-screen')), findsOneWidget);
    expect(find.text('Xinyi Flagship Store'), findsOneWidget);
  });
}
