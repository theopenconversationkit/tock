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
import {Application} from "../../model/application";
import {MdSnackBar} from "@angular/material";
import {StateService} from "../../core/state.service";

@Component({
  selector: 'tock-applications',
  templateUrl: 'applications.component.html',
  styleUrls: ['applications.component.css']
})
export class ApplicationsComponent implements OnInit {

  constructor(private snackBar: MdSnackBar,
              private state: StateService) {
  }

  ngOnInit() {
  }

  selectApplication(app: Application) {
    this.state.changeApplication(app);
    this.snackBar.open(`Application ${app.name} selected`, "Selection", {duration: 1000});
  }


}
