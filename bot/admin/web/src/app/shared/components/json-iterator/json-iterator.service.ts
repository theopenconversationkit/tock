import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class JsonIteratorService {
  public communication = new Subject<any>();

  expandAll(): void {
    this.communication.next({
      type: 'expand'
    });
  }
}
