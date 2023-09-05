import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbAutocompleteModule, NbInputModule } from '@nebular/theme';
import { TestingModule } from '../../../../testing';

import { AutocompleteInputComponent } from './autocomplete-input.component';

describe('AutocompleteInputComponent', () => {
  let component: AutocompleteInputComponent;
  let fixture: ComponentFixture<AutocompleteInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AutocompleteInputComponent],
      imports: [TestingModule, NbAutocompleteModule, NbInputModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AutocompleteInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
