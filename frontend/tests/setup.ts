import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import type { ReactNode } from 'react';
import { afterEach, vi } from 'vitest';

afterEach(() => cleanup());

vi.mock('react-i18next', () => ({
  Trans: ({ children, i18nKey }: { children?: ReactNode; i18nKey: string }) => children ?? i18nKey,
  useTranslation: () => ({
    i18n: {
      language: 'en',
      resolvedLanguage: 'en',
    },
    t: (key: string) => key,
  }),
}));
