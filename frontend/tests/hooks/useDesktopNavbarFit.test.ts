import { describe, expect, it } from 'vitest';

import { navbarSectionsFit } from '../../src/hooks/useDesktopNavbarFit';

describe('navbarSectionsFit', () => {
  it('accepts separated sections with a centered brand', () => {
    expect(
      navbarSectionsFit(
        { left: 0, right: 1000 },
        { left: 32, right: 390 },
        { left: 470, right: 530 },
        { left: 610, right: 968 }
      )
    ).toBe(true);
  });

  it('rejects translated content that gets too close to the brand', () => {
    expect(
      navbarSectionsFit(
        { left: 0, right: 1000 },
        { left: 32, right: 455 },
        { left: 470, right: 530 },
        { left: 610, right: 968 }
      )
    ).toBe(false);
  });

  it('rejects a layout where the brand is pushed off center', () => {
    expect(
      navbarSectionsFit(
        { left: 0, right: 1000 },
        { left: 32, right: 390 },
        { left: 450, right: 510 },
        { left: 610, right: 968 }
      )
    ).toBe(false);
  });
});
