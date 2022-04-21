/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ClassifiedEntity, EntityContainer } from '../../model/nlp';
import { StateService } from '../../core-nlp/state.service';

@Component({
  selector: 'tock-edit-entities',
  templateUrl: 'edit-entities.component.html',
  styleUrls: ['edit-entities.component.css']
})
export class EditEntitiesComponent {
  @Input() container: EntityContainer;
  @Input() displayProbabilities: boolean = false;
  @Input() paddingLeft: number = 0;
  @Input() readOnly: boolean = false;

  @Output() containerChange = new EventEmitter();

  constructor(public state: StateService) {}

  onDeleteEntity(entity: ClassifiedEntity) {
    this.container.removeEntity(entity);
    this.containerChange.emit(true);
  }

  onContainerChange() {
    this.container = this.container.clone();
    this.containerChange.emit(true);
  }
}
