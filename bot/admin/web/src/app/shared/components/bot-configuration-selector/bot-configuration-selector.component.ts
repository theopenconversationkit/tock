import { Component, EventEmitter, Input, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { Subject, take, takeUntil } from 'rxjs';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../../core/model/configuration';

export interface currentConfigurationSelection {
  configuration: BotApplicationConfiguration;
  restConfiguration: BotApplicationConfiguration | undefined;
}
@Component({
  selector: 'tock-bot-configuration-selector',
  templateUrl: './bot-configuration-selector.component.html',
  styleUrl: './bot-configuration-selector.component.scss'
})
export class BotConfigurationSelectorComponent implements OnDestroy {
  destroy = new Subject();

  allConfigurations: BotApplicationConfiguration[];

  configurations: BotApplicationConfiguration[];

  currentRestConfiguration: BotApplicationConfiguration;
  currentConfiguration: BotApplicationConfiguration;

  botNames: string[];

  currentBotName: string;

  @Input() currentConfigurationId: string;

  @Output() currentConfigurationChange = new EventEmitter<currentConfigurationSelection>();

  constructor(private botConfiguration: BotConfigurationService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.allConfigurations) {
      this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((conf) => {
        this.allConfigurations = conf;
        this.initConfigurations();
      });

      return;
    }

    if (changes.currentConfigurationId && changes.currentConfigurationId.previousValue !== changes.currentConfigurationId.currentValue) {
      this.initConfigurations();
    }
  }

  initConfigurations(): void {
    const initConfiguration = this.allConfigurations.find((conf) => {
      return conf._id === this.currentConfigurationId;
    });

    if (initConfiguration) {
      if (initConfiguration.targetConfigurationId) {
        this.currentRestConfiguration = initConfiguration;
        this.currentConfiguration = this.allConfigurations.find((conf) => {
          return conf._id === initConfiguration.targetConfigurationId;
        });
      } else {
        this.currentConfiguration = initConfiguration;
        this.currentRestConfiguration = this.allConfigurations.find((conf) => {
          return conf.targetConfigurationId === initConfiguration._id;
        });
      }

      this.currentBotName = this.currentConfiguration.name;
    }

    this.listConfigurations(this.currentConfiguration.name);
    this.botNames = Array.from(new Set(this.allConfigurations.map((c) => c.name))).sort();
  }

  listConfigurations(botName: string) {
    this.configurations = this.allConfigurations
      .filter((c) => c.targetConfigurationId == null)
      .filter((c) => c.name === botName)
      .sort((c1, c2) => c1.applicationId.localeCompare(c2.applicationId));
  }

  changeBotName() {
    this.listConfigurations(this.currentBotName);
    this.currentConfiguration = undefined;
    this.currentRestConfiguration = undefined;
  }

  changeCurrentConfiguration() {
    this.currentRestConfiguration = this.allConfigurations.find((conf) => {
      return conf.targetConfigurationId === this.currentConfiguration._id;
    });

    this.currentConfigurationChange.emit({
      configuration: this.currentConfiguration,
      restConfiguration: this.currentRestConfiguration
    });
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
