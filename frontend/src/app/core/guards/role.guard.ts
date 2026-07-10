import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const roleGuard = (rolesAutorises: string[]): CanActivateFn => {
  return (route, state) => {
    const router = inject(Router);
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (!token) {
      router.navigate(['/login']);
      return false;
    }

    if (!role || !rolesAutorises.includes(role)) {
      router.navigate(['/non-autorise']);
      return false;
    }

    return true;
  };
};