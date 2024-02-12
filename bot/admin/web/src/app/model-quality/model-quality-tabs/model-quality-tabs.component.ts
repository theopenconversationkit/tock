import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

class TabLink {
  constructor(public route: string, public title: string, public icon?: string) {}
}

const tabs = [
  new TabLink('log-stats', 'Model Stats', 'activity-outline'),
  new TabLink('intent-quality', 'Intent Distance', 'pantone-outline'),
  new TabLink('count-stats', 'Count Stats', 'hash-outline'),
  new TabLink('model-builds', 'Model Builds', 'save-outline'),
  new TabLink('test-builds', 'Test Trends', 'trending-down-outline'),
  new TabLink('test-intent-errors', 'Test Intent Errors', 'alert-triangle-outline'),
  new TabLink('test-entity-errors', 'Test Entity Errors', 'alert-triangle-outline')
];

@Component({
  selector: 'tock-model-quality-tabs',
  templateUrl: './model-quality-tabs.component.html',
  styleUrls: ['./model-quality-tabs.component.scss']
})
export class ModelQualityTabsComponent implements OnInit {
  tabLinks = tabs;

  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/quality')) {
      this.router.navigateByUrl('/quality/log-stats');
    }
  }
}
