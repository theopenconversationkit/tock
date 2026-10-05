import { Component, OnDestroy, OnInit } from '@angular/core';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration, BotConfiguration, ConnectorType, UserInterfaceType } from '../../core/model/configuration';
import { StateService } from '../../core-nlp/state.service';
import { NbToastrService } from '@nebular/theme';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { Subject, takeUntil } from 'rxjs';
import { ChoiceDialogComponent } from '../../shared/components';
import { TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'tock-bot-configurations',
  templateUrl: './bot-configurations.component.html',
  styleUrls: ['./bot-configurations.component.scss']
})
export class BotConfigurationsComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  newApplicationConfiguration: BotApplicationConfiguration;
  configurations: BotConfiguration[];
  displayTestConfigurations = false;

  constructor(
    public state: StateService,
    private botConfiguration: BotConfigurationService,
    private dialogService: DialogService,
    private toastrService: NbToastrService,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      const botConfigurationsByName = new Map<string, BotApplicationConfiguration[]>();
      confs.forEach((c) => {
        const configs = botConfigurationsByName.get(c.name);
        if (!configs) {
          botConfigurationsByName.set(c.name, [c]);
        } else {
          configs.push(c);
        }
      });
      const bots = this.botConfiguration.bots.getValue();
      this.configurations = Array.from(botConfigurationsByName.values()).map((botConfigurations) => {
        const existingConf = bots.find((botConfig) => botConfig.name === botConfigurations[0].name);
        if (existingConf) {
          existingConf.configurations = botConfigurations;
          return existingConf;
        }
        const firstBotConfiguration = botConfigurations[0];
        return new BotConfiguration(
          firstBotConfiguration.botId,
          firstBotConfiguration.name,
          firstBotConfiguration.namespace,
          firstBotConfiguration.nlpModel,
          botConfigurations
        );
      });
    });
  }

  isFirstLevelConfiguration(bot: BotConfiguration, conf: BotApplicationConfiguration): boolean {
    return conf.targetConfigurationId === null || conf.targetConfigurationId == undefined;
  }

  prepareCreate(): void {
    this.newApplicationConfiguration = new BotApplicationConfiguration(
      this.state.currentApplication.name,
      this.state.currentApplication.name,
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      new ConnectorType('messenger', UserInterfaceType.textChat),
      this.state.currentApplication.name,
      new Map<string, string>()
    );
  }

  cancelCreate(): void {
    this.newApplicationConfiguration = null;
  }

  refresh(): void {
    this.botConfiguration.updateConfigurations();
    this.toastrService.show(this.transloco.translate('common.messages.success'), this.transloco.translate('common.actions.refresh'), {
      duration: 2000
    });
  }

  create(): void {
    const param = this.newApplicationConfiguration.parameters;
    Object.keys(param).forEach((k) => {
      param.set(k, param[k]);
    });

    this.botConfiguration.saveConfiguration(this.newApplicationConfiguration).subscribe((_) => {
      this.botConfiguration.updateConfigurations();
      this.toastrService.show(
        this.transloco.translate('configuration.bot-configurations.configurationCreated'),
        this.transloco.translate('common.actions.create'),
        {
          duration: 5000,
          status: 'success'
        }
      );
      this.newApplicationConfiguration = null;
    });
  }

  update(conf: BotApplicationConfiguration): void {
    this.botConfiguration.saveConfiguration(conf).subscribe({
      next: (_) => {
        this.botConfiguration.updateConfigurations();
        this.toastrService.show(
          this.transloco.translate('configuration.bot-configurations.configurationUpdated'),
          this.transloco.translate('common.actions.update'),
          {
            duration: 5000,
            status: 'success'
          }
        );
      },
      error: (error) => {
        this.botConfiguration.updateConfigurations();
      }
    });
  }

  remove(conf: BotApplicationConfiguration): void {
    const action = this.transloco.translate('common.actions.remove');
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `${this.transloco.translate('configuration.bot-configurations.deleteConfigurationTitle')} "${conf.name}"`,
        subtitle: this.transloco.translate('configuration.bot-configurations.deleteConfigurationSubtitle'),
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ],
        modalStatus: 'danger'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        this.botConfiguration.deleteConfiguration(conf).subscribe((_) => {
          this.botConfiguration.updateConfigurations();
          this.toastrService.show(
            this.transloco.translate('configuration.bot-configurations.configurationDeleted'),
            this.transloco.translate('common.actions.delete'),
            {
              duration: 5000,
              status: 'success'
            }
          );
        });
      }
    });
  }

  saveBot(bot: BotConfiguration): void {
    this.botConfiguration.saveBot(bot).subscribe((_) => {
      this.botConfiguration.updateConfigurations();
      this.toastrService.show(
        this.transloco.translate('configuration.bot-configurations.webhookSaved'),
        this.transloco.translate('common.actions.save'),
        { duration: 5000, status: 'success' }
      );
    });
  }

  copyToClipboard(bot: BotConfiguration): void {
    navigator.clipboard.writeText(bot.apiKey);
    this.toastrService.show(
      `${bot.apiKey} ${this.transloco.translate('configuration.bot-configurations.copiedToClipboard')}`,
      this.transloco.translate('configuration.bot-configurations.clipboardTitle'),
      {
        duration: 2000
      }
    );
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
