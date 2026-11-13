/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export type FloatingMode = 'normal' | 'maximized' | 'collapsed';

export interface FloatingWindow {
  /** normal: remembered geometry, maximized: fills the viewport, collapsed: rolled up to its header */
  setMode: (mode: FloatingMode) => void;
  dispose: () => void;
}

export interface FloatingOptions {
  /** Called when the user drags or resizes a maximized window, which then becomes a normal one */
  onLeaveMaximized?: () => void;
}

interface Box {
  left: number;
  top: number;
  width: number;
  height: number;
}

const MAXIMIZED_MARGIN = 16;
const MIN_WIDTH = 320;
const MIN_HEIGHT = 240;
const EDGE = 6;
const CORNER = 14;
const TRANSITION_MS = 200;
const STORAGE_KEY = 'tock-test-dialog-geometry';

/**
 * Makes a Nebular window free-floating: it owns the position and size of the window (Nebular's own
 * minimized / maximized / full-screen layouts are replaced by in-place modes), draggable by its header,
 * resizable from any edge or corner, always kept inside the viewport, and remembered across reloads.
 */
export function makeWindowFloating(host: HTMLElement, options: FloatingOptions = {}): FloatingWindow {
  const windowEl = host.closest('nb-window') as HTMLElement | null;
  const header = windowEl?.querySelector('nb-card-header') as HTMLElement | null;
  const card = windowEl?.querySelector('nb-card') as HTMLElement | null;
  if (!windowEl || !header || !card) return { setMode: () => {}, dispose: () => {} };

  const px = (value: string) => parseFloat(value) || 0;
  const vw = () => document.documentElement.clientWidth;
  const vh = () => document.documentElement.clientHeight;

  const defaultBox = (): Box => {
    const width = Math.min(Math.max(vw() * 0.55, MIN_WIDTH), vw());
    const height = Math.min(Math.max(vh() * 0.7, MIN_HEIGHT), vh());
    return { left: 16, top: vh() - height - 16, width, height };
  };

  const loadBox = (): Box => {
    try {
      const s = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null');
      if (s) return { left: px(s.left), top: px(s.top), width: px(s.width), height: px(s.height) };
    } catch {
      // unreadable storage: use the default geometry
    }
    return defaultBox();
  };

  const fit = (b: Box): Box => {
    const width = Math.min(Math.max(b.width, MIN_WIDTH), vw());
    const height = Math.min(Math.max(b.height, MIN_HEIGHT), vh());
    return {
      width,
      height,
      left: Math.min(Math.max(b.left, 0), vw() - width),
      top: Math.min(Math.max(b.top, 0), vh() - height)
    };
  };

  let box = fit(loadBox());
  let mode: FloatingMode = 'normal';
  let animationTimer: ReturnType<typeof setTimeout> | undefined;

  const persist = () => {
    try {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({ left: box.left + 'px', top: box.top + 'px', width: box.width + 'px', height: box.height + 'px' })
      );
    } catch {
      // storage unavailable: the geometry is only kept in memory
    }
  };

  const offset = { x: 0, y: 0 };
  let measured = false;

  const place = (b: Box) => {
    const s = windowEl.style;
    s.position = 'fixed';
    s.setProperty('margin', '0', 'important');
    s.transform = 'none';
    s.maxWidth = 'none';
    s.maxHeight = 'none';
    s.left = b.left + offset.x + 'px';
    s.top = b.top + offset.y + 'px';
    s.width = b.width + 'px';
    s.height = b.height + 'px';
    card.style.maxWidth = 'none';
    card.style.maxHeight = 'none';
    card.style.width = '100%';
    card.style.height = '100%';
    if (offset.x === 0 && offset.y === 0 && !measured) {
      // An ancestor with a transform would make "fixed" relative to itself: measure it once, outside any transition
      measured = true;
      const after = windowEl.getBoundingClientRect();
      offset.x = b.left - after.left;
      offset.y = b.top - after.top;
      s.left = b.left + offset.x + 'px';
      s.top = b.top + offset.y + 'px';
    }
  };

  const render = (animate: boolean) => {
    windowEl.dataset['floating'] = 'true';
    windowEl.dataset['mode'] = mode;
    header.style.cursor = 'move';
    clearTimeout(animationTimer);
    windowEl.style.transition = animate ? `left ${TRANSITION_MS}ms, top ${TRANSITION_MS}ms, width ${TRANSITION_MS}ms, height ${TRANSITION_MS}ms` : '';
    if (animate) animationTimer = setTimeout(() => (windowEl.style.transition = ''), TRANSITION_MS + 50);

    if (mode === 'maximized') {
      place({ left: MAXIMIZED_MARGIN, top: MAXIMIZED_MARGIN, width: vw() - 2 * MAXIMIZED_MARGIN, height: vh() - 2 * MAXIMIZED_MARGIN });
    } else if (mode === 'collapsed') {
      const height = header.getBoundingClientRect().height;
      const width = Math.min(box.width, vw());
      place({ left: Math.min(Math.max(box.left, 0), vw() - width), top: Math.min(Math.max(box.top, 0), vh() - height), width, height });
    } else {
      box = fit(box);
      place(box);
    }
  };

  const leaveMaximized = () => {
    if (mode !== 'maximized') return;
    box = { left: MAXIMIZED_MARGIN, top: MAXIMIZED_MARGIN, width: vw() - 2 * MAXIMIZED_MARGIN, height: vh() - 2 * MAXIMIZED_MARGIN };
    mode = 'normal';
    render(false);
    persist();
    options.onLeaveMaximized?.();
  };

  const startDrag = (down: MouseEvent) => {
    if ((down.target as HTMLElement).closest('button, a, nb-icon')) return;
    leaveMaximized();
    const startX = down.clientX;
    const startY = down.clientY;
    const startLeft = px(windowEl.style.left);
    const startTop = px(windowEl.style.top);
    const move = (e: MouseEvent) => {
      const rect = windowEl.getBoundingClientRect();
      windowEl.style.left = Math.min(Math.max(startLeft + e.clientX - startX, 0), Math.max(0, vw() - rect.width)) + 'px';
      windowEl.style.top = Math.min(Math.max(startTop + e.clientY - startY, 0), Math.max(0, vh() - rect.height)) + 'px';
    };
    const up = () => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', up);
      box = { ...box, left: px(windowEl.style.left) - offset.x, top: px(windowEl.style.top) - offset.y };
      persist();
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', up);
    down.preventDefault();
  };

  const startResize = (dir: string, down: MouseEvent) => {
    if (mode === 'collapsed') return;
    leaveMaximized();
    const startX = down.clientX;
    const startY = down.clientY;
    const start = { ...box };
    const move = (e: MouseEvent) => {
      const dx = e.clientX - startX;
      const dy = e.clientY - startY;
      let { left, top, width, height } = start;
      if (dir.includes('e')) width = Math.min(Math.max(start.width + dx, MIN_WIDTH), vw() - start.left);
      if (dir.includes('s')) height = Math.min(Math.max(start.height + dy, MIN_HEIGHT), vh() - start.top);
      if (dir.includes('w')) {
        const right = start.left + start.width;
        left = Math.min(Math.max(start.left + dx, 0), right - MIN_WIDTH);
        width = right - left;
      }
      if (dir.includes('n')) {
        const bottom = start.top + start.height;
        top = Math.min(Math.max(start.top + dy, 0), bottom - MIN_HEIGHT);
        height = bottom - top;
      }
      windowEl.style.left = left + offset.x + 'px';
      windowEl.style.top = top + offset.y + 'px';
      windowEl.style.width = width + 'px';
      windowEl.style.height = height + 'px';
    };
    const up = () => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', up);
      box = { left: px(windowEl.style.left) - offset.x, top: px(windowEl.style.top) - offset.y, width: px(windowEl.style.width), height: px(windowEl.style.height) };
      persist();
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', up);
    down.preventDefault();
    down.stopPropagation();
  };

  card.classList.add('floating-resizable');
  if (getComputedStyle(card).position === 'static') card.style.position = 'relative';

  const cursors: Record<string, string> = {
    n: 'ns-resize', s: 'ns-resize', e: 'ew-resize', w: 'ew-resize',
    ne: 'nesw-resize', sw: 'nesw-resize', nw: 'nwse-resize', se: 'nwse-resize'
  };
  const handles = Object.keys(cursors).map((dir) => {
    const el = document.createElement('div');
    el.className = 'floating-resize-handle';
    const corner = dir.length === 2;
    const size = corner ? CORNER : EDGE;
    const st = el.style;
    st.position = 'absolute';
    st.zIndex = '10';
    st.cursor = cursors[dir];
    if (dir.includes('n')) st.top = '0';
    if (dir.includes('s')) st.bottom = '0';
    if (dir.includes('w')) st.left = '0';
    if (dir.includes('e')) st.right = '0';
    st.width = dir.includes('e') || dir.includes('w') ? size + 'px' : `calc(100% - ${2 * CORNER}px)`;
    st.height = dir.includes('n') || dir.includes('s') ? size + 'px' : `calc(100% - ${2 * CORNER}px)`;
    if (!corner && (dir === 'n' || dir === 's')) st.left = CORNER + 'px';
    if (!corner && (dir === 'e' || dir === 'w')) st.top = CORNER + 'px';
    el.addEventListener('mousedown', (e) => startResize(dir, e));
    card.appendChild(el);
    return el;
  });

  const onViewportResize = () => render(false);
  header.addEventListener('mousedown', startDrag);
  window.addEventListener('resize', onViewportResize);
  render(false);

  return {
    setMode: (next) => {
      mode = next;
      render(true);
    },
    dispose: () => {
      clearTimeout(animationTimer);
      header.removeEventListener('mousedown', startDrag);
      window.removeEventListener('resize', onViewportResize);
      handles.forEach((h) => h.remove());
    }
  };
}
