import { Component, OnInit } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { UserRole } from '../../model/auth';
import { TabLink } from '../../shared/utils';

const tabLinks = [
  new TabLink('sources', 'Rag sources', 'cloud-download-outline'),
  new TabLink('exclusions', 'Rag exclusions', 'alert-triangle-outline'),
  new TabLink('settings', 'Rag settings', 'settings-outline')
];

@Component({
  selector: 'tock-rag-tabs',
  templateUrl: './rag-tabs.component.html',
  styleUrls: ['./rag-tabs.component.scss']
})
export class RagTabsComponent implements OnInit {
  tabLinks: TabLink[] = tabLinks;

  constructor(private state: StateService) {
    if (!state.hasRole(UserRole.admin)) {
      this.tabLinks = this.tabLinks.filter((t) => t.route !== 'sources');
    }
  }

  ngOnInit(): void {}
}
