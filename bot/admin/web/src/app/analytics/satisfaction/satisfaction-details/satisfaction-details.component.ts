/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { AnalyticsService } from '../../analytics.service';
import { RatingReportQueryResult } from './RatingReportQueryResult';
import { DialogsListComponent } from '../../dialogs/dialogs-list/dialogs-list.component';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-satisfaction-details',
  templateUrl: './satisfaction-details.component.html',
  styleUrls: ['./satisfaction-details.component.scss']
})
export class SatisfactionDetailsComponent implements OnInit, AfterViewInit {
  private readonly destroy$: Subject<boolean> = new Subject();

  loading = false;

  count: string = '';

  ratingFilter: number[] = [1, 2, 3, 4, 5];

  satisfactionStat: RatingReportQueryResult;

  @ViewChild('dialogsList') dialogsList: DialogsListComponent;

  constructor(private analytics: AnalyticsService) {}

  ngOnInit(): void {
    this.analytics
      .getSatisfactionStat()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: RatingReportQueryResult) => {
          this.satisfactionStat = data.ratingBot ? data : null;
        },
        error: (err) => console.error(err)
      });
  }

  ngAfterViewInit() {
    this.dialogsList.totalDialogsCount.pipe(takeUntil(this.destroy$)).subscribe((count) => {
      this.count = count;
    });
  }

  refresh() {
    this.dialogsList.refresh();
  }

  exportDialogs() {
    this.dialogsList.exportDialogs();
  }

  updateRatingFilter(event: any) {
    if (event.target.checked) {
      if (this.ratingFilter.length == 5) {
        this.ratingFilter = [];
      }
      this.ratingFilter = this.ratingFilter.concat(event.currentTarget.value);
    } else {
      if (this.ratingFilter.length == 1) {
        this.ratingFilter = [1, 2, 3, 4, 5];
      } else {
        this.ratingFilter = this.ratingFilter.filter((item) => item != event.currentTarget.value);
      }
    }
  }

  getNbUsersByNote(note: number): number {
    const res = this.satisfactionStat.ratingDetails.find((it) => it.rating == note);
    return res ? res.nbUsers : 0;
  }

  getStarArray(): any[] {
    const roundedRating = Math.round(this.satisfactionStat.ratingBot);
    return Array(roundedRating).fill(0);
  }

  getStyles() {
    const percent = (this.satisfactionStat.ratingBot / 5) * 100;
    return {
      background: `conic-gradient(${this.getColorFromRating(this.satisfactionStat.ratingBot)} ${percent}%, #e6e6e6 ${percent}% 100%)`
    };
  }

  getColorFromRating(rating: number): string {
    const maxRating = 5; // Maximum rating
    const minHue = 0; // Minimum hue (red)
    const maxHue = 120; // Maximum hue (green)

    //  Calculate the hue based on the rating (from 0 to 120)
    const hue = (rating / maxRating) * (maxHue - minHue) + minHue;

    // Convert the hue to a CSS color
    return `hsl(${hue}, 70%, 50%)`;
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
