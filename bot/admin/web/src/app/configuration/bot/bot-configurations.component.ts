/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, ElementRef, OnInit, ViewChild} from "@angular/core";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {
  BotApplicationConfiguration,
  BotConfiguration,
  ConnectorType,
  UserInterfaceType
} from "../../core/model/configuration";
import { MatDialog } from "@angular/material/dialog";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";
import {StateService} from "../../core-nlp/state.service";
import { NbToastrService } from '@nebular/theme';

@Component({
  selector: 'tock-bot-configurations',
  templateUrl: './bot-configurations.component.html',
  styleUrls: ['./bot-configurations.component.css']
})
export class BotConfigurationsComponent implements OnInit {

  newApplicationConfiguration: BotApplicationConfiguration;
  configurations: BotConfiguration[];
  displayTestConfigurations: boolean = false;

  //used to copy to clipboard
  @ViewChild('copy') tmpTextArea: ElementRef;

  constructor(private state: StateService,
              private botConfiguration: BotConfigurationService,
              private toastrService: NbToastrService,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.load();
  }

  private load() {
    this.botConfiguration.configurations.subscribe(confs => {
      const r = new Map<string, BotApplicationConfiguration[]>();
      confs.forEach(c => {
        const a = r.get(c.name);
        if (!a) {
          r.set(c.name, [c]);
        } else {
          a.push(c);
        }
      });
      const bots = this.botConfiguration.bots.getValue();
      this.configurations = Array.from(r.values()).map(
        e => {
          const existingConf = bots.find(b => b.name === e[0].name);
          if (existingConf) {
            existingConf.configurations = e;
            return existingConf;
          }
          const c = e[0];
          return new BotConfiguration(c.botId, c.name, c.namespace, c.nlpModel, e)
        }
      );
    });
  }

  isFirstLevelConfiguration(bot: BotConfiguration, conf: BotApplicationConfiguration): boolean {
    return conf.targetConfigurationId === null;
  }

  prepareCreate() {
    this.newApplicationConfiguration = new BotApplicationConfiguration(
      this.state.currentApplication.name,
      this.state.currentApplication.name,
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      new ConnectorType("messenger", UserInterfaceType.textChat),
      this.state.currentApplication.name,
      new Map<string, string>());
  }

  cancelCreate() {
    this.newApplicationConfiguration = null;
  }

  refresh() {
    this.botConfiguration.updateConfigurations();
    this.toastrService.show(`Configurations reloaded`, "Refresh", {duration: 2000});
  }

  create() {
    //black magic? welcome to the js world! :)
    const param = this.newApplicationConfiguration.parameters;
    Object.keys(param).forEach(k => {
      param.set(k, param[k])
    });

    this.botConfiguration.saveConfiguration(this.newApplicationConfiguration)
      .subscribe(_ => {
        this.botConfiguration.updateConfigurations();
        this.toastrService.show(`Configuration created`, "Creation", {duration: 5000});
        this.newApplicationConfiguration = null;
      });
  }

  update(conf: BotApplicationConfiguration) {
    this.botConfiguration.saveConfiguration(conf)
      .subscribe(_ => {
        this.botConfiguration.updateConfigurations();
        this.toastrService.show(`Configuration updated`, "Update", {duration: 5000});
      });
  }

  remove(conf: BotApplicationConfiguration) {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Delete the configuration`,
        subtitle: "Are you sure?",
        action: "Remove"
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.botConfiguration.deleteConfiguration(conf)
          .subscribe(_ => {
            this.botConfiguration.updateConfigurations();
            this.toastrService.show(`Configuration deleted`, "Delete", {duration: 5000});
          });
      }
    });
  }

  saveBot(bot: BotConfiguration) {
    this.botConfiguration.saveBot(bot).subscribe(_ => {
        this.toastrService.show(`Webhook saved`, "Save", {duration: 5000});
        this.botConfiguration.updateConfigurations();
      }
    );
  }

  copyToClipboard(bot: BotConfiguration) {
    const t = this.tmpTextArea.nativeElement;
    t.style.display = "block";
    const text = bot.apiKey;
    t.value = text;
    t.select();
    let successful = false;
    try {
      successful = document.execCommand('copy');
    } catch (err) {
      //do nothing
    }
    t.style.display = "none";
    this.toastrService.show(successful ? `${text} copied to clipboard` : `Unable to copy to clipboard`, "Clipboard", {duration: 2000})
  }

}
