import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'tock-metrics-tabs',
  template: '<nb-route-tabset></nb-route-tabset>'
})
export class MetricsTabsComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/business-metrics')) {
      this.router.navigateByUrl('/business-metrics/board');
    }
  }
}
