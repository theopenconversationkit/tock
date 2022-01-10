import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ReplaySubject} from 'rxjs';
import {StateService} from 'src/app/core-nlp/state.service';
import {WithSidePanel} from '../common/mixin/with-side-panel';
import {blankFrequentQuestion, FrequentQuestion, QaStatus} from '../common/model/frequent-question';
import {FaqQaFilter, QaGridComponent} from './qa-grid/qa-grid.component';
import {QaSidebarEditorService} from './sidebars/qa-sidebar-editor.service';
import { truncate } from '../common/util/string-utils';
import { DialogService } from 'src/app/core-nlp/dialog.service';

// Specific action payload
export type EditorTabName = 'Info' | 'Answer' | 'Question';

@Component({
  selector: 'tock-qa',
  templateUrl: './qa.component.html',
  styleUrls: ['./qa.component.scss']
})
export class QaComponent extends WithSidePanel() implements OnInit, OnDestroy {

  activeQaTab: EditorTabName = 'Info';

  applicationName: string;
  currentItem?: FrequentQuestion;

  editorPanelName?: string;
  editorFormValid = false;

  public filter: FaqQaFilter;
  @ViewChild(QaGridComponent) grid;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService,
    private readonly sidebarEditorService: QaSidebarEditorService,
    private readonly dialog: DialogService,
  ) {
    super();
  }

  ngOnInit(): void {
    this.filter = {
      sort: [],
      search: null,
      clone: function () {
        return {...this};
      }
    };

    this.applicationName = this.state.currentApplication.name;
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  search(filter: Partial<FaqQaFilter>): void {

    this.filter.search = filter.search;
    this.filter.sort = filter.sort;

    this.grid.refresh();
  }

  openImportSidepanel() {
    this.dock("import");
  }

  edit(fq: FrequentQuestion): void {
    this.editorPanelName = 'Edit QA';
    this.currentItem = fq;

    this.dock("edit");
  }

  onEditorValidityChanged(value: boolean): void {
    window.setTimeout(() => { // ExpressionChangedAfterItHasBeenCheckedError workaround
      this.editorFormValid = value;
    }, 0);

  }

  openNewSidepanel() {
    this.editorPanelName = 'New QA';
    this.currentItem = blankFrequentQuestion();
    this.activeQaTab = 'Info';

    this.dock("edit");
  }

  details(fq: FrequentQuestion) {
    console.log("qa", fq);
  }

  activateEditorTab(tabName: EditorTabName): void {
    this.activeQaTab = tabName;
  }

  async save(): Promise<any> {
    const fq = await this.sidebarEditorService.save(this.destroy$);

    this.dialog.notify(`Saved`,
      truncate(fq.title || ''), {duration: 2000, status: "basic"});

    this.grid.refresh();
  }

}
