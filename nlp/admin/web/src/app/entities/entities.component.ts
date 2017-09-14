/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, OnInit} from "@angular/core";
import {StateService} from "../core/state.service";
import {NlpService} from "../nlp-tabs/nlp.service";
import {MdDialog, MdSnackBar, MdSnackBarConfig} from "@angular/material";
import {ApplicationService} from "../core/applications.service";
import {EntityDefinition} from "../model/nlp";

@Component({
  selector: 'tock-entities',
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent implements OnInit {

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MdSnackBar,
              private dialog: MdDialog,
              private applicationService: ApplicationService) {
  }

  ngOnInit() {
  }

  update(entity: EntityDefinition) {
    this.nlp.updateEntityDefinition(
      this.state.createUpdateEntityDefinitionQuery(entity)
    ).map(_ => this.applicationService.reloadCurrentApplication())
      .subscribe(_ => this.snackBar.open(`Entity updated`, "Update", {duration: 1000} as MdSnackBarConfig));
  }

}
