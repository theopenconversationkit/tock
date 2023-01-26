import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TabLink } from '../../shared/utils';

const tabs = [new TabLink('board', 'Metrics', 'pie-chart-outline'), new TabLink('indicators', 'Indicators', 'compass-outline')];

@Component({
  selector: 'tock-metrics-tabs',
  templateUrl: './metrics-tabs.component.html',
  styleUrls: ['./metrics-tabs.component.scss']
})
export class MetricsTabsComponent implements OnInit {
  metricsTabLinks = tabs;

  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/business-metrics')) {
      this.router.navigateByUrl('/business-metrics/board');
    }
  }
}
