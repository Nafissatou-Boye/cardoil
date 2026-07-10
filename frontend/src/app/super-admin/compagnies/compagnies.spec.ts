import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Compagnies } from './compagnies';

describe('Compagnies', () => {
  let component: Compagnies;
  let fixture: ComponentFixture<Compagnies>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Compagnies],
    }).compileComponents();

    fixture = TestBed.createComponent(Compagnies);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
