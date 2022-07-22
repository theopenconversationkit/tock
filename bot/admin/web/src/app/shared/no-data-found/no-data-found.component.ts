import { APP_BASE_HREF } from '@angular/common';
import { Component, Inject, Input } from '@angular/core';

@Component({
  selector: 'tock-no-data-found',
  templateUrl: './no-data-found.component.html',
  styleUrls: ['./no-data-found.component.scss']
})
export class NoDataFoundComponent {
  @Input() title: string = 'No data found';
  @Input() message?: string;

  @Inject(APP_BASE_HREF) BASE_HREF: string;
}
