/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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
 * Display mode which allow components to adapt themselves to available display space variations
 *
 * Modes:
 * - FULL_WIDTH:               Take entire screen when screen is wide
 * - RESPONSIVE_FULL_WIDTH:    Take entire screen when screen is not wide
 * - DOCKED_PANEL:             There is a panel to the right
 * - RESPONSIVE_DOCKED_PANEL:  There is a panel to the right when screen is not wide
 */
export type ViewMode = 'FULL_WIDTH' | 'DOCKED_PANEL' | 'RESPONSIVE_FULL_WIDTH' | 'RESPONSIVE_DOCKED_PANEL';

export function toggleSmallScreenMode(mode: ViewMode): ViewMode {
  if (mode === 'FULL_WIDTH') {
    return 'RESPONSIVE_FULL_WIDTH';
  } else if (mode === 'DOCKED_PANEL') {
    return 'RESPONSIVE_DOCKED_PANEL';
  } else {
    return mode;
  }
}

export function toggleWideScreenMode(mode: ViewMode): ViewMode {
  if (mode === 'RESPONSIVE_FULL_WIDTH') {
    return 'FULL_WIDTH';
  } else if (mode === 'RESPONSIVE_DOCKED_PANEL') {
    return 'DOCKED_PANEL';
  } else {
    return mode;
  }
}

export function dock(mode: ViewMode): ViewMode {
  if (mode === 'FULL_WIDTH') {
    return 'DOCKED_PANEL';
  } else if (mode === 'RESPONSIVE_FULL_WIDTH') {
    return 'RESPONSIVE_DOCKED_PANEL';
  } else {
    return mode;
  }
}

export function undock(mode: ViewMode): ViewMode {
  if (mode === 'DOCKED_PANEL') {
    return 'FULL_WIDTH';
  } else if (mode === 'RESPONSIVE_DOCKED_PANEL') {
    return 'RESPONSIVE_FULL_WIDTH';
  } else {
    return mode;
  }
}

export function isDocked(mode: ViewMode): boolean {
  return (mode === 'DOCKED_PANEL') || (mode === 'RESPONSIVE_DOCKED_PANEL');
}

export function isDockedOrSmall(mode: ViewMode): boolean {
  return (mode === 'DOCKED_PANEL') ||
    (mode === 'RESPONSIVE_DOCKED_PANEL') ||
    (mode === 'RESPONSIVE_FULL_WIDTH');
}
