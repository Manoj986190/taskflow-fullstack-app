import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task';

describe('TaskService', () => {

  let service: TaskService;
  let httpMock: HttpTestingController;

  const mockTasks = [
    {
      id: 1,
      title: 'Test Task',
      description: 'Test Description',
      dueDate: '2026-03-01',
      status: 'TODO'
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskService]
    });

    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch tasks', () => {

    service.getTasks().subscribe(tasks => {
      expect(tasks.length).toBe(1);
      expect(tasks[0].title).toBe('Test Task');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/tasks');
    expect(req.request.method).toBe('GET');

    req.flush(mockTasks);
  });

});