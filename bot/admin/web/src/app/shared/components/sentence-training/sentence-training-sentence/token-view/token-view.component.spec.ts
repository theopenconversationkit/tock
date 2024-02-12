import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenViewComponent } from './token-view.component';

describe('TokenViewComponent', () => {
  let component: TokenViewComponent;
  let fixture: ComponentFixture<TokenViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TokenViewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TokenViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
