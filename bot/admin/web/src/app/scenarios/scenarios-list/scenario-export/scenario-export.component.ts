import { DatePipe } from '@angular/common';
import { Component, Input, OnDestroy } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { exportJsonDump, normalizedSnakeCase } from '../../commons/utils';
import { Saga, Scenario, SCENARIO_STATE } from '../../models';

@Component({
  selector: 'tock-scenario-export',
  templateUrl: './scenario-export.component.html',
  styleUrls: ['./scenario-export.component.scss']
})
export class ScenarioExportComponent implements OnDestroy {
  destroy = new Subject();

  @Input() sagas: Saga[];
  constructor(
    protected state: StateService,
    public dialogRef: NbDialogRef<ScenarioExportComponent>,
    private datePipe: DatePipe
  ) {}

  form: FormGroup = new FormGroup({
    allOrCurrentOnly: new FormControl('one')
  });

  export() {
    const mode = this.form.value.allOrCurrentOnly;
    if (mode === 'all') {
      this.sagas.forEach((saga) => {
        saga.scenarios.forEach((scenario) => {
          this.download(scenario);
        });
      });
    } else {
      let exportables = [];
      this.sagas.forEach((saga) => {
        let current = saga.scenarios.find((scenario) => scenario.state === SCENARIO_STATE.current);
        if (current) exportables.push(current);
        else {
          const drafts = saga.scenarios.filter(
            (scenario) => scenario.state === SCENARIO_STATE.draft
          );
          if (drafts.length) {
            exportables.push(
              drafts.sort((a, b) => {
                return new Date(b.createDate).getTime() - new Date(a.createDate).getTime();
              })[0]
            );
          } else {
            exportables.push(saga.scenarios[saga.scenarios.length - 1]);
          }
        }
      });
      exportables.forEach((scenario) => {
        this.download(scenario);
      });
    }

    this.dialogRef.close();
  }

  download(scenario: Scenario) {
    const fileName = [
      this.state.currentApplication.name,
      'SCENARIO',
      normalizedSnakeCase(scenario.name),
      scenario.state,
      this.datePipe.transform(scenario.createDate, 'yyyy-MM-dd')
    ].join('_');

    exportJsonDump(scenario, fileName);
  }

  cancel() {
    this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
