/*
 * @file xinyi_pos_app.dart
 * @description POS App 根組件 / POS app root widget
 * @description_en Defines app-level theme, routing shell, and first screen.
 * @description_zh 定義應用層級主題、路由外殼與第一個畫面。
 */
import 'package:flutter/material.dart';

import '../features/pos_terminal/screens/pos_terminal_screen.dart';

class XinyiPosApp extends StatelessWidget {
  const XinyiPosApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Xinyi POS',
      theme: ThemeData(
        brightness: Brightness.dark,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFFF6B00),
          brightness: Brightness.dark,
        ),
        scaffoldBackgroundColor: const Color(0xFF11141B),
        useMaterial3: true,
      ),
      home: const PosTerminalScreen(),
    );
  }
}
