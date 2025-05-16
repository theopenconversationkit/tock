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

export const timeRanges = [
  {
    duration: 0,
    label: 'Today',
    tooltipLabel: 'today'
  },
  {
    duration: 1,
    offset: -1,
    label: 'Yesterday',
    tooltipLabel: 'yesterday'
  },
  {
    duration: 6,
    label: '7 days',
    tooltipLabel: 'the last 7 days'
  },
  {
    duration: 13,
    label: '14 days',
    tooltipLabel: 'the last 14 days'
  },
  {
    duration: 29,
    label: '30 days',
    tooltipLabel: 'the last 30 days'
  },
  {
    duration: 89,
    label: '90 days',
    tooltipLabel: 'the last 90 days'
  },
  {
    duration: 179,
    label: '180 days',
    tooltipLabel: 'the last 180 days'
  }
];
