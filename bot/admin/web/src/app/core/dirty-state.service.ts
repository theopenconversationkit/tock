import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface DirtyStateGuard {
  canDeactivate: () => Observable<boolean> | boolean;
}

@Injectable({ providedIn: 'root' })
export class DirtyStateService {
  private _guard: DirtyStateGuard | null = null;

  register(guard: DirtyStateGuard): void {
    this._guard = guard;
  }

  unregister(): void {
    this._guard = null;
  }

  confirm(): Observable<boolean> {
    if (!this._guard) return of(true);
    const result = this._guard.canDeactivate();
    return result instanceof Observable ? result : of(result);
  }
}
