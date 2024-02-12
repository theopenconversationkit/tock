import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentenceTrainingCreateEntityComponent } from './sentence-training-create-entity.component';

describe('SentenceTrainingCreateEntityComponent', () => {
  let component: SentenceTrainingCreateEntityComponent;
  let fixture: ComponentFixture<SentenceTrainingCreateEntityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SentenceTrainingCreateEntityComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SentenceTrainingCreateEntityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
