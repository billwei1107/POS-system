/*
 * @file xinyi_pos_app.dart
 * @description POS App 根組件 / POS app root widget
 * @description_en Defines app-level theme, routing shell, and first screen.
 * @description_zh 定義應用層級主題、路由外殼與第一個畫面。
 */
import 'package:flutter/material.dart';

import '../core/repositories/auth_repository.dart';
import '../core/repositories/demo_auth_repository.dart';
import '../features/auth/screens/pin_login_screen.dart';
import '../features/auth/state/pos_session_state.dart';
import '../features/pos_terminal/screens/pos_terminal_screen.dart';

class XinyiPosApp extends StatefulWidget {
  const XinyiPosApp({
    super.key,
    this.authRepository = const DemoAuthRepository(),
  });

  final AuthRepository authRepository;

  @override
  State<XinyiPosApp> createState() => _XinyiPosAppState();
}

class _XinyiPosAppState extends State<XinyiPosApp> {
  PosSessionState _sessionState = PosSessionState.signedOut();

  void _logout() {
    setState(() => _sessionState = _sessionState.logout());
  }

  @override
  Widget build(BuildContext context) {
    final session = _sessionState.session;

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
      home: session == null
          ? PinLoginScreen(
              authRepository: widget.authRepository,
              onSignedIn: (nextSession) {
                setState(
                  () => _sessionState = PosSessionState.signedIn(nextSession),
                );
              },
            )
          : PosTerminalScreen(session: session, onLogout: _logout),
    );
  }
}
