import { describe, expect, it } from 'vitest';
import { CLASSROOM_THEMES, isDistinctFocus, randomClassroomThemeFocus } from '../config/classroomThemes';

describe('classroom theme focus configuration', () => {
  it('keeps every configured focus distinct from its broad theme', () => {
    CLASSROOM_THEMES.forEach((theme) => {
      expect(theme.focuses.length).toBeGreaterThan(0);
      theme.focuses.forEach((focus) => {
        expect(isDistinctFocus(theme.name, focus), `${theme.name} / ${focus}`).toBe(true);
      });
    });
  });

  it('returns a broad theme with a non-repetitive focus', () => {
    for (let index = 0; index < 50; index += 1) {
      const selection = randomClassroomThemeFocus();
      expect(CLASSROOM_THEMES.map((theme) => theme.name)).toContain(selection.theme);
      expect(isDistinctFocus(selection.theme, selection.focus)).toBe(true);
    }
  });
});
