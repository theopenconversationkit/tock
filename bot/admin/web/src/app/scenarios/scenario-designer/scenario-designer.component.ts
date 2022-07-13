import {
  Component,
  ElementRef,
  HostListener,
  Injectable,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { Subject } from 'rxjs';
import { pluck, takeUntil } from 'rxjs/operators';
import { ScenarioDesignerService } from './scenario-conception/scenario-designer-service.service';
import {
  Scenario,
  scenarioItem,
  scenarioItemFrom,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE
} from '../models/scenario.model';
import { ScenarioService } from '../services/scenario.service';
import { ActivatedRoute, CanDeactivate, Router } from '@angular/router';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { ConfirmDialogComponent } from 'src/app/shared-nlp/confirm-dialog/confirm-dialog.component';
import { NbToastrService } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { entityColor, qualifiedName, qualifiedRole } from '../../model/nlp';
import { getContrastYIQ } from '../commons/utils';

const CANVAS_TRANSITION_TIMING = 300;

@Component({
  selector: 'scenario-designer',
  templateUrl: './scenario-designer.component.html',
  styleUrls: ['./scenario-designer.component.scss'],
  providers: [ScenarioDesignerService]
})
export class ScenarioDesignerComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @ViewChild('canvasWrapperElem') canvasWrapperElem: ElementRef;
  @ViewChild('canvasElem') canvasElem: ElementRef;

  scenarioId: number;
  scenario: Scenario;
  scenarioBackup: string;

  readonly SCENARIO_MODE = SCENARIO_MODE;
  readonly SCENARIO_ITEM_FROM_CLIENT = SCENARIO_ITEM_FROM_CLIENT;
  readonly SCENARIO_ITEM_FROM_BOT = SCENARIO_ITEM_FROM_BOT;

  qualifiedName = qualifiedName;

  constructor(
    private scenarioService: ScenarioService,
    private scenarioDesignerService: ScenarioDesignerService,
    route: ActivatedRoute,
    private router: Router,
    private toastrService: NbToastrService,
    protected state: StateService,
    private dialogService: DialogService
  ) {
    route.params
      .pipe(takeUntil(this.destroy), pluck('id'))
      .subscribe((id) => (this.scenarioId = +id));
  }

  ngOnInit(): void {
    this.scenarioService
      .getScenario(this.scenarioId)
      .pipe(takeUntil(this.destroy))
      .subscribe((data) => {
        if (typeof data.mode == 'undefined') data.mode = SCENARIO_MODE.writing;
        this.scenarioBackup = JSON.stringify(data);
        this.scenario = JSON.parse(JSON.stringify(data));

        this.switchMode(this.scenario.mode || SCENARIO_MODE.writing);

        if (!this.scenario.data) this.scenario.data = { scenarioItems: [], contexts: [] };
        if (!this.scenario.data.scenarioItems.length) {
          this.scenario.data.scenarioItems.push({
            id: 0,
            from: SCENARIO_ITEM_FROM_CLIENT,
            text: ''
          });
        }
        if (!this.scenario.data.contexts) {
          this.scenario.data.contexts = [];
        }
      });

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => {
      this.exit();
    });
  }

  switchMode(mode): void {
    this.scenario.mode = mode;
  }

  @ViewChild('tickStoryJsonTempModal') tickStoryJsonTempModal: TemplateRef<any>;
  goToProduction() {
    let tickStory = {
      name: this.scenario.name,
      sagaId: 321658,
      stateMachine: 'Soon to come...',
      primaryIntents: ['62bb118e49e78735af27aa98'],
      secondaryIntents: ['65sd99ze1sd6ert6df21se89', 'df5d58ze54ds875q45sdf89'],
      tickContexts: this.scenario.data.contexts,
      tickActions: []
    };

    this.scenario.data.scenarioItems.forEach((item) => {
      if (item.from == SCENARIO_ITEM_FROM_BOT && item.tickActionDefinition) {
        tickStory.tickActions.push(item.tickActionDefinition);
      }
    });

    console.log(tickStory);
    const tickStoryJson = JSON.stringify(tickStory, null, 4);

    this.dialogService.openDialog(this.tickStoryJsonTempModal, { context: tickStoryJson });
  }

  stringifiedCleanScenario(): string {
    return JSON.stringify(this.scenario, function (key, value) {
      if (key.indexOf('_') == 0) return undefined;
      return value;
    });
  }

  getCleanScenario(): Scenario {
    return JSON.parse(this.stringifiedCleanScenario());
  }

  save(exit: boolean = false): void {
    this.scenarioService.putScenario(this.scenarioId, this.getCleanScenario()).subscribe((data) => {
      this.toastrService.success(`Scenario successfully saved`, 'Success', {
        duration: 5000,
        status: 'success'
      });
      this.scenarioBackup = JSON.stringify(data);
      if (exit) this.exit();
    });
  }

  exit(): void {
    this.router.navigateByUrl('/scenarios');
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (!this.canDeactivate()) {
      $event.returnValue = true;
    }
  }

  canDeactivate(): boolean {
    return this.scenarioBackup == this.stringifiedCleanScenario();
  }
}

@Injectable()
export class ScenarioDesignerNavigationGuard implements CanDeactivate<any> {
  constructor(private dialogService: DialogService) {}

  canDeactivate(component: any) {
    const canDeactivate = component.canDeactivate();

    if (!canDeactivate) {
      const subject = new Subject<boolean>();
      const dialogResponseVerb = 'Exit';
      const modal = this.dialogService.openDialog(ConfirmDialogComponent, {
        context: {
          title: `You're about to leave without saving the changes`,
          subtitle: 'Are you sure?',
          action: dialogResponseVerb
        }
      });
      modal.onClose.subscribe((res) => {
        subject.next(res == dialogResponseVerb.toLowerCase());
      });

      return subject.asObservable();
    }
    return true;
  }
}
