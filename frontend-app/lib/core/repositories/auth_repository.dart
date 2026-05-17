/*
 * @file auth_repository.dart
 * @description 認證 Repository 介面 / Auth repository interface
 * @description_en Defines the POS PIN login contract before real API integration.
 * @description_zh 在真實 API 串接前定義 POS PIN 登入資料來源合約。
 */
import '../models/pin_login_request.dart';
import '../models/pos_session.dart';

abstract interface class AuthRepository {
  Future<PosSession> loginWithPin(PinLoginRequest request);
}
