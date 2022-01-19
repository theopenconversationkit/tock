/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {Directive, ElementRef} from '@angular/core';

/**
 * Hack: fix https://github.com/akveo/nebular/issues/2723
 */
@Directive({
  selector: '[tock-delay]'
})
export class DelayDirective {

  constructor(el: ElementRef) {
    el.nativeElement.style.display = 'none';
    setTimeout(() => el.nativeElement.style.display = 'inherit', 20);
  }

}
