import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'tock-model-quality-tabs',
  template: '<nb-route-tabset></nb-route-tabset>'
})
export class ModelQualityTabsComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit() {
    if (this.router.routerState.snapshot.url.endsWith('/quality')) {
      this.router.navigateByUrl('/quality/log-stats');
    }
  }
}
