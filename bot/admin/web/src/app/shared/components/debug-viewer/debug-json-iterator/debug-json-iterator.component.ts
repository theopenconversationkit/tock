import { Component, Input } from '@angular/core';
import { isPrimitive } from '../../../utils';

@Component({
  selector: 'tock-debug-json-iterator',
  templateUrl: './debug-json-iterator.component.html',
  styleUrls: ['./debug-json-iterator.component.scss']
})
export class DebugJsonIteratorComponent {
  @Input() data: any;
  @Input() isRoot: boolean = true;
  @Input() parentKey: string;

  isDeployed: boolean = false;

  isPrimitive = isPrimitive;

  switchDeployed() {
    this.isDeployed = !this.isDeployed;
  }
}
