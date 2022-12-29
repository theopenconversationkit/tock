/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import {Component, OnInit} from '@angular/core';
import {AnalyticsService} from "../../analytics.service";
import {RatingReportQueryResult} from "./RatingReportQueryResult";

@Component({
  selector: 'tock-satisfaction-details',
  templateUrl: './satisfaction-details.component.html',
  styleUrls: ['./satisfaction-details.component.css']
})
export class SatisfactionDetailsComponent implements OnInit {

  ratingFilter: number[] = [1,2,3,4,5];

  satisfactionStat: RatingReportQueryResult;

  constructor(private analytics: AnalyticsService) {
  }

  ngOnInit(): void {
    this.analytics.getSatisfactionStat().subscribe((data : RatingReportQueryResult) => {
        this.satisfactionStat = data.ratingBot ? data : null;
      },
      err => console.error(err));
  }


  updateRatingFilter(event: any) {
    if (event.target.checked) {
      if (this.ratingFilter.length == 5) {
        this.ratingFilter = [];
      }
      this.ratingFilter = this.ratingFilter.concat(event.currentTarget.value)
    } else {
      if (this.ratingFilter.length == 1) {
        this.ratingFilter = [1, 2, 3, 4, 5];
      } else {
        this.ratingFilter = this.ratingFilter.filter(item => item != event.currentTarget.value);
      }
    }
  }

  getNbUsersByNote(note: number): number {
    const res = this.satisfactionStat.ratingDetails.find(it => it.rating == note)
    return res ? res.nbUsers : 0;
  }
}

