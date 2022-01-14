import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {NbTagInputAddEvent} from '@nebular/theme/components/tag/tag-input.directive';
import {NbTagComponent} from '@nebular/theme/components/tag/tag.component';
import {BehaviorSubject, combineLatest, Observable, ReplaySubject} from 'rxjs';
import {distinct, filter, takeUntil } from 'rxjs/operators';
import {debounceTime, map, take} from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { EditUtteranceResult, notCancelled, ValidUtteranceResult } from 'src/app/faq/common/components/edit-utterance/edit-utterance-result';
import { EditUtteranceComponent } from 'src/app/faq/common/components/edit-utterance/edit-utterance.component';
import {FrequentQuestion, Utterance, utteranceEquals, utteranceEquivalent, utteranceSomewhatSimilar} from 'src/app/faq/common/model/frequent-question';
import {ActionResult, QaEditorEvent, QaSidebarEditorService} from '../qa-sidebar-editor.service';
import {getPosition, hasItem} from '../../../common/util/array-utils';
import {somewhatSimilar, verySimilar } from 'src/app/faq/common/util/string-utils';
import { concatMap } from 'rxjs/operators';
import { SentencesService } from 'src/app/faq/common/sentences.service';
import { QaService } from 'src/app/faq/common/qa.service';
import { EditorTabName } from '../../qa.component';
import { startWith } from 'rxjs/operators';

// Simple builder for text 'utterance predicate'
function textMatch(text: string): (Utterance) => boolean {
  if (!text?.trim()) {
    return _ => true;
  }
  const lowerText = text.trim().toLowerCase();

  return (u: Utterance) => {
    const lowerUtteranceText = (u || '').toLowerCase();
    return lowerUtteranceText.includes(lowerText);
  };
}

/**
 * Content for Q&A Edition sidebar
 *
 * Handle both 'New' and 'Edit existing' cases
 *
 * Note: Everything is in this single component because we consider all tabs as a single form
 */
@Component({
  selector: 'tock-qa-sidebar-editor-content',
  templateUrl: './qa-sidebar-editor-content.component.html',
  styleUrls: ['./qa-sidebar-editor-content.component.scss'],
  host: {
    "class": "h-100 d-flex flex-column justify-content-start align-items-stretch"
  }
})
export class QaSidebarEditorContentComponent implements OnInit, OnDestroy, OnChanges {

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  @Input()
  tabName: EditorTabName;

  @Input()
  fq?: FrequentQuestion;

  @Output()
  validityChanged = new EventEmitter<boolean>();

  /* Form Data */

  tags: Set<string> = new Set();

  editedUtterances$: ReplaySubject<Utterance[]> = new ReplaySubject(1);

