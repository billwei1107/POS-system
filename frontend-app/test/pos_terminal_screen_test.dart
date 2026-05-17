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
  }

  testWidgets('shows Android POS terminal shell', (tester) async {
    await pumpTabletApp(tester);

    expect(find.text('Xinyi Flagship Store'), findsOneWidget);
    expect(find.text('Android Tablet POS · Demo Terminal 01'), findsOneWidget);
    expect(find.text('線上'), findsOneWidget);
    expect(find.text('待同步 0'), findsOneWidget);
    expect(find.text('現金結帳'), findsOneWidget);
  });

  testWidgets('adds product to cart and updates total', (tester) async {
    await pumpTabletApp(tester);

    await tester.tap(find.text('美式咖啡 12oz'));
    await tester.pump();

    expect(find.text('尚未加入商品'), findsNothing);
    expect(find.text('x 1'), findsOneWidget);
    expect(find.text('\$90'), findsWidgets);

    final checkoutButton = tester.widget<FilledButton>(
      find.widgetWithText(FilledButton, '現金結帳'),
    );
    expect(checkoutButton.onPressed, isNotNull);
  });
}
