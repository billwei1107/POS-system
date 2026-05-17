/*
 * @file pin_login_screen.dart
 * @description PIN 登入畫面 / PIN login screen
 * @description_en Provides the Android tablet cashier PIN entry shell.
 * @description_zh 提供 Android 平板收銀員 PIN 輸入入口。
 */
import 'package:flutter/material.dart';

import '../../../core/models/pin_login_request.dart';
import '../../../core/models/pos_session.dart';
import '../../../core/repositories/auth_repository.dart';

class PinLoginScreen extends StatefulWidget {
  const PinLoginScreen({
    super.key,
    required this.authRepository,
    required this.onSignedIn,
    this.terminalCode = 'T-001',
  });

  final AuthRepository authRepository;
  final ValueChanged<PosSession> onSignedIn;
  final String terminalCode;

  @override
  State<PinLoginScreen> createState() => _PinLoginScreenState();
}

class _PinLoginScreenState extends State<PinLoginScreen> {
  static const int _maxPinLength = 6;

  String _pin = '';
  String? _errorMessage;
  bool _isSubmitting = false;

  bool get _canSubmit => _pin.length >= 4 && !_isSubmitting;

  // ========================================
  // PIN 輸入 / PIN Entry
  // ========================================
  void _appendDigit(String digit) {
    if (_pin.length >= _maxPinLength || _isSubmitting) {
      return;
    }

    setState(() {
      _pin = '$_pin$digit';
      _errorMessage = null;
    });
  }

  void _deleteDigit() {
    if (_pin.isEmpty || _isSubmitting) {
      return;
    }

    setState(() {
      _pin = _pin.substring(0, _pin.length - 1);
      _errorMessage = null;
    });
  }

  // ========================================
  // 登入提交 / Login Submit
  // ========================================
  Future<void> _submitPin() async {
    if (!_canSubmit) {
      return;
    }

    setState(() {
      _isSubmitting = true;
      _errorMessage = null;
    });

    try {
      final session = await widget.authRepository.loginWithPin(
        PinLoginRequest(pin: _pin, terminalCode: widget.terminalCode),
      );

      if (!mounted) {
        return;
      }

      widget.onSignedIn(session);
    } on Exception {
      if (!mounted) {
        return;
      }

      setState(() {
        _pin = '';
        _isSubmitting = false;
        _errorMessage = 'PIN 驗證失敗';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            return SingleChildScrollView(
              child: ConstrainedBox(
                constraints: BoxConstraints(minHeight: constraints.maxHeight),
                child: Center(
                  child: ConstrainedBox(
                    constraints: const BoxConstraints(maxWidth: 460),
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        key: const ValueKey('pin-login-screen'),
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          const Icon(
                            Icons.point_of_sale,
                            color: Color(0xFFFF7A1A),
                            size: 42,
                          ),
                          const SizedBox(height: 14),
                          const Text(
                            'Xinyi Flagship Store',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontSize: 26,
                              fontWeight: FontWeight.w900,
                            ),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            'Android Tablet POS · ${widget.terminalCode}',
                            textAlign: TextAlign.center,
                            style: const TextStyle(
                              color: Color(0xFFAEB2C3),
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          const SizedBox(height: 22),
                          _PinDisplay(
                            length: _pin.length,
                            hasError: _errorMessage != null,
                          ),
                          const SizedBox(height: 10),
                          AnimatedSwitcher(
                            duration: const Duration(milliseconds: 160),
                            child: _errorMessage == null
                                ? const SizedBox(height: 22)
                                : Text(
                                    _errorMessage!,
                                    key: const ValueKey('pin-error-message'),
                                    textAlign: TextAlign.center,
                                    style: const TextStyle(
                                      color: Color(0xFFFF8A80),
                                      fontWeight: FontWeight.w800,
                                    ),
                                  ),
                          ),
                          const SizedBox(height: 14),
                          _NumberPad(
                            onDigit: _appendDigit,
                            onDelete: _deleteDigit,
                            onSubmit: _submitPin,
                            canSubmit: _canSubmit,
                            isSubmitting: _isSubmitting,
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}

class _PinDisplay extends StatelessWidget {
  const _PinDisplay({required this.length, required this.hasError});

  final int length;
  final bool hasError;

  @override
  Widget build(BuildContext context) {
    final color = hasError ? const Color(0xFFFF8A80) : const Color(0xFFA9BEFF);

    return Container(
      height: 64,
      decoration: BoxDecoration(
        color: const Color(0xFF242736),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: hasError ? color : const Color(0xFF383C4D)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: List.generate(6, (index) {
          final filled = index < length;
          return AnimatedContainer(
            duration: const Duration(milliseconds: 120),
            width: 14,
            height: 14,
            margin: const EdgeInsets.symmetric(horizontal: 8),
            decoration: BoxDecoration(
              color: filled ? color : Colors.transparent,
              shape: BoxShape.circle,
              border: Border.all(color: color),
            ),
          );
        }),
      ),
    );
  }
}

class _NumberPad extends StatelessWidget {
  const _NumberPad({
    required this.onDigit,
    required this.onDelete,
    required this.onSubmit,
    required this.canSubmit,
    required this.isSubmitting,
  });

  final ValueChanged<String> onDigit;
  final VoidCallback onDelete;
  final VoidCallback onSubmit;
  final bool canSubmit;
  final bool isSubmitting;

  @override
  Widget build(BuildContext context) {
    const digits = <String>['1', '2', '3', '4', '5', '6', '7', '8', '9'];

    return Column(
      children: [
        GridView.count(
          crossAxisCount: 3,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          mainAxisSpacing: 10,
          crossAxisSpacing: 10,
          childAspectRatio: 2.2,
          children: [
            for (final digit in digits)
              _PadButton(
                key: ValueKey('pin-digit-$digit'),
                label: digit,
                onPressed: () => onDigit(digit),
              ),
            _IconPadButton(
              key: const ValueKey('pin-delete-button'),
              icon: Icons.backspace_outlined,
              tooltip: '刪除',
              onPressed: onDelete,
            ),
            _PadButton(
              key: const ValueKey('pin-digit-0'),
              label: '0',
              onPressed: () => onDigit('0'),
            ),
            _IconPadButton(
              key: const ValueKey('pin-submit-button'),
              icon: isSubmitting ? Icons.hourglass_top : Icons.check,
              tooltip: '登入',
              onPressed: canSubmit ? onSubmit : null,
              isPrimary: true,
            ),
          ],
        ),
      ],
    );
  }
}

class _PadButton extends StatelessWidget {
  const _PadButton({super.key, required this.label, required this.onPressed});

  final String label;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return FilledButton.tonal(
      onPressed: onPressed,
      style: FilledButton.styleFrom(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      child: Text(
        label,
        style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w900),
      ),
    );
  }
}

class _IconPadButton extends StatelessWidget {
  const _IconPadButton({
    super.key,
    required this.icon,
    required this.tooltip,
    required this.onPressed,
    this.isPrimary = false,
  });

  final IconData icon;
  final String tooltip;
  final VoidCallback? onPressed;
  final bool isPrimary;

  @override
  Widget build(BuildContext context) {
    final style = FilledButton.styleFrom(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      backgroundColor: isPrimary ? const Color(0xFFFF6B00) : null,
      foregroundColor: isPrimary ? Colors.white : null,
    );

    return Tooltip(
      message: tooltip,
      child: FilledButton.tonal(
        onPressed: onPressed,
        style: style,
        child: Icon(icon, size: 28),
      ),
    );
  }
}
