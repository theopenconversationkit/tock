/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Optional, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Observable, Subject, take, takeUntil } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { deepCopy, getExportFileName, getPropertyByNameSpace, isObject } from '../../../shared/utils';
import { saveAs } from 'file-saver-es';
import Papa from 'papaparse';
import JSZip from 'jszip';
import { unified } from 'unified';
import remarkParse from 'remark-parse';
import remarkStringify from 'remark-stringify';
import remarkGfm from 'remark-gfm';
import { visit } from 'unist-util-visit';
import type { Heading } from 'mdast';

enum Formats {
  json = 'json',
  csv = 'csv',
  md = 'md'
}

enum Delimiters {
  Comma = ',',
  Semi = ';',
  Pipe = '|',
  Tab = '\t',
  Space = ' '
}

enum ListDelimiters {
  Comma = ',',
  Semi = ';',
  Newline = '\n'
}

interface ExportForm {
  format: FormControl<Formats>;
  delimiter: FormControl<Delimiters>;
  listDelimiter: FormControl<ListDelimiters>;
  columns: FormArray<FormGroup<ExportFormColumnsGroup>>;
}

interface ExportFormColumnsGroup {
  name: FormControl<string>;
  selected: FormControl<boolean>;
  deepeningPathKey?: FormControl<string>;
  deepeningPathDirimantValue?: FormControl<string>;
}

type ExtractFormControlType<T> = {
  [K in keyof T]: T[K] extends FormControl<infer U>
    ? U
    : T[K] extends FormArray<FormControl<infer U>>
    ? Array<U>
    : T[K] extends FormArray<FormGroup<infer U>>
    ? Array<Partial<ExtractFormControlType<U>>>
    : T[K] extends FormGroup<infer U>
    ? Partial<ExtractFormControlType<U>>
    : T[K];
};

type GenericObject = { [key: string]: any };

interface DeepeningPropStrategy {
  [key: string]: {
    path: string;
    dirimantKey: string;
    possibleDirimantKeyValues?: string[];
    valueKey: string;
  };
}

