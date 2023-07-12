import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StateService } from '../../core-nlp/state.service';
import { UserRole } from '../../model/auth';
import { TabLink } from '../../shared/utils';

const managementTab = new TabLink('management', 'Faq management', 'book-open-outline');
const trainingTab = new TabLink('training', 'Faq training', 'checkmark-square-outline');

@Component({
  selector: 'tock-faq-tabs',
  templateUrl: './faq-tabs.component.html',
  styleUrls: ['./faq-tabs.component.scss']
})
export class FaqTabsComponent implements OnInit {
  UserRole = UserRole;
  tabLinks = [];

  constructor(private router: Router, public state: StateService) {}

  ngOnInit() {
    if (this.state.hasRole(UserRole.faqBotUser)) this.tabLinks.push(managementTab);
    if (this.state.hasRole(UserRole.faqNlpUser)) this.tabLinks.push(trainingTab);
  }
}
