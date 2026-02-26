import { TestBed } from '@angular/core/testing';
import { Dashboard } from './dashboard';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { TaskService } from '../../services/task';
import { Auth } from '../../services/auth';

describe('Dashboard', () => {

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideRouter([
          { path: 'login', component: Dashboard } // dummy route
        ]),
        {
          provide: TaskService,
          useValue: {
            getTasks: () => of([]),
            createTask: () => of({}),
            deleteTask: () => of({}),
            updateTask: () => of({})
          }
        },
        {
          provide: Auth,
          useValue: {
            isLoggedIn: () => true,
            getUserName: () => 'test@test.com',
            logout: () => {}
          }
        }
      ]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(Dashboard);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

});