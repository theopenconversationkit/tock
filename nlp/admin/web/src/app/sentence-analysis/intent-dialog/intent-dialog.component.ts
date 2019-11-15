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

import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";

@Component({
  selector: 'tock-intent-dialog',
  templateUrl: './intent-dialog.component.html',
  styleUrls: ['./intent-dialog.component.css']
})
export class IntentDialogComponent implements OnInit {

  create: boolean;
  name: string;
  label: string;
  category: string = "default";
  description: string;
  categories: string[] = [];
  originalCategories: string[] = [];
  dialogType: string;
  private nameInitialized = false;

  @ViewChild('labelElement', {static: false}) labelElement: ElementRef;

  constructor(
    public dialogRef: MatDialogRef<IntentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private state: StateService) {
    this.create = this.data.create;
    this.name = this.data.name;
    this.label = this.data.label;
    this.description = this.data.description;
    this.category = this.data.category;
    this.dialogType = this.data.story ? "Story" : "Intent";
    setTimeout(() => this.labelElement.nativeElement.focus(), 500);
  }

  ngOnInit() {
    this.state.currentIntentsCategories.subscribe(c => {
      this.categories = c.map(cat => cat.category);
      this.originalCategories = this.categories;
    });
  }

  copyToName() {
    if (!this.nameInitialized && this.create) {
      this.formatName(this.label);
    }
  }

  categoryChange() {
    if (this.category) {
      let cat = this.category.toLowerCase().trim();
      this.categories = cat.length === 0
        ? this.originalCategories
        : this.originalCategories.filter(c => c.toLowerCase().startsWith(cat));
    }
  }

  format() {
    this.nameInitialized = true;
    this.formatName(this.name);
  }

  private formatName(name: string) {
    if (name) {
      this.name = name.replace(/[^A-Za-z_-]*/g, '').toLowerCase().trim();
    }
  }

  save() {
    if (this.name && this.name.trim().length !== 0) {
      this.format();
      this.dialogRef.close({
        name: this.name.trim(),
        label: !this.label || this.label.trim().length === 0 ? null : this.label.trim(),
        description: !this.description || this.description.trim().length === 0 ? null : this.description.trim(),
        category: !this.category || this.category.trim().length === 0 ? null : this.category.trim(),
      });
    }
  }

}
