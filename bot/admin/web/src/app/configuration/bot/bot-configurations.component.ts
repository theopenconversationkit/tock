/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, OnInit} from "@angular/core";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {BotApplicationConfiguration, ConnectorType, UserInterfaceType} from "../../core/model/configuration";
import {MatDialog, MatSnackBar} from "@angular/material";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";
import {StateService} from "../../core-nlp/state.service";

@Component({
  selector: 'tock-bot-configurations',
  templateUrl: './bot-configurations.component.html',
  styleUrls: ['./bot-configurations.component.css']
})
export class BotConfigurationsComponent implements OnInit {

  newApplicationConfiguration: BotApplicationConfiguration;
  configurations: BotConfiguration[];
  displayTestConfigurations: boolean = false;

  constructor(private state: StateService,
              private botConfiguration: BotConfigurationService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.botConfiguration.configurations.subscribe(confs => {
      const r = new Map();
      confs.forEach(c => {
        const a = r.get(c.botId);
        if (!a) {
          r.set(c.botId, [c]);
        } else {
          a.push(c);
        }
      });
      this.configurations = Array.from(r).map(e => new BotConfiguration(e[0], e[1]));
    });
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
    this.snackBar.open(`Configurations reloaded`, "Refresh", {duration: 1000});
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
        this.snackBar.open(`Configuration created`, "Creation", {duration: 5000});
        this.newApplicationConfiguration = null;
      });
  }

  update(conf: BotApplicationConfiguration) {
    this.botConfiguration.saveConfiguration(conf)
      .subscribe(_ => {
        this.botConfiguration.updateConfigurations();
        this.snackBar.open(`Configuration updated`, "Update", {duration: 5000});
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
            this.snackBar.open(`Configuration deleted`, "Delete", {duration: 5000});
          });
      }
    });


  }

}

export class BotConfiguration {
  constructor(public botId: string, public configurations: BotApplicationConfiguration[]) {

  }
}
