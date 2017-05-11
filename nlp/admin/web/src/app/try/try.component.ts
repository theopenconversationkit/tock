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
import {ParseQuery, Sentence} from "../model/nlp";
import {NlpService} from "../nlp-tabs/nlp.service";
import {StateService} from "../core/state.service";
import {MdSnackBar} from "@angular/material";

@Component({
  selector: 'tock-try',
  templateUrl: './try.component.html',
  styleUrls: ['./try.component.css']
})
export class TryComponent implements OnInit {

  sentence: Sentence;

  constructor(private nlp: NlpService,
              private state: StateService,
              private snackBar: MdSnackBar) {
  }

  ngOnInit() {
  }

  onTry(value: string) {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    const v = value.trim();
    if (v.length == 0) {
      this.snackBar.open(`Please enter a non-empty query`, "ERROR", {duration: 2000});
    } else {
      this.nlp.parse(new ParseQuery(app.namespace, app.name, language, v, this.state.currentEngine())).subscribe(sentence => {
        this.sentence = sentence;
      });
    }
  }

  onClose() {
    this.sentence = null;
  }

}
