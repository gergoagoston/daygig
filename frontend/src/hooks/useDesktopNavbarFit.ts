import { useLayoutEffect, useState, type RefObject } from 'react';

const NAVBAR_FIT_BUFFER = 24;

type HorizontalBounds = {
  left: number;
  right: number;
};

export function navbarSectionsFit(
  header: HorizontalBounds,
  start: HorizontalBounds,
  center: HorizontalBounds,
  end: HorizontalBounds
) {
  const headerCenter = (header.left + header.right) / 2;
  const centerSectionMiddle = (center.left + center.right) / 2;

  return (
    start.left >= header.left &&
    start.right + NAVBAR_FIT_BUFFER <= center.left &&
    center.right + NAVBAR_FIT_BUFFER <= end.left &&
    end.right <= header.right &&
    Math.abs(headerCenter - centerSectionMiddle) < 1
  );
}

function useDesktopNavbarFit(headerRef: RefObject<HTMLElement | null>) {
  const [fits, setFits] = useState(false);

  useLayoutEffect(() => {
    const header = headerRef.current;
    if (!header) {
      return;
    }

    const [start, center, end] = (['start', 'center', 'end'] as const).map((section) =>
      header.querySelector<HTMLElement>(`[data-navbar-section="${section}"]`)
    );
    if (!start || !center || !end) {
      return;
    }

    const measure = () => {
      setFits(
        navbarSectionsFit(
          header.getBoundingClientRect(),
          start.getBoundingClientRect(),
          center.getBoundingClientRect(),
          end.getBoundingClientRect()
        )
      );
    };

    const resizeObserver = new ResizeObserver(measure);
    [header, start, center, end].forEach((element) => resizeObserver.observe(element));

    const mutationObserver = new MutationObserver(measure);
    mutationObserver.observe(header, {
      characterData: true,
      childList: true,
      subtree: true,
    });

    measure();

    return () => {
      resizeObserver.disconnect();
      mutationObserver.disconnect();
    };
  }, [headerRef]);

  return fits;
}

export default useDesktopNavbarFit;
