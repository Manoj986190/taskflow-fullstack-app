import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast';

describe('ToastService', () => {

  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ToastService]
    });

    service = TestBed.inject(ToastService);
  });

  it('should create the service', () => {
    expect(service).toBeTruthy();
  });

  it('should show toast message', () => {

    service.show('Test message', 'success');

    const toast = service.toast();

    expect(toast?.message).toBe('Test message');
    expect(toast?.type).toBe('success');

  });

});