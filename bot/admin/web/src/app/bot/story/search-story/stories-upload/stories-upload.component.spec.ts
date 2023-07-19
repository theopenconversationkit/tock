import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { StateService } from '../../../../core-nlp/state.service';
import { TestSharedModule } from '../../../../shared/test-shared.module';
import { BotService } from '../../../bot-service';

import { StoriesUploadComponent } from './stories-upload.component';

describe('StoriesUploadComponent', () => {
  let component: StoriesUploadComponent;
  let fixture: ComponentFixture<StoriesUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      providers: [
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApp', namespace: 'TestNamespace' }, currentLocale: 'fr' }
        },
        {
          provide: BotService,
          useValue: {}
        },
        {
          provide: NbToastrService,
          useValue: { show: () => {} }
        },
        {
          provide: NbDialogRef,
          useValue: {}
        }
      ],
      declarations: [StoriesUploadComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(StoriesUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
