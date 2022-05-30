import { SelectionModel } from '@angular/cdk/collections';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbSelectModule,
  NbTooltipModule
} from '@nebular/theme';
import { BehaviorSubject, of } from 'rxjs';

import { StateService } from '../../../core-nlp/state.service';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { FaqTrainingListComponent } from './faq-training-list.component';

class StateServiceMock {
  currentIntentsCategories: BehaviorSubject<any[]> = new BehaviorSubject([]);
}

describe('FaqTrainingListComponent', () => {
  let component: FaqTrainingListComponent;
  let fixture: ComponentFixture<FaqTrainingListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqTrainingListComponent, PaginationComponent],
      imports: [
        TestSharedModule,
        NbCheckboxModule,
        NbIconModule,
        NbFormFieldModule,
        NbSelectModule,
        NbCardModule,
        NbButtonModule,
        NbTooltipModule
      ],
      providers: [
        {
          provide: StateService,
          useClass: StateServiceMock
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqTrainingListComponent);
    component = fixture.componentInstance;
    component.selection = new SelectionModel();
    component.pagination = {
      size: 0,
      start: 0,
      end: 0,
      total: 0
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
