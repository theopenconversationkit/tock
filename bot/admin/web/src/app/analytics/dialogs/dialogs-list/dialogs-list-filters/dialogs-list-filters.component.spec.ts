import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogsListFiltersComponent } from './dialogs-list-filters.component';

describe('DialogsListFiltersComponent', () => {
  let component: DialogsListFiltersComponent;
  let fixture: ComponentFixture<DialogsListFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogsListFiltersComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DialogsListFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
