import { Component, Input, OnDestroy, SimpleChanges } from '@angular/core';
import { Subject, take, takeUntil } from 'rxjs';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../../core/model/configuration';

@Component({
  selector: 'tock-bot-configuration-selector',
  templateUrl: './bot-configuration-selector.component.html',
  styleUrl: './bot-configuration-selector.component.scss'
})
export class BotConfigurationSelectorComponent implements OnDestroy {
  destroy = new Subject();

  allConfigurations: BotApplicationConfiguration[];

  currentRestConfiguration: BotApplicationConfiguration;
  currentConfiguration: BotApplicationConfiguration;

  botNames: string[];

  currentBotName: string;

  @Input() configurationId: string;

  constructor(private botConfiguration: BotConfigurationService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.allConfigurations) {
      this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((conf) => {
        this.allConfigurations = conf;
        this.initConfigurations();
      });

      return;
    }

    if (changes.configurationId && changes.configurationId.previousValue! == changes.configurationId.currentValue) {
      console.log(changes.configurationId);
      this.initConfigurations();
    }
  }

  initConfigurations(): void {
    const initConfiguration = this.allConfigurations.find((conf) => {
      return conf._id === this.configurationId;
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

    const retainedConfs = this.allConfigurations
      .filter((c) => c.targetConfigurationId == null)
      .sort((c1, c2) => c1.applicationId.localeCompare(c2.applicationId));

    this.botNames = Array.from(new Set(retainedConfs.map((c) => c.name))).sort();
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
