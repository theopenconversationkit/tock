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

import { Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { StateService } from '../../core-nlp/state.service';
import { IntentsCategory } from '../../model/nlp';

@Component({
  selector: 'tock-story-dialog',
  templateUrl: './story-dialog.component.html',
  styleUrls: ['./story-dialog.component.css']
})
export class StoryDialogComponent implements OnInit {
  create: boolean;
  name: string;
  intent: string;
  label: string;
  tag: string;
  category: string = 'default';
  description: string;
  categories: string[] = [];
  originalCategories: IntentsCategory[] = [];
  dialogType: string;
  private nameInitialized = false;
  intentCategories: IntentsCategory[] = [];
  freezeIntent: boolean = false;
  userSentence: string = '';

  @ViewChild('labelElement') labelElement: ElementRef;

  constructor(public dialogRef: MatDialogRef<StoryDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: any, public state: StateService) {
    this.create = this.data.create;
    this.intent = this.data.intent;
    this.name = this.data.name;
    this.label = this.data.label;
    this.tag = this.data.tag;
    this.description = this.data.description;
    this.category = this.data.category;
    this.dialogType = 'Story';
    this.freezeIntent = this.data.freezeIntent;
    this.userSentence = this.data.userSentence ? this.data.userSentence : '';
    setTimeout(() => this.labelElement.nativeElement.focus(), 500);
  }

  ngOnInit() {
    this.state.currentIntentsCategories.subscribe((c) => {
      this.originalCategories = c;
      this.intentCategories = c;
      this.categories = c.map((cat) => cat.category);
    });
  }

  private oldIntent: string;

  intentCategoryChange() {
    if (this.oldIntent !== this.intent) {
      this.oldIntent = this.intent;
      let intent = this.intent.trim().toLowerCase();
      this.intentCategories = this.originalCategories
        .map(
          (c) =>
            new IntentsCategory(
              c.category,
              c.intents.filter((i) => i.intentLabel().toLowerCase().startsWith(intent))
            )
        )
        .filter((c) => c.intents.length !== 0);
    }
  }

  categoryChange() {
    let cat = this.category.toLowerCase().trim();
    let allCats = this.originalCategories.map((cat) => cat.category);
    this.categories = cat.length === 0 ? allCats : allCats.filter((c) => c.toLowerCase().startsWith(cat));
  }

  copyToName() {
    if (!this.nameInitialized && this.create) {
      this.formatName(this.label);
    }
  }

  format() {
    this.nameInitialized = true;
    this.formatName(this.name);
  }

  private formatName(name: string) {
    if (name) {
      this.name = name
        .replace(/[^A-Za-z_-]*/g, '')
        .toLowerCase()
        .trim();
    }
  }

  save() {
    if (this.name && this.name.trim().length !== 0 && this.intent && this.intent.trim().length !== 0) {
      this.format();
      this.dialogRef.close({
        name: this.name.trim(),
        label: !this.label || this.label.trim().length === 0 ? this.name.trim() : this.label.trim(),
        tag: this.tag ? this.tag.trim() : null,
        intent: this.intent.trim(),
        description: !this.description || this.description.trim().length === 0 ? '' : this.description.trim(),
        category: !this.category || this.category.trim().length === 0 ? 'default' : this.category.trim(),
        userSentence: this.userSentence
      });
    }
  }
}
