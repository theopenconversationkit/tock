import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbButtonModule, NbIconModule, NbTooltipModule } from '@nebular/theme';
import { TestSharedModule } from '../test-shared.module';

import { FileUploadComponent } from './file-upload.component';

describe('FileUploadComponent', () => {
  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FileUploadComponent],
      imports: [TestSharedModule, NbButtonModule, NbIconModule, NbTooltipModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FileUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  fit('should create', () => {
    expect(component).toBeTruthy();
  });
});
