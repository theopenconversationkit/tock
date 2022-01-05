import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ReplaySubject} from 'rxjs';
import {StateService} from 'src/app/core-nlp/state.service';
import {WithSidePanel} from '../common/mixin/with-side-panel';
import {blankFrequentQuestion, FrequentQuestion, QaStatus} from '../common/model/frequent-question';
import {FaqQaFilter, QaGridComponent} from './qa-grid/qa-grid.component';
import {EditorTabName, QaSidebarEditorService} from './sidebars/qa-sidebar-editor.service';

@Component({
  selector: 'tock-qa',
  templateUrl: './qa.component.html',
  styleUrls: ['./qa.component.scss']
})
export class QaComponent extends WithSidePanel() implements OnInit, OnDestroy {

  activeQaTab: EditorTabName = 'Info';

  applicationName: string;
  currentItem?: FrequentQuestion;

  public filter: FaqQaFilter;
  @ViewChild(QaGridComponent) grid;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService,
    private readonly sidebarEditorService: QaSidebarEditorService,
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
    this.currentItem = fq;
    this.dock("edit");
  }

  openNewSidepanel() {
    this.currentItem = blankFrequentQuestion();
    this.dock("edit");
  }

  details(fq: FrequentQuestion) {
    console.log("qa", fq);
  }

  activateEditorTab(tabName: EditorTabName): void {
    this.activeQaTab = tabName;
    this.sidebarEditorService.switchTab(tabName);
  }

}
