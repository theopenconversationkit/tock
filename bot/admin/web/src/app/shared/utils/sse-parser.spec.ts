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


import { SseParser } from './sse-parser';

describe('SseParser', () => {
  it('reassembles an event split across chunks at any position', () => {
    const stream = 'event: answer\ndata: {"a":"b\\nc"}\n\nevent: token\ndata: {"x":1}\n\n';
    for (let cut = 1; cut < stream.length; cut++) {
      const parser = new SseParser();
      const events = [...parser.push(stream.slice(0, cut)), ...parser.push(stream.slice(cut))];
      expect(events).toEqual([
        { event: 'answer', data: '{"a":"b\\nc"}' },
        { event: 'token', data: '{"x":1}' }
      ]);
    }
  });

  it('supports CRLF, comments and multi-line data', () => {
    const events = new SseParser().push(': ping\r\nevent: e\r\ndata: l1\r\ndata: l2\r\n\r\n');
    expect(events).toEqual([{ event: 'e', data: 'l1\nl2' }]);
  });

  it('flushes a final event without trailing blank line', () => {
    const parser = new SseParser();
    expect(parser.push('event: answer\ndata: {}')).toEqual([]);
    expect(parser.flush()).toEqual([{ event: 'answer', data: '{}' }]);
  });
});
