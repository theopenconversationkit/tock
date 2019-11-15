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
import {AuthService} from "../auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  displayLogin:boolean = false;
  email: string;
  password: string;
  errorMessage: string;
  sendLogin: boolean;

  constructor(public authService: AuthService, private router: Router) {
  }

  ngOnInit(): void {
    if (this.authService.isSSO()) {
      this.router.navigateByUrl("/");
    } else {
      this.displayLogin = true;
    }
  }

  onSubmit() {
    if (!this.password || this.password.trim().length === 0) {
      this.errorMessage = "Empty password";
    } else {
      this.errorMessage = null;
      this.sendLogin = true;
      this.authService.authenticate(this.email, this.password).subscribe(
        response => {
          this.sendLogin = false;
          if (response) {
            this.router.navigateByUrl(this.authService.getRedirectUrl());
          } else {
            this.errorMessage = "Invalid credentials";
          }
        },
        error => {
          this.sendLogin = false;
        }
      );
    }
    return false;
  }

}
