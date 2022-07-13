import { Component, Injectable, Input, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { Scenario } from '../../models';

@Component({
  selector: 'scenario-production',
  templateUrl: './scenario-production.component.html',
  styleUrls: ['./scenario-production.component.scss']
})
export class ScenarioProductionComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() scenario: Scenario;

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
