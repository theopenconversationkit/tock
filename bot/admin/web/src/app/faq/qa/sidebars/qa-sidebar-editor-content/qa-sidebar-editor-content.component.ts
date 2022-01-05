import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChange} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {NbTagInputAddEvent} from '@nebular/theme/components/tag/tag-input.directive';
import {NbTagComponent} from '@nebular/theme/components/tag/tag.component';
import {BehaviorSubject, combineLatest, Observable, ReplaySubject} from 'rxjs';
import {filter, takeUntil } from 'rxjs/operators';
import {debounceTime, map, take} from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { EditUtteranceResult, notCancelled, ValidUtteranceResult } from 'src/app/faq/common/components/edit-utterance/edit-utterance-result';
import { EditUtteranceComponent } from 'src/app/faq/common/components/edit-utterance/edit-utterance.component';
import {FrequentQuestion, Utterance, utteranceEquals, utteranceEquivalent} from 'src/app/faq/common/model/frequent-question';
import {EditorTabName, QaSidebarEditorService} from '../qa-sidebar-editor.service';
import {getPosition, hasItem} from '../../../common/util/array-utils';
import { verySimilar } from 'src/app/faq/common/util/string-utils';

// Simple builder for text 'utterance predicate'
function textMatch(text: string): (Utterance) => boolean {
  if (!text?.trim()) {
    return _ => true;
  }
  const lowerText = text.trim().toLowerCase();

  return u => {
    const lowerUtteranceText = (u.value || '').toLowerCase();
    return lowerUtteranceText.includes(lowerText);
  };
}

/**
 * Content for Q&A Edition sidebar
 *
 * Handle both 'New' and 'Edit existing' cases
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
  fq?: FrequentQuestion;

  /* Form Data */

  tags: Set<string> = new Set(["test"]);
  editedUtterances$: ReplaySubject<Utterance[]> = new ReplaySubject(1);
  tabName: EditorTabName = 'Info';

  readonly newFaqForm = new FormGroup({
    name: new FormControl('', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(40)
    ]),
    description: new FormControl('', [
      Validators.maxLength(500)
    ]),
  });

  /* Search */

  search?: string;
  searchSubject$: BehaviorSubject<string> = new BehaviorSubject('');
  displayedUtterances$: Observable<Utterance[]>;

  constructor(
    private readonly sidebarEditorService: QaSidebarEditorService,
    private readonly dialog: DialogService,
  ) {
  }

  ngOnInit(): void {
    this.sidebarEditorService.whenSwitchTab(this.destroy$)
      .subscribe(value => {
        this.tabName = value;
      });

    this.observeUtteranceSearch();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnChanges(changes: { [key: string]: SimpleChange }): any {
    this.updateForm(changes?.fq?.currentValue);
  }

  observeUtteranceSearch() {
    this.displayedUtterances$ = combineLatest(this.editedUtterances$, this.searchSubject$).pipe( // unsubscribe handled by angular template mechanism
      debounceTime(200),
      map(([utterances, search]) => {
        const res = utterances.filter(textMatch(search));

        res.sort((a, b) => (a?.value || '').localeCompare(b?.value || ''))
        return res;
      })
    );
  }

  updateForm(fq?: FrequentQuestion): void {
    if (this.fq) {
      const utterances = JSON.parse(JSON.stringify(fq.utterances)); // deep clone
      this.editedUtterances$.next(utterances);

      this.newFaqForm.setValue({
        name: '' + fq.title,
        description: ''
      });
    } else {
      this.editedUtterances$.next([]);
      this.newFaqForm.setValue({
        name: '',
        description: ''
      });
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

  utteranceSearchChange(e) {
    this.searchSubject$.next(e.target.value);
  }

  private removeFromUtterances(u: Utterance): void {

    // get array
    this.editedUtterances$.pipe(take(1)).subscribe(arr => {
        // find existing item location
        const index = getPosition(arr, u, utteranceEquivalent);

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

      // if we found similar item
      if (hasItem(arr, u, utteranceEquivalent)) {

        // replace that similar item
        const index = getPosition(arr, u, utteranceEquivalent);
        const updatedArr = arr.slice();
        updatedArr.splice(index, 1);
        return;
      }

      // Append created utterance
        const updatedArr = arr.concat([u]);

        // publish updated array
        this.editedUtterances$.next(updatedArr);
      }
    );
  }

  private replaceUtterance(oldVersion: Utterance, newVersion: Utterance): void {
    if (oldVersion.value === newVersion.value) {
      return;
    }

    // get array
    this.editedUtterances$.pipe(take(1)).subscribe(arr => {
        const updatedArr = arr.slice(); // copy
        const index = getPosition(arr, oldVersion, utteranceEquivalent);

        // when we match another item
        if (hasItem(arr, newVersion, utteranceEquivalent)) {

          // just remove previous value
          const index = getPosition(arr, oldVersion, utteranceEquals);
          updatedArr.splice(index, 1);

        } else { // when new value is unique

          // replace by a new version
          updatedArr.splice(index, 1, newVersion);
        }

        // publish updated array
        this.editedUtterances$.next(updatedArr);
      }
    );
  }

  edit(utterance: Utterance): void {

    const origValue = utterance.value || '';

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

        const newVersion: Utterance = {
          value: '' + (result.value || '')
        };

        this.replaceUtterance(utterance, newVersion);
    });
  }

  remove(utterance: Utterance): void {
    this.removeFromUtterances(utterance);
  }

  utteranceLookupFor(utterances: Utterance[]): (string) => (Utterance | null) {
    return search => {
      return utterances.filter(u => verySimilar(u.value, search))[0] || null;
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

        this.appendToUtterances( {
          value: ''+ (result.value || '')
        });
    });
  }
}
