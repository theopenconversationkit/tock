// resilient-date.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

@Pipe({ name: 'resilientDate' })
export class ResilientDatePipe implements PipeTransform {
  constructor(private datePipe: DatePipe) {}

  transform(value: string | Date, format: string = 'y/MM/dd HH:mm'): string {
    if (!value) return '';
    if (value instanceof Date) {
      return this.datePipe.transform(value, format) || '';
    }
    if (typeof value !== 'string') return '';

    let date: Date | null = null;

    // 1. Try to parse as ISO (e.g., 2026-02-10T16:13:08 or 2026-02-10T16:13:08Z)
    try {
      date = new Date(value);
      if (!isNaN(date.getTime())) {
        return this.datePipe.transform(date, format) || '';
      }
    } catch (e) {
      // Skip and move on to the next step
    }

    // 2. Try parsing as a custom format: 2026-02-10_16h13m08
    const customRegex = /^(\d{4})-(\d{2})-(\d{2})_(\d{2})h(\d{2})m(\d{2})$/;
    const customMatch = value.match(customRegex);
    if (customMatch) {
      const [, year, month, day, hours, minutes, seconds] = customMatch;
      date = new Date(
        parseInt(year),
        parseInt(month) - 1, // Month 0-indexed
        parseInt(day),
        parseInt(hours),
        parseInt(minutes),
        parseInt(seconds)
      );
      if (!isNaN(date.getTime())) {
        return this.datePipe.transform(date, format) || '';
      }
    }

    // 3. Fallback: returns the original value (to prevent errors)
    console.warn(`[ResilientDatePipe] Format not recognized : ${value}`);
    return value;
  }
}
