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


export interface ParsedSseEvent {
  event?: string;
  data?: string;
}

/**
 * Incremental Server-Sent Events parser: keeps the trailing incomplete line between
 * chunks and dispatches an event only on its terminating blank line.
 */
export class SseParser {
  private buffer = '';
  private eventType?: string;
  private dataLines: string[] = [];

  push(chunk: string): ParsedSseEvent[] {
    this.buffer += chunk;
    const events: ParsedSseEvent[] = [];
    let newline: number;
    while ((newline = this.buffer.indexOf('\n')) >= 0) {
      const line = this.buffer.substring(0, newline).replace(/\r$/, '');
      this.buffer = this.buffer.substring(newline + 1);
      this.processLine(line, events);
    }
    return events;
  }

  /** Dispatches a pending event when the stream ends without a final blank line. */
  flush(): ParsedSseEvent[] {
    const events: ParsedSseEvent[] = [];
    if (this.buffer.length > 0) {
      this.processLine(this.buffer.replace(/\r$/, ''), events);
      this.buffer = '';
    }
    this.processLine('', events);
    return events;
  }

  private processLine(line: string, events: ParsedSseEvent[]): void {
    if (line === '') {
      if (this.dataLines.length > 0) {
        events.push({ event: this.eventType, data: this.dataLines.join('\n') });
      }
      this.eventType = undefined;
      this.dataLines = [];
    } else if (line.startsWith(':')) {
      // comment / keep-alive
    } else if (line.startsWith('event:')) {
      this.eventType = line.substring(6).trim();
    } else if (line.startsWith('data:')) {
      const value = line.substring(5);
      this.dataLines.push(value.startsWith(' ') ? value.substring(1) : value);
    }
  }
}
