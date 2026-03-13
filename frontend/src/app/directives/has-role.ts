import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { Auth } from '../services/auth';

@Directive({
  selector: '[appHasRole]',
  standalone: true
})
export class HasRoleDirective {

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private auth: Auth
  ) {}

  @Input() set appHasRole(roles: string | string[]) {

    const token = this.auth.getToken();

    if (!token) {
      this.viewContainer.clear();
      return;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const userRole: string = payload.role;

      // ✅ Handle both single string and array
      const allowedRoles = Array.isArray(roles) ? roles : [roles];

      if (allowedRoles.includes(userRole)) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      } else {
        this.viewContainer.clear();
      }

    } catch {
      this.viewContainer.clear();
    }
  }
}