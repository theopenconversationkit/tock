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

/** Whole days elapsed since an ISO date. Negative values are clamped to zero. */
export function daysSince(isoDate: string): number {
  const elapsed = Date.now() - new Date(isoDate).getTime();
  return Math.max(0, Math.floor(elapsed / 86400000));
}

/**
 * Builds the polyline and polygon point lists of a sparkline.
 * Kept out of the component so it stays trivially testable.
 */
export function buildSparklinePath(
  values: number[],
  width: number,
  height: number,
  padding: number = 4
): { line: string; area: string } {
  if (!values.length) {
    return { line: '', area: '' };
  }

  const max = Math.max(...values) * 1.15 || 1;
  const step = values.length > 1 ? (width - padding * 2) / (values.length - 1) : 0;

  const points = values.map((value, index) => {
    const x = padding + index * step;
    const y = height - padding - (value / max) * (height - padding * 2);
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  });

  const line = points.join(' ');
  const area = `${padding},${height - padding} ${line} ${(width - padding).toFixed(1)},${height - padding}`;

  return { line, area };
}
