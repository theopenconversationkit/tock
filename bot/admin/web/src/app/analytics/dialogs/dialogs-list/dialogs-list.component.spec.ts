import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogsListComponent } from './dialogs-list.component';

describe('DialogsListComponent', () => {
  let component: DialogsListComponent;
  let fixture: ComponentFixture<DialogsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DialogsListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DialogsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
