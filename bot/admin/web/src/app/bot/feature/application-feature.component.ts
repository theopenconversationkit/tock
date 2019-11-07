/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

import {Component, ElementRef, OnInit, ViewChild} from "@angular/core";
import {Feature} from "../model/feature";
import {BotService} from "../bot-service";
import {StateService} from "../../core-nlp/state.service";

@Component({
  selector: 'tock-application-feature',
  templateUrl: './application-feature.component.html',
  styleUrls: ['./application-feature.component.css']
})
export class ApplicationFeatureComponent implements OnInit {

  private currentApplicationUnsuscriber: any;
  features: Feature[] = [];
  create: boolean = false;
  feature: Feature = new Feature("", "", false);
  loadingApplicationsFeatures: boolean = false;
  @ViewChild('newCategory', {static: false}) newCategory: ElementRef;

  constructor(private state: StateService,
              private botService: BotService) {
  }

  ngOnInit(): void {
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe(a => this.refresh());
    this.refresh();
  }

  prepareCreate() {
    this.create = true;
    setTimeout(_ => this.newCategory.nativeElement.focus());
  }

  cancelCreate() {
    this.create = false;
  }

  toggleNew(newState: boolean) {
    this.feature.enabled = newState;
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
      this.loadingApplicationsFeatures = true;
      this.botService.getFeatures(this.state.currentApplication.name).subscribe(f => {
        this.features = f;
        this.loadingApplicationsFeatures = false;
      });
    }
  }

  toggle(f: Feature, newState) {
    f.enabled = newState;
    this.botService.toggleFeature(this.state.currentApplication.name, f.category, f.name).subscribe();
  }

  deleteFeature(f: Feature) {
    this.botService.deleteFeature(this.state.currentApplication.name, f.category, f.name)
      .subscribe(_ => this.refresh());
  }

}
