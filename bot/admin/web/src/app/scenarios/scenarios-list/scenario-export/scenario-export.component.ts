import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

import { StateService } from '../../../core-nlp/state.service';
import { ExportableScenarioGroup, ScenarioGroup, SCENARIO_STATE } from '../../models';
import { ScenarioService } from '../../services';

@Component({
  selector: 'tock-scenario-export',
  templateUrl: './scenario-export.component.html',
  styleUrls: ['./scenario-export.component.scss']
})
export class ScenarioExportComponent {
  @Input() scenariosGroups: ScenarioGroup[];
  @Output() onClose = new EventEmitter<boolean>();

  form: FormGroup = new FormGroup({
    allOrCurrentOnly: new FormControl<'all' | 'one'>('one')
  });

  constructor(protected state: StateService, private scenarioService: ScenarioService) {}

  export(): void {
    let exportableGroups: ExportableScenarioGroup[] = [];
    const mode = this.form.value.allOrCurrentOnly;

    if (mode === 'all') {
      this.scenariosGroups.forEach((group) => {
        exportableGroups.push({ id: group.id, versionsIds: group.versions.map((v) => v.id) });
      });
    } else {
      this.scenariosGroups.forEach((group) => {
        let current = group.versions.find((scenarioVersion) => scenarioVersion.state === SCENARIO_STATE.current);
        if (current) {
          exportableGroups.push({ id: group.id, versionsIds: [current.id] });
        } else {
          const drafts = group.versions.filter((scenarioVersion) => scenarioVersion.state === SCENARIO_STATE.draft);
          if (drafts.length) {
            let latest = drafts.sort((a, b) => {
              return new Date(b.creationDate).getTime() - new Date(a.creationDate).getTime();
            })[0];
            exportableGroups.push({ id: group.id, versionsIds: [latest.id] });
          } else {
            let residual = group.versions[group.versions.length - 1];
            exportableGroups.push({ id: group.id, versionsIds: [residual.id] });
          }
        }
      });
    }

    this.scenarioService.loadScenariosAndDownload(this.scenariosGroups, exportableGroups);

    this.close();
  }

  close(): void {
    this.onClose.emit(true);
  }
}