  readonly newFaqForm = new FormGroup({
    name: new FormControl('', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(40)
    ]),
    description: new FormControl('', [
      Validators.maxLength(500)
    ]),
    answer: new FormControl('', [
      Validators.required,
      Validators.maxLength(800)
    ]),
    active: new FormControl(true, [
      Validators.required
    ]),
  });

  /* Search */

  search?: string;
  searchSubject$: BehaviorSubject<string> = new BehaviorSubject('');
  displayedUtterances$: Observable<Utterance[]>;

  constructor(
    private readonly sidebarEditorService: QaSidebarEditorService,
    private readonly qaService: QaService,
    private readonly dialog: DialogService,
  ) {
  }

  ngOnInit(): void {
    this.observeUtteranceSearch();
    this.observeValidity();
    this.registerSaveAction();
  }

  ngAfterViewInit() {
    this.validityChanged.emit(this.newFaqForm.valid); // initial event value
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnChanges(changes: { [key: string]: SimpleChange }): any {
    if ('fq' in changes) {
      this.updateForm(changes?.fq?.currentValue);
    }
  }

  observeUtteranceSearch(): void {
    this.displayedUtterances$ = combineLatest(this.editedUtterances$, this.searchSubject$).pipe( // unsubscribe handled by angular template mechanism
      takeUntil(this.destroy$),
      debounceTime(200),
      map(([utterances, search]) => {
        const res = utterances.filter(textMatch(search));

        res.sort((a, b) => (a || '').localeCompare(b || ''))
        return res;
      })
    );
  }

  observeValidity(): void {
    const valueChanges$ =  this.newFaqForm.valueChanges
      .pipe(
        startWith(null), // so combineLatest just has to wait for first editedUtterances$ event
        takeUntil(this.destroy$),
        debounceTime(200)
      );

    combineLatest(this.editedUtterances$, valueChanges$)
      .pipe(
        map(([utterances, _]) => {
          return this.newFaqForm.valid && !!utterances?.length;
        })
      )
      .subscribe(
      validity => this.validityChanged.next(validity)
    );
  }

  registerSaveAction(): void {
    // listen to event 'save'
    this.sidebarEditorService.registerActionHandler('save', this.destroy$, this.save.bind(this));
  }

  save(evt: QaEditorEvent): Observable<ActionResult> {
    // replay last known utterances array
    return this.editedUtterances$.pipe(take(1), concatMap( utterances => {

      // validate and construct entity from form data
      const fq: FrequentQuestion = {
        id: this.fq.id,
        applicationId: this.fq.applicationId,
        language: this.fq.language,
        tags: Array.from(this.tags),
        description: '' + (this.newFaqForm.controls['description'].value || ''),
        answer: '' + (this.newFaqForm.controls['answer'].value || ''),
        title: '' + (this.newFaqForm.controls['name'].value || ''),
        status: this.fq.status,
        utterances,
        enabled: (true === this.newFaqForm.controls['active'].value)
      };

      return this.qaService.save(fq, this.destroy$).pipe(
        map(fq => {
          const res: ActionResult = {
            outcome: 'save-done',
            payload: fq
          };
          return res;
        })
      );
    }));
  }

  getControlStatus(controlName: string): 'success' | 'basic' | 'warning' {
    const control = this.newFaqForm.controls[controlName];

    if (control.dirty || control.touched) {
      if (control.invalid) {
        return 'warning';
      } else {
        return 'success';
      }
    } else {
      return 'basic';
    }
  }

  getTagControlStatus(): 'success' | 'basic' {
    if (this.tags.size) {
      return 'success';
    } else {
      return 'basic';
    }
  }

  isControlAlert(controlName: string): boolean {
    const control = this.newFaqForm.controls[controlName];

    return control.invalid && (control.dirty || control.touched);
  }

  updateForm(fq?: FrequentQuestion): void {
    if (fq) {
      const utterances = JSON.parse(JSON.stringify(fq.utterances)); // deep clone

      this.editedUtterances$.next(utterances);

      this.newFaqForm.setValue({
        name: '' + (fq.title || ''),
        description: '' + (fq.description || ''),
        answer: '' + (fq.answer || ''),
        active: fq.enabled === true
      });
      this.tags = new Set<string>(fq.tags.slice());
    } else {

      this.editedUtterances$.next([]);
      this.newFaqForm.setValue({
        name: '',
        description: '',
        answer: '',
        active: false
      });
      this.tags = new Set<string>();
    }
  }

  public onTagRemove(tagToRemove: NbTagComponent): void {
    this.tags.delete(tagToRemove.text);
  }

  public onTagAdd({value, input}: NbTagInputAddEvent): void {
    if (value) {
      this.tags.add(value)
    }
    input.nativeElement.value = '';
  }

  handleKeyEnter(evt) {
    evt.preventDefault(); // fix weird behavior
  }

  utteranceSearchChange(e) {
    this.searchSubject$.next(e.target.value);
  }

  private removeFromUtterances(u: Utterance): void {

    // get array
    this.editedUtterances$.pipe(take(1)).subscribe(arr => {
        // find existing item location
        const index = getPosition(arr, u, utteranceEquals);

        // Remove utterance
        const updatedArr = arr.slice();
        updatedArr.splice(index, 1);

        // publish updated array
        this.editedUtterances$.next(updatedArr);
      }
    );
  }

  private appendToUtterances(u: Utterance): void {

    // get array
    this.editedUtterances$.pipe(take(1)).subscribe(arr => {
      let updatedArr: Utterance[];

        if (hasItem(arr, u, utteranceSomewhatSimilar)) { // if we found similar item
          // replace that similar item
          updatedArr = arr.slice();
          const index = getPosition(arr, u, utteranceSomewhatSimilar);
          updatedArr.splice(index, 1, u);
        } else { // else it means that item is new
          updatedArr = arr.concat([u]);
        }

        // publish updated array
        this.editedUtterances$.next(updatedArr);
      }
    );
  }

  private replaceUtterance(oldVersion: Utterance, newVersion: Utterance): void {
    if (oldVersion === newVersion) {
      return;
    }

    // get array
    this.editedUtterances$.pipe(take(1)).subscribe(arr => {
        const updatedArr = arr.slice(); // copy

        // when edited value is already elsewhere
        if (hasItem(arr, newVersion, utteranceSomewhatSimilar)) {

          // update value at targeted position
          const targetedIndex = getPosition(arr, newVersion, utteranceSomewhatSimilar);
          updatedArr.splice(targetedIndex, 1, newVersion);

          // remove edited position because value is now represented in another existing position
          const prevIndex = getPosition(arr, oldVersion, utteranceEquals);
          updatedArr.splice(prevIndex, 1);

        } else { // when edited value is not elsewhere

          // replace value at edited position
          const prevIndex = getPosition(arr, oldVersion, utteranceEquals);
          updatedArr.splice(prevIndex, 1, newVersion);
        }

        // publish updated array
        this.editedUtterances$.next(updatedArr);
      }
    );
  }

  edit(utterance: Utterance): void {

    const origValue = utterance || '';

    // ask user to modify its utterance
    const dialogRef = this.dialog.openDialog(
      EditUtteranceComponent,
      {
        context:
          {
            value: '' + origValue,
            title: 'Edit training question'
          }
      }
    );

    // wait for user response
    dialogRef.onClose
      .pipe(take(1), takeUntil(this.destroy$), filter(notCancelled))
      .subscribe((result: ValidUtteranceResult) => {

        const newVersion: Utterance = '' + (result.value || '');

        this.replaceUtterance(utterance, newVersion);
    });
  }

  remove(utterance: Utterance): void {
    this.removeFromUtterances(utterance);
  }

  utteranceLookupFor(utterances: Utterance[]): (string) => (Utterance | null) {
    return search => {
      return utterances.filter(u => somewhatSimilar(u, search))[0] || null;
    };
  }

  async addUtterance(): Promise<any> {

    const allUtterances = await this.editedUtterances$.pipe(take(1)).toPromise();

    const dialogRef = this.dialog.openDialog(
      EditUtteranceComponent,
      {
        context:
          {
            value: '',
            title: 'New training question',
            lookup: this.utteranceLookupFor(allUtterances)
          }
      }
    );
    dialogRef.onClose
      .pipe(take(1), takeUntil(this.destroy$), filter(notCancelled))
      .subscribe((result: ValidUtteranceResult) => {

        this.appendToUtterances( ''+ (result?.value || ''));
    });
  }
}
