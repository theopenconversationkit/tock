import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { TestSharedModule } from '../../../shared/test-shared.module';
import { BotService } from '../../bot-service';

import { EditStoryComponent } from './edit-story.component';

describe('EditStoryComponent', () => {
  let component: EditStoryComponent;
  let fixture: ComponentFixture<EditStoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestSharedModule],
      declarations: [EditStoryComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: of([{}]) }
        },
        {
          provide: Router,
          useValue: {}
        },
        {
          provide: BotService,
          useValue: {}
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(EditStoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
