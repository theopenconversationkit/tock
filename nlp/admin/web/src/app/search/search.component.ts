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

import {Component, OnInit, ViewChild} from '@angular/core';
import {SentenceFilter, SentencesScrollComponent} from "../sentences-scroll/sentences-scroll.component";
import {SentenceStatus} from "../model/nlp";
import {StateService} from "../core/state.service";

@Component({
  selector: 'tock-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  filter : SentenceFilter = new SentenceFilter();
  status : SentenceStatus;

  @ViewChild(SentencesScrollComponent) scroll;

  constructor(public state: StateService) { }

  ngOnInit() {
  }

  search() {
    if(this.status) {
      this.filter.status = [this.status];
    } else {
      this.filter.status = [];
    }
    if(this.filter.intentId === "-1") {
      this.filter.intentId = null;
    }
    this.scroll.refresh();
  }

}
