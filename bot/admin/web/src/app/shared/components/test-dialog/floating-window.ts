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

/**
 * Makes a Nebular window draggable by its header and resizable from its bottom-right corner
 * (Nebular only offers minimize/maximize/full-screen). The window is switched to fixed
 * positioning on the first interaction so it can be freely placed on the screen.
 */
export function makeWindowFloating(host: HTMLElement): { dispose: () => void; reset: () => void } {
  const windowEl = host.closest('nb-window') as HTMLElement | null;
  const header = windowEl?.querySelector('nb-card-header') as HTMLElement | null;
  const card = windowEl?.querySelector('nb-card') as HTMLElement | null;
  if (!windowEl || !header || !card) return { dispose: () => {}, reset: () => {} };

  header.style.cursor = 'move';
  header.style.userSelect = 'none';
  card.classList.add('floating-resizable');

  const detach = () => {
    const rect = windowEl.getBoundingClientRect();
    if (windowEl.dataset['floating']) return;
    windowEl.dataset['floating'] = 'true';
    windowEl.style.position = 'fixed';
    windowEl.style.margin = '0';
    windowEl.style.left = rect.left + 'px';
    windowEl.style.top = rect.top + 'px';
    windowEl.style.width = rect.width + 'px';
    windowEl.style.height = rect.height + 'px';
    windowEl.style.maxWidth = 'none';
    card.style.maxWidth = 'none';
    card.style.width = '100%';
    card.style.height = '100%';
  };

  // Give the layout back to Nebular (needed when its state changes: minimize, maximize, full screen)
  const reset = () => {
    delete windowEl.dataset['floating'];
    ['position', 'margin', 'left', 'top', 'width', 'height', 'maxWidth'].forEach((p) => windowEl.style.removeProperty(p.replace(/[A-Z]/g, (c) => '-' + c.toLowerCase())));
    ['maxWidth', 'width', 'height'].forEach((p) => card.style.removeProperty(p.replace(/[A-Z]/g, (c) => '-' + c.toLowerCase())));
  };

  const onMouseDown = (down: MouseEvent) => {
    if ((down.target as HTMLElement).closest('button, a, nb-icon')) return;
    detach();
    const startX = down.clientX;
    const startY = down.clientY;
    const startLeft = windowEl.offsetLeft;
    const startTop = windowEl.offsetTop;
    const move = (e: MouseEvent) => {
      const left = Math.min(Math.max(startLeft + e.clientX - startX, 0 - windowEl.offsetWidth + 80), window.innerWidth - 80);
      const top = Math.min(Math.max(startTop + e.clientY - startY, 0), window.innerHeight - 40);
      windowEl.style.left = left + 'px';
      windowEl.style.top = top + 'px';
    };
    const up = () => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', up);
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', up);
    down.preventDefault();
  };

  // Native CSS resize on the card: fix the window to fixed positioning before the first resize
  const onCardMouseDown = (e: MouseEvent) => {
    const r = card.getBoundingClientRect();
    const inCorner = e.clientX > r.right - 20 && e.clientY > r.bottom - 20;
    if (!inCorner) return;
    detach();
    const sync = new ResizeObserver(() => {
      windowEl.style.width = card.offsetWidth + 'px';
      windowEl.style.height = card.offsetHeight + 'px';
    });
    sync.observe(card);
    const up = () => {
      sync.disconnect();
      document.removeEventListener('mouseup', up);
    };
    document.addEventListener('mouseup', up);
  };

  header.addEventListener('mousedown', onMouseDown);
  card.addEventListener('mousedown', onCardMouseDown);
  return {
    reset,
    dispose: () => {
      header.removeEventListener('mousedown', onMouseDown);
      card.removeEventListener('mousedown', onCardMouseDown);
    }
  };
}
