import { TestBed } from '@angular/core/testing';
import { Register } from './register';
import { provideRouter } from '@angular/router';

describe('Register', () => {

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideRouter([])
      ]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(Register);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

});