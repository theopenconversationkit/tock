import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

interface DebugDocument {
  content: string;
  metadata: {
    index_session_id?: string;
    id: string;
    title: string;
    chunk: string;
    [key: string]: any;
  };
}

interface ContextUsageEntry {
  chunk: string; // format "<id>:<chunk>"
  sentences?: string[];
  used_in_response: boolean;
  reason?: string;
}

interface DocumentSummaryItem {
  title: string;
  chunk: string;
  id: string;
  used: boolean;
  reason?: string;
  hasUsageInfo: boolean;
  content: string;
  sentences: string[];
  metadata: Record<string, any>;
  metadataEntries: { key: string; value: any }[];
  expanded: boolean;
}

@Component({
  selector: 'tock-debug-viewer-dialog',
  templateUrl: './debug-viewer-dialog.component.html',
  styleUrls: ['./debug-viewer-dialog.component.scss']
})
export class DebugViewerDialogComponent implements OnInit {
  @Input() debug?: any;
  @Input() title?: string = 'Debug infos';

  allDocuments: DocumentSummaryItem[] = [];
  usedDocuments: DocumentSummaryItem[] = [];
  unusedDocuments: DocumentSummaryItem[] = [];
  hasContextUsage = false;

  constructor(public dialogRef: NbDialogRef<DebugViewerDialogComponent>) {}

  get hasDocuments(): boolean {
    return this.allDocuments.length > 0;
  }

  ngOnInit(): void {
    this.buildDocumentsSummary();
  }

  private buildDocumentsSummary(): void {
    const documents: DebugDocument[] = this.debug?.documents ?? [];
    const contextUsage: ContextUsageEntry[] = this.debug?.answer?.context_usage ?? [];

    this.hasContextUsage = contextUsage.length > 0;

    const usageMap = new Map<string, ContextUsageEntry>();
    contextUsage.forEach((entry) => {
      if (entry?.chunk) usageMap.set(entry.chunk, entry);
    });

    this.allDocuments = documents.map((doc) => {
      const metadata = doc.metadata ?? ({} as DebugDocument['metadata']);
      const id = metadata.id ?? '';
      const chunk = metadata.chunk ?? '';
      const usage = usageMap.get(`${id}:${chunk}`);

      return {
        title: metadata.title ?? 'Sans titre',
        chunk,
        id,
        used: usage ? !!usage.used_in_response : false,
        reason: usage?.reason,
        hasUsageInfo: !!usage,
        content: doc.content ?? '',
        sentences: usage?.sentences ?? [],
        metadata,
        metadataEntries: Object.keys(metadata).map((key) => ({ key, value: metadata[key] })),
        expanded: false
      };
    });

    if (this.hasContextUsage) {
      this.usedDocuments = this.allDocuments.filter((d) => d.used);
      this.unusedDocuments = this.allDocuments.filter((d) => !d.used);
    } else {
      this.usedDocuments = [];
      this.unusedDocuments = [];
    }
  }

  toggle(doc: DocumentSummaryItem): void {
    doc.expanded = !doc.expanded;
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
