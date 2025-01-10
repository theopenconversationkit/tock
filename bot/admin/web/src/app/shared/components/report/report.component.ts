import { Component, Input, OnInit } from '@angular/core';
import { ActionReport } from '../../model/dialog-data';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'tock-report',
  templateUrl: './report.component.html',
  styleUrl: './report.component.scss'
})
export class ReportComponent implements OnInit {
  loading: boolean = true;

  @Input() actionReport: ActionReport;

  constructor(private dialogRef: NbDialogRef<ReportComponent>) {}

  ngOnInit(): void {
    this.loading = false;
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
