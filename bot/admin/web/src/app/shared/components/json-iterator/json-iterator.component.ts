import { Component, Input } from '@angular/core';
import { isPrimitive } from '../../utils';
import { JsonIteratorService } from './json-iterator.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-json-iterator',
  templateUrl: './json-iterator.component.html',
  styleUrls: ['./json-iterator.component.scss']
})
export class JsonIteratorComponent {
  destroy = new Subject();

  @Input() data: { [key: string]: any };
  @Input() isRoot: boolean = true;
  @Input() parentKey: string;

  isDeployed: boolean = false;

  isPrimitive = isPrimitive;

  constructor(private jsonIteratorService: JsonIteratorService) {
    this.jsonIteratorService.communication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type === 'expand' && !this.isDeployed) {
        this.isDeployed = true;
        setTimeout(() => {
          this.expandAll();
        });
      }
    });
  }

  switchDeployed() {
    this.isDeployed = !this.isDeployed;
  }

  expandAll() {
    this.jsonIteratorService.expandAll();
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
