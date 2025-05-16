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

import { Component, Input, OnDestroy } from '@angular/core';
import { isPrimitive } from '../../utils';
import { JsonIteratorService } from './json-iterator.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-json-iterator',
  templateUrl: './json-iterator.component.html',
  styleUrls: ['./json-iterator.component.scss']
})
export class JsonIteratorComponent implements OnDestroy {
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
