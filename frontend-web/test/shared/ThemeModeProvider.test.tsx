/**
 * @file ThemeModeProvider.test.tsx
 * @description 主題模式測試 / Theme mode tests
 * @description_en Verifies persistent dark and light mode switching
 * @description_zh 驗證深色與淺色模式切換會持久化
 */
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import { ThemeModeToggle } from '../../src/shared/components';
import { THEME_MODE_STORAGE_KEY } from '../../src/shared/theme';
import { ThemeModeProvider } from '../../src/shared/theme/ThemeModeProvider';

const renderThemeToggle = () => render(
  <ThemeModeProvider>
    <ThemeModeToggle />
  </ThemeModeProvider>
);

beforeEach(() => {
  window.localStorage.clear();
});

describe('ThemeModeProvider', () => {
  it('switches from dark mode to light mode and persists the choice', async () => {
    const user = userEvent.setup();
    renderThemeToggle();

    await user.click(screen.getByRole('button', { name: '切換淺色模式' }));

    expect(screen.getByRole('button', { name: '切換深色模式' })).toBeInTheDocument();
    expect(window.localStorage.getItem(THEME_MODE_STORAGE_KEY)).toBe('light');
  });

  it('loads the stored light mode preference', () => {
    window.localStorage.setItem(THEME_MODE_STORAGE_KEY, 'light');

    renderThemeToggle();

    expect(screen.getByRole('button', { name: '切換深色模式' })).toBeInTheDocument();
  });
});
