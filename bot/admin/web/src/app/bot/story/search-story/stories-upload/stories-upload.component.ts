import { Component, OnInit } from '@angular/core';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { FileItem, FileUploader, ParsedResponseHeaders } from 'ng2-file-upload';
import { StateService } from '../../../../core-nlp/state.service';
import { BotService } from '../../../bot-service';

@Component({
  selector: 'tock-stories-upload',
  templateUrl: './stories-upload.component.html',
  styleUrls: ['./stories-upload.component.scss']
})
export class StoriesUploadComponent implements OnInit {
  uploader: FileUploader;
  loading: boolean;
  constructor(
    private toastrService: NbToastrService,
    public state: StateService,
    private bot: BotService,
    public dialogRef: NbDialogRef<StoriesUploadComponent>
  ) {}

  ngOnInit(): void {
    this.uploader = new FileUploader({ removeAfterUpload: true });
    this.uploader.onCompleteItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
      this.toastrService.show(`Dump uploaded`, 'Dump', { duration: 3000, status: 'success' });
      this.state.resetConfiguration();
    };
  }

  upload() {
    this.uploader.onCompleteAll = () => {
      this.loading = false;
      this.cancel();
    };
    this.bot.prepareStoryDumpUploader(this.uploader, this.state.currentApplication.name, this.state.currentLocale);
    this.uploader.uploadAll();

    this.loading = true;
  }
  cancel() {
    this.dialogRef.close();
  }
}
