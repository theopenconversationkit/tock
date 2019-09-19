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
import {Feature} from "../model/feature";
import {BotService} from "../bot-service";
import {StateService} from "../../core-nlp/state.service";

@Component({
  selector: 'tock-feature',
  templateUrl: './feature.component.html',
  styleUrls: ['./feature.component.css']
})
export class FeatureComponent implements OnInit {

  private currentApplicationUnsuscriber: any;
  features: Feature[] = [];
  create: boolean = false;
  feature: Feature = new Feature("category", "name", false);

  constructor(private state: StateService,
              private botService: BotService) {
  }

  ngOnInit(): void {
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe(a => this.refresh());
    this.refresh();
  }

  prepareCreate() {
    this.create = true;
  }

  cancelCreate() {
    this.create = false;
  }

  toggleNew() {
    this.feature.enabled = !this.feature.enabled;
  }

  addFeature() {
    this.botService.addFeature(this.state.currentApplication.name, this.feature.enabled, this.feature.category, this.feature.name).subscribe(
      _ => {
        this.refresh();
        this.create = false;
      }
    );
  }

  refresh() {
    if (this.state.currentApplication) {
      this.botService.getFeatures(this.state.currentApplication.name).subscribe(f => this.features = f);
    }
  }

  toggle(f: Feature) {
    this.botService.toggleFeature(this.state.currentApplication.name, f.category, f.name).subscribe();
  }

  deleteFeature(f: Feature) {
    this.botService.deleteFeature(this.state.currentApplication.name, f.category, f.name)
      .subscribe(_ => this.refresh());
  }

}
