import { Component, Input } from '@angular/core';

/**
 * Renders a chunk's metadata as a key/value tree.
 *
 * Ingestion metadata has no fixed shape: it carries whatever the ingestion
 * pipeline wrote (id, chunk, title, source, index_datetime, a nested rank
 * object, and anything a future pipeline adds). The component walks the
 * structure recursively rather than assuming known keys, so a new field appears
 * without any change here.
 */
@Component({
  selector: 'tock-vector-store-metadata',
  templateUrl: './metadata-view.component.html',
  styleUrl: './metadata-view.component.scss',
  standalone: false
})
export class MetadataViewComponent {
  @Input() metadata: Record<string, unknown> | null = null;

  get entries(): { key: string; value: unknown }[] {
    if (!this.metadata) return [];
    return Object.entries(this.metadata).map(([key, value]) => ({ key, value }));
  }

  isObject(value: unknown): boolean {
    return value !== null && typeof value === 'object' && !Array.isArray(value);
  }

  isArray(value: unknown): boolean {
    return Array.isArray(value);
  }

  asRecord(value: unknown): Record<string, unknown> {
    return value as Record<string, unknown>;
  }

  asArray(value: unknown): unknown[] {
    return value as unknown[];
  }

  /** A scalar renders as text; an http(s) value renders as a link. */
  isLink(value: unknown): boolean {
    return typeof value === 'string' && /^https?:\/\//.test(value);
  }
}
