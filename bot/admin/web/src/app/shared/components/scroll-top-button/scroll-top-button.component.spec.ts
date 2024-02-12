import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScrollTopButtonComponent } from './scroll-top-button.component';

describe('ScrollTopButtonComponent', () => {
  let component: ScrollTopButtonComponent;
  let fixture: ComponentFixture<ScrollTopButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScrollTopButtonComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ScrollTopButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
