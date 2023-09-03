import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StoriesFilterComponent } from './stories-filter.component';

describe('StoriesFilterComponent', () => {
  let component: StoriesFilterComponent;
  let fixture: ComponentFixture<StoriesFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StoriesFilterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StoriesFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
