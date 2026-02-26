import { Injectable, signal } from '@angular/core';

export interface Toast {
  message: string;
  type: 'error' | 'success' | 'warning';
}

@Injectable({ providedIn: 'root' })
export class ToastService {

  toast = signal<Toast | null>(null);

  show(message: string, type: Toast['type'] = 'error') {
    this.toast.set({ message, type });

    setTimeout(() => {
      this.toast.set(null);
    }, 4000);
  }
}