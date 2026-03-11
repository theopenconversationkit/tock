import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SamplesBoardComponent } from './samples-board.component';

describe('SamplesBoardComponent', () => {
  let component: SamplesBoardComponent;
  let fixture: ComponentFixture<SamplesBoardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SamplesBoardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SamplesBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
