/**
 * MarkdownDiffService
 *
 * Dispatcher vers le Web Worker partagé.
 * Un seul Worker est instancié pour toute l'application.
 * Chaque appel à diff() retourne une Promise résolue quand le Worker répond.
 * Les 20+ appels parallèles sont mis en file automatiquement par le Worker.
 */
import { Injectable, OnDestroy } from '@angular/core';

export interface DiffResult {
  textA: string;
  textB: string;
}

interface PendingJob {
  resolve: (result: DiffResult) => void;
  reject: (err: Error) => void;
}

@Injectable({ providedIn: 'root' })
export class MarkdownDiffService implements OnDestroy {
  private worker!: Worker;

  // Map job id → pending Promise callbacks
  private pending = new Map<string, PendingJob>();

  // Monotonic counter for unique job IDs
  private counter = 0;

  constructor() {
    this.initWorker();
  }

  // ── Public API ──────────────────────────────────────────────────────────────

  /**
   * Compute the diff asynchronously in the Web Worker.
   * Returns a Promise that resolves with annotated Markdown for both sides.
   *
   * Safe to call 20+ times in parallel — the Worker queues jobs automatically.
   */
  diff(textA: string, textB: string): Promise<DiffResult> {
    return new Promise<DiffResult>((resolve, reject) => {
      const id = `diff-${++this.counter}`;
      this.pending.set(id, { resolve, reject });
      this.worker.postMessage({ id, textA, textB });
    });
  }

  // ── Lifecycle ───────────────────────────────────────────────────────────────

  ngOnDestroy(): void {
    this.worker.terminate();
    this.pending.forEach(({ reject }) => reject(new Error('MarkdownDiffService destroyed')));
    this.pending.clear();
  }

  // ── Private ─────────────────────────────────────────────────────────────────

  private initWorker(): void {
    // Angular CLI registers the worker via the webWorker builder option.
    // The URL syntax below is what Angular CLI needs to detect and bundle it.
    this.worker = new Worker(new URL('./markdown-diff.worker', import.meta.url), { type: 'module' });

    this.worker.addEventListener('message', ({ data }) => {
      const job = this.pending.get(data.id);
      if (!job) return;
      this.pending.delete(data.id);

      if (data.error) {
        job.reject(new Error(data.error));
      } else {
        job.resolve({ textA: data.textA, textB: data.textB });
      }
    });

    this.worker.addEventListener('error', (event) => {
      // Worker-level crash: reject all pending jobs
      const err = new Error(`Worker error: ${event.message}`);
      this.pending.forEach(({ reject }) => reject(err));
      this.pending.clear();

      // Attempt to restart
      console.error('[MarkdownDiffService] Worker crashed, restarting…', event);
      this.initWorker();
    });
  }
}
