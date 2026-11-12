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

const MARGIN = 0;
const WINDOW_PROPS = ['position', 'margin', 'left', 'top', 'width', 'height', 'max-width', 'max-height', 'transform'];
const CARD_PROPS = ['max-width', 'max-height', 'width', 'height'];

/**
 * Makes a Nebular window draggable by its header and resizable from its bottom-right corner
 * (Nebular only offers minimize/maximize/full-screen), from any edge or corner. The window is switched to fixed
 * positioning on the first interaction and always kept inside the viewport.
 */
export interface FloatingWindow {
  dispose: () => void;
  /** Gives the layout back to Nebular and forgets the floating geometry */
  reset: () => void;
  /** Gives the layout back to Nebular but remembers the floating geometry (window minimized) */
  suspend: () => void;
  /** Re-applies the remembered floating geometry */
  resume: () => void;
}

export function makeWindowFloating(host: HTMLElement): FloatingWindow {
  const windowEl = host.closest('nb-window') as HTMLElement | null;
  const header = windowEl?.querySelector('nb-card-header') as HTMLElement | null;
  const card = windowEl?.querySelector('nb-card') as HTMLElement | null;
  if (!windowEl || !header || !card) return { dispose: () => {}, reset: () => {}, suspend: () => {}, resume: () => {} };

  header.style.cursor = 'move';
  header.style.userSelect = 'none';
  card.classList.add('floating-resizable');

  const px = (value: string) => parseFloat(value) || 0;

  const clamp = () => {
    const rect = windowEl.getBoundingClientRect();
    const w = Math.min(rect.width, window.innerWidth - 2 * MARGIN);
    const h = Math.min(rect.height, window.innerHeight - 2 * MARGIN);
    const left = Math.min(Math.max(px(windowEl.style.left), MARGIN), window.innerWidth - w - MARGIN);
    const top = Math.min(Math.max(px(windowEl.style.top), MARGIN), window.innerHeight - h - MARGIN);
    windowEl.style.width = w + 'px';
    windowEl.style.height = h + 'px';
    windowEl.style.left = left + 'px';
    windowEl.style.top = top + 'px';
    windowEl.style.maxWidth = window.innerWidth - left - MARGIN + 'px';
    windowEl.style.maxHeight = window.innerHeight - top - MARGIN + 'px';
  };

  const detach = () => {
    if (windowEl.dataset['floating']) return;
    const rect = windowEl.getBoundingClientRect();
    windowEl.dataset['floating'] = 'true';
    windowEl.style.position = 'fixed';
    windowEl.style.setProperty('margin', '0', 'important');
    windowEl.style.transform = 'none';
    windowEl.style.left = rect.left + 'px';
    windowEl.style.top = rect.top + 'px';
    windowEl.style.width = rect.width + 'px';
    windowEl.style.height = rect.height + 'px';
    card.style.maxWidth = 'none';
    card.style.maxHeight = 'none';
    card.style.width = '100%';
    card.style.height = '100%';
    // An ancestor with a transform would make "fixed" relative to itself: compensate
    const after = windowEl.getBoundingClientRect();
    windowEl.style.left = rect.left + (rect.left - after.left) + 'px';
    windowEl.style.top = rect.top + (rect.top - after.top) + 'px';
    clamp();
  };

  const STORAGE_KEY = 'tock-test-dialog-geometry';
  type Geometry = { left: string; top: string; width: string; height: string };
  let saved: Geometry | null = null;
  try {
    saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null');
  } catch {
    saved = null;
  }

  // Remembers the current floating geometry (also across window closes and page reloads)
  const commit = () => {
    clamp();
    saved = { left: windowEl.style.left, top: windowEl.style.top, width: windowEl.style.width, height: windowEl.style.height };
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(saved));
    } catch {
      // storage unavailable: the geometry is only kept in memory
    }
  };

  // Gives the layout back to Nebular (minimized / full screen) but keeps the geometry
  const suspend = () => release();
  const reset = () => release();

  const resume = () => {
    if (!saved) return;
    windowEl.dataset['floating'] = 'true';
    windowEl.style.position = 'fixed';
    windowEl.style.setProperty('margin', '0', 'important');
    windowEl.style.transform = 'none';
    windowEl.style.left = saved.left;
    windowEl.style.top = saved.top;
    windowEl.style.width = saved.width;
    windowEl.style.height = saved.height;
    card.style.maxWidth = 'none';
    card.style.maxHeight = 'none';
    card.style.width = '100%';
    card.style.height = '100%';
    clamp();
  };

  const release = () => {
    delete windowEl.dataset['floating'];
    WINDOW_PROPS.forEach((p) => windowEl.style.removeProperty(p));
    CARD_PROPS.forEach((p) => card.style.removeProperty(p));
  };

  const onHeaderMouseDown = (down: MouseEvent) => {
    if ((down.target as HTMLElement).closest('button, a, nb-icon')) return;
    detach();
    const startX = down.clientX;
    const startY = down.clientY;
    const startLeft = px(windowEl.style.left);
    const startTop = px(windowEl.style.top);
    const move = (e: MouseEvent) => {
      const rect = windowEl.getBoundingClientRect();
      const maxLeft = Math.max(MARGIN, window.innerWidth - rect.width - MARGIN);
      const maxTop = Math.max(MARGIN, window.innerHeight - rect.height - MARGIN);
      windowEl.style.left = Math.min(Math.max(startLeft + e.clientX - startX, MARGIN), maxLeft) + 'px';
      windowEl.style.top = Math.min(Math.max(startTop + e.clientY - startY, MARGIN), maxTop) + 'px';
    };
    const up = () => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', up);
      commit();
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', up);
    down.preventDefault();
  };

  const MIN_WIDTH = 320;
  const MIN_HEIGHT = 240;
  const EDGE = 6;
  const CORNER = 14;

  if (getComputedStyle(card).position === 'static') card.style.position = 'relative';

  const startResize = (dir: string, down: MouseEvent) => {
    detach();
    const startX = down.clientX;
    const startY = down.clientY;
    const rect = windowEl.getBoundingClientRect();
    const start = { left: px(windowEl.style.left), top: px(windowEl.style.top), width: rect.width, height: rect.height };
    windowEl.style.maxWidth = 'none';
    windowEl.style.maxHeight = 'none';
    const move = (e: MouseEvent) => {
      const dx = e.clientX - startX;
      const dy = e.clientY - startY;
      let { left, top, width, height } = start;
      if (dir.includes('e')) width = Math.min(Math.max(start.width + dx, MIN_WIDTH), window.innerWidth - start.left - MARGIN);
      if (dir.includes('s')) height = Math.min(Math.max(start.height + dy, MIN_HEIGHT), window.innerHeight - start.top - MARGIN);
      if (dir.includes('w')) {
        const right = start.left + start.width;
        left = Math.min(Math.max(start.left + dx, MARGIN), right - MIN_WIDTH);
        width = right - left;
      }
      if (dir.includes('n')) {
        const bottom = start.top + start.height;
        top = Math.min(Math.max(start.top + dy, MARGIN), bottom - MIN_HEIGHT);
        height = bottom - top;
      }
      windowEl.style.left = left + 'px';
      windowEl.style.top = top + 'px';
      windowEl.style.width = width + 'px';
      windowEl.style.height = height + 'px';
    };
    const up = () => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', up);
      commit();
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', up);
    down.preventDefault();
    down.stopPropagation();
  };

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

  header.addEventListener('mousedown', onHeaderMouseDown);
  return {
    reset,
    suspend,
    resume,
    dispose: () => {
      header.removeEventListener('mousedown', onHeaderMouseDown);
      handles.forEach((h) => h.remove());
    }
  };
}
