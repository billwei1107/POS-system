/*
 * @file pos_session_state.dart
 * @description POS 工作階段狀態 / POS session state
 * @description_en Keeps login, logout, and session freshness testable outside widgets.
 * @description_zh 將登入、登出與工作階段有效性集中管理，方便脫離 Widget 測試。
 */
import '../../../core/models/pin_login_request.dart';
import '../../../core/models/pos_session.dart';
import '../../../core/repositories/auth_repository.dart';

class PosSessionState {
  const PosSessionState._({
    required this.session,
    required this.isLoading,
    required this.errorCode,
  });

  factory PosSessionState.signedOut() {
    return const PosSessionState._(
      session: null,
      isLoading: false,
      errorCode: null,
    );
  }

  factory PosSessionState.signedIn(PosSession session) {
    return PosSessionState._(
      session: session,
      isLoading: false,
      errorCode: null,
    );
  }

  final PosSession? session;
  final bool isLoading;
  final String? errorCode;

  bool get isSignedIn => session != null;

  bool isActive(DateTime now) {
    final currentSession = session;
    return currentSession != null && !currentSession.isExpired(now);
  }

  // ========================================
  // PIN 登入 / PIN Login
  // ========================================
  Future<PosSessionState> loginWithPin({
    required AuthRepository repository,
    required PinLoginRequest request,
  }) async {
    try {
      final nextSession = await repository.loginWithPin(request);

      return PosSessionState._(
        session: nextSession,
        isLoading: false,
        errorCode: null,
      );
    } on Exception catch (error) {
      return PosSessionState._(
        session: null,
        isLoading: false,
        errorCode: error.toString(),
      );
    }
  }

  // ========================================
  // 登出 / Logout
  // ========================================
  PosSessionState logout() {
    if (session == null && errorCode == null) {
      return this;
    }

    return PosSessionState.signedOut();
  }
}
