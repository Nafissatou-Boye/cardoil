import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NonAutorise } from './non-autorise';

describe('NonAutorise', () => {
  let component: NonAutorise;
  let fixture: ComponentFixture<NonAutorise>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NonAutorise],
    }).compileComponents();

    fixture = TestBed.createComponent(NonAutorise);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