@Component({
  selector: 'tock-data-export',
  templateUrl: './data-export.component.html',
  styleUrl: './data-export.component.scss'
})
export class DataExportComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();
  loading: boolean = false;
  isSubmitted: boolean = false;

  formats = Formats;
  delimiters = Delimiters;
  listDelimiters = ListDelimiters;

  columnNames: string[];

  @Input() data!: GenericObject[];
  @Input() exportFileNameType!: string;
  @Input() searchQuery?: Observable<any>;

  @Input() deepeningPropertiesStrategies: DeepeningPropStrategy;

  /**
   * Optional ordered list of column names to pre-select and prioritize.
   * Columns listed here will appear first (in the given order) and be checked by default.
   * Remaining columns are appended after, unchecked.
   */
  @Input() defaultColumns?: string[];

  /**
   * Optional prefix for the H1 heading in markdown export.
   * If provided, the heading will be built as "# {mdHeadingPrefix}: {entry.title}" when a title exists,
   * or "# {mdHeadingPrefix}" alone if not. Falls back to "# {entry.id}" when absent.
   */
  @Input() mdHeadingPrefix?: string;

  @Output() onClose = new EventEmitter<boolean>();

  constructor(
    private stateService: StateService,
    private toastrService: NbToastrService,
    @Optional() public dialogRef: NbDialogRef<DataExportComponent>
  ) {}

  ngOnInit(): void {
    const referenceLine = this.data[0];
    this.columnNames = Object.keys(referenceLine);

    // Build all column FormGroups, keyed by column name for easy lookup
    const columnGroupsByName = new Map<string, FormGroup<ExportFormColumnsGroup>>();

    this.columnNames.forEach((key) => {
      if (this.deepeningPropertiesStrategies?.hasOwnProperty(key)) {
        const deepeningStrategy = this.deepeningPropertiesStrategies[key];

        let dirimants;
        if (deepeningStrategy.possibleDirimantKeyValues?.length) {
          dirimants = deepeningStrategy.possibleDirimantKeyValues;
        } else {
          const targetedObj = referenceLine[key];
          const targetedProp = getPropertyByNameSpace(deepeningStrategy.path, targetedObj);
          dirimants = targetedProp.map((p) => p[deepeningStrategy.dirimantKey]);
        }

        dirimants.forEach((dirimant) => {
          const name = [key, deepeningStrategy.path, dirimant].join('.');
          columnGroupsByName.set(
            name,
            new FormGroup({
              name: new FormControl(name),
              selected: new FormControl(false),
              deepeningPathKey: new FormControl(key),
              deepeningPathDirimantValue: new FormControl(dirimant)
            }) as FormGroup<ExportFormColumnsGroup>
          );
        });
      } else {
        columnGroupsByName.set(
          key,
          new FormGroup({
            name: new FormControl(key),
            selected: new FormControl(false)
          })
        );
      }
    });

    // Push defaultColumns first (in order), pre-checked, then remaining columns
    if (this.defaultColumns?.length) {
      this.defaultColumns.forEach((colName) => {
        const group = columnGroupsByName.get(colName);
        if (group) {
          group.controls.selected.setValue(true);
          this.form.controls['columns'].push(group);
          columnGroupsByName.delete(colName);
        }
      });
    }

    columnGroupsByName.forEach((group) => {
      this.form.controls['columns'].push(group);
    });

    this.format.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((format: Formats) => {
      if (format === Formats.csv || format === Formats.md) {
        this.columns.setValidators([Validators.required, this.validateColumnsFormArray]);
      } else {
        this.columns.clearValidators();
      }

      this.columns.updateValueAndValidity();
    });

    this.columns.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((next: ExtractFormControlType<ExportFormColumnsGroup>[]) => {
      this.updateColumnsIndexes(next);
    });
  }

  form = new FormGroup<ExportForm>({
    format: new FormControl(Formats.json, [Validators.required]),
    delimiter: new FormControl(Delimiters.Comma, [Validators.required]),
    listDelimiter: new FormControl(ListDelimiters.Newline, [Validators.required]),
    columns: new FormArray([])
  });

  get format(): FormControl {
    return this.form.get('format') as FormControl;
  }

  get columns(): FormArray<FormGroup<ExportFormColumnsGroup>> {
    return this.form.get('columns') as FormArray<FormGroup<ExportFormColumnsGroup>>;
  }

  get delimiter(): FormControl {
    return this.form.get('delimiter') as FormControl;
  }

  get listDelimiter(): FormControl {
    return this.form.get('listDelimiter') as FormControl;
  }

  get canSave(): boolean {
    return this.form.valid;
  }

  validateColumnsFormArray(columns: FormArray): ValidationErrors | null {
    if (!columns.value.some((col) => col.selected)) {
      return { custom: 'Please select at least one column' };
    }

    return null;
  }

  getFirstFreeSlotIndex(stack, currIndex: number): number | null {
    for (let i = 0; i < stack.length; i++) {
      if (i !== currIndex && !stack[i].selected) return i;
    }
    return null;
  }

  updateColumnsIndexes(stack: ExtractFormControlType<ExportFormColumnsGroup>[]): void {
    for (let i = stack.length - 1; i > 0; i--) {
      if (stack[i].selected && i > 0) {
        const freeIndex = this.getFirstFreeSlotIndex(stack, i);

        if (freeIndex !== null && freeIndex < i) {
          const col = this.columns.at(i);
          this.columns.removeAt(i);
          this.columns.insert(freeIndex, col);
        }
      }
    }
  }

  canIncreaseColumnIndex(index: number): boolean {
    if (index >= this.columns.length - 1) return false;

    const freeIndex = this.getFirstFreeSlotIndex(this.columns.value, index);
    if (freeIndex === null) return true;
    return freeIndex > index + 1;
  }

  changeColumnIndex(shift, index): void {
    const col = this.columns.at(index);
    this.columns.removeAt(index);
    this.columns.insert(index + shift, col);
  }

  trackByFn(index: number, item: any): any {
    return item;
  }

  export(): void {
    this.isSubmitted = true;
    if (this.canSave) {
      this.loading = true;
      if (this.searchQuery) {
        this.searchQuery.pipe(take(1)).subscribe((result) => {
          this.download(Array.isArray(result) ? result : result.rows);
        });
      } else {
        this.download(deepCopy(this.data));
      }
    }
  }

  // Shared remark pipelines — instantiated once to avoid per-call overhead
  private readonly remarkParser = unified().use(remarkParse).use(remarkGfm);
  private readonly remarkSerializer = unified().use(remarkStringify).use(remarkGfm);

  /**
   * Uses remark to parse the string and inspect the AST to determine whether the value
   * is likely markdown content rather than plain text.
   *
   * Confidence tiers:
   * - High (1 match sufficient): code blocks (``` fences), links — syntactically unambiguous
   * - Medium (2 matches, or 1 combined with a low signal): headings, blockquotes
   * - Low (not sufficient alone): lists (- / *), bold (**) — too common in plain text
   */
  private looksLikeMarkdown(value: string): boolean {
    const tree = this.remarkParser.parse(value);

    const highConfidenceTypes = new Set(['code', 'link', 'table']);
    const mediumConfidenceTypes = new Set(['heading', 'blockquote', 'strikethrough']);
    const lowConfidenceTypes = new Set(['list', 'strong']);

    let highCount = 0;
    let mediumCount = 0;
    let lowCount = 0;

    visit(tree, (node) => {
      if (highConfidenceTypes.has(node.type)) highCount++;
      else if (mediumConfidenceTypes.has(node.type)) mediumCount++;
      else if (lowConfidenceTypes.has(node.type)) lowCount++;
    });

    return (
      highCount >= 1 || // a single code block or link is enough
      mediumCount >= 2 || // two or more headings / blockquotes
      (mediumCount >= 1 && lowCount >= 1) // e.g. one heading combined with a list
    );
  }

  /**
   * Uses remark to parse the markdown, increment all heading depths by 1 (max depth: 6),
   * then stringify back. Code block contents are opaque AST nodes and are never touched.
   */
  private async demoteMarkdownHeadings(md: string): Promise<string> {
    const tree = this.remarkParser.parse(md);

    visit(tree, 'heading', (node: Heading) => {
      node.depth = Math.min(node.depth + 1, 6) as Heading['depth'];
    });

    return this.remarkSerializer.stringify(tree);
  }

  private async serializeValueToMarkdown(value: any): Promise<string> {
    if (value === null || value === undefined) {
      return '';
    }

    if (Array.isArray(value)) {
      return value.map((entry) => `- ${isObject(entry) ? JSON.stringify(entry) : entry}`).join('\n');
    }

    if (isObject(value)) {
      return Object.entries(value)
        .map(([k, v]) => `- **${k}**: ${isObject(v) || Array.isArray(v) ? JSON.stringify(v) : v}`)
        .join('\n');
    }

    const str = String(value);

    if (this.looksLikeMarkdown(str)) {
      return this.demoteMarkdownHeadings(str);
    }

    return str;
  }

  private buildMarkdownFileName(entry: GenericObject): string | null {
    // Reserved filenames on Windows (case-insensitive)
    const WINDOWS_RESERVED = /^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$/i;
    // Max length: 255 chars is the limit on most FS; we keep 200 to leave room for extension + zip path
    const MAX_LENGTH = 200;

    const sanitize = (str: string): string =>
      str
        .normalize('NFD') // decompose accented chars (é -> e + ́)
        .replace(/[̀-ͯ]/g, '') // strip combining diacritics
        .replace(/[<>:"/\\|?*\x00-\x1f]/g, '') // strip chars forbidden on Windows/macOS/Linux
        .replace(/[\s]+/g, '_') // replace whitespace with underscores
        .replace(/[^a-zA-Z0-9_\-]/g, '_') // replace any remaining unsafe chars
        .replace(/_+/g, '_') // collapse consecutive underscores
        .replace(/^[_\-]+|[_\-]+$/g, ''); // trim leading/trailing underscores and hyphens

    const id = entry['id'] ? sanitize(String(entry['id'])) : '';
    const title = entry['title'] ? sanitize(String(entry['title'])) : '';
    // Re-check after sanitization: special-char-only values may have been reduced to empty strings

    let name: string;
    if (!title && !id) return null;

    if (title && id) {
      // Ensure the combined name fits within MAX_LENGTH: reserve space for id + separator
      const reservedForId = id.length + 1; // 1 for '_'
      const truncatedTitle = title.slice(0, MAX_LENGTH - reservedForId);
      name = `${truncatedTitle}_${id}`;
    } else if (title) {
      name = title.slice(0, MAX_LENGTH);
    } else if (id) {
      name = id.slice(0, MAX_LENGTH);
    }

    // Final guard: replace Windows reserved names
    if (WINDOWS_RESERVED.test(name)) {
      name = `_${name}`;
    }

    return name;
  }

  private buildMarkdownHeading(entry: GenericObject): string | null {
    if (this.mdHeadingPrefix) {
      const title = entry['title'];
      return title ? `# ${this.mdHeadingPrefix}: ${title}` : `# ${this.mdHeadingPrefix}`;
    }
    const id = entry['id'];
    return id ? `# ${id}` : null;
  }

  private async generateMarkdownForEntry(
    entry: GenericObject,
    selectedColumns: Partial<ExtractFormControlType<ExportFormColumnsGroup>>[]
  ): Promise<string> {
    const heading = this.buildMarkdownHeading(entry);
    const lines: string[] = heading ? [heading, ''] : [];

    for (const col of selectedColumns) {
      const value = entry[col.name];
      const serialized = await this.serializeValueToMarkdown(value);

      // Skip the section entirely if there is no content to render
      if (!serialized?.trim()) continue;

      lines.push(`## ${col.name}`);
      lines.push('');
      lines.push(serialized);
      lines.push('');
    }

    return lines.join('\n');
  }

  download(data: GenericObject[]): void {
    const exportFileName = getExportFileName(
      this.stateService.currentApplication.namespace,
      this.stateService.currentApplication.name,
      this.exportFileNameType,
      this.format.value
    );

    let blob: Blob;

    if (this.format.value === Formats.json) {
      blob = new Blob([JSON.stringify(data)], {
        type: 'application/json'
      });

      saveAs(blob, exportFileName);
    }

    if (this.format.value === Formats.csv) {
      const selectedColumns = this.columns.value.filter((col) => col.selected);
      const columns = selectedColumns.map((col) => col.name);

      selectedColumns.forEach((selectedColumn) => {
        if (selectedColumn.deepeningPathKey) {
          const deepeningStrategy = this.deepeningPropertiesStrategies[selectedColumn.deepeningPathKey];

          data = data.map((line) => {
            const targetedObj = line[selectedColumn.deepeningPathKey];
            const targetedProp = getPropertyByNameSpace(deepeningStrategy.path, targetedObj);

            const targetedPropEntry = targetedProp.find((entry) => {
              return entry[deepeningStrategy.dirimantKey] === selectedColumn.deepeningPathDirimantValue;
            });

            if (targetedPropEntry) {
              const targetedPropEntryValue = targetedPropEntry[deepeningStrategy.valueKey];
              if (targetedPropEntryValue) {
                line[selectedColumn.name] = targetedPropEntryValue;
              }
            }

            return line;
          });
        }

        data = data.map((line) => {
          if (isObject(line[selectedColumn.name])) {
            line[selectedColumn.name] = JSON.stringify(line[selectedColumn.name]);
          } else if (Array.isArray(line[selectedColumn.name])) {
            line[selectedColumn.name] = line[selectedColumn.name].map((entry) => {
              if (isObject(entry)) entry = JSON.stringify(entry);
              return entry;
            });
          }
          return line;
        });
      });

      if (this.listDelimiter.value !== ListDelimiters.Comma) {
        data = data.map((line) => {
          Object.entries(line).forEach(([key, value]) => {
            if (Array.isArray(value)) {
              line[key] = value.join(this.listDelimiter.value);
            }
          });
          return line;
        });
      }

      const rawCsv = Papa.unparse(
        {
          fields: columns,
          data: data
        },
        { delimiter: this.delimiter.value }
      );

      blob = new Blob([rawCsv], { type: 'text/csv' });
      saveAs(blob, exportFileName);
    }

    if (this.format.value === Formats.md) {
      const selectedColumns = this.columns.value.filter((col) => col.selected);

      // Apply deepening strategies (same logic as CSV)
      selectedColumns.forEach((selectedColumn) => {
        if (selectedColumn.deepeningPathKey) {
          const deepeningStrategy = this.deepeningPropertiesStrategies[selectedColumn.deepeningPathKey];

          data = data.map((line) => {
            const targetedObj = line[selectedColumn.deepeningPathKey];
            const targetedProp = getPropertyByNameSpace(deepeningStrategy.path, targetedObj);

            const targetedPropEntry = targetedProp.find((entry) => {
              return entry[deepeningStrategy.dirimantKey] === selectedColumn.deepeningPathDirimantValue;
            });

            if (targetedPropEntry) {
              const targetedPropEntryValue = targetedPropEntry[deepeningStrategy.valueKey];
              if (targetedPropEntryValue) {
                line[selectedColumn.name] = targetedPropEntryValue;
              }
            }

            return line;
          });
        }
      });

      const zip = new JSZip();
      const zipFolderName = exportFileName.replace('.md', '');
      const folder = zip.folder(zipFolderName);

      // Generate all markdown files concurrently, then zip
      Promise.all(
        data.map((entry, index) => {
          const fileName = this.buildMarkdownFileName(entry) ?? `entry_${index}`;
          return this.generateMarkdownForEntry(entry, selectedColumns).then((markdownContent) => {
            folder.file(`${fileName}.md`, markdownContent);
          });
        })
      )
        .then(() => zip.generateAsync({ type: 'blob' }))
        .then((zipBlob) => {
          saveAs(zipBlob, `${zipFolderName}.zip`);

          this.toastrService.show(`${this.exportFileNameType} dump provided`, `${this.exportFileNameType} dump`, {
            duration: 3000,
            status: 'success'
          });

          this.loading = false;
        })
        .catch((err) => {
          console.error('Markdown export failed', err);

          this.toastrService.show('An error occurred during the markdown export', 'Export failed', {
            duration: 5000,
            status: 'danger'
          });

          this.loading = false;
        });

      // Return early — toastr and loading are handled in the promise chain above
      return;
    }

    this.toastrService.show(`${this.exportFileNameType} dump provided`, `${this.exportFileNameType} dump`, {
      duration: 3000,
      status: 'success'
    });

    this.loading = false;
  }

  close(): void {
    this.onClose.emit(true);
    if (this.dialogRef) this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
