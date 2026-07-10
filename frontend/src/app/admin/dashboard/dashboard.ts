import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AdminDashboardService, DashboardAdminCompagnie } from '../../core/services/admin-dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {

  stats: DashboardAdminCompagnie | null = null;
  chargement = true;

  constructor(
    private adminDashboardService: AdminDashboardService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.adminDashboardService.getDashboard().subscribe({
      next: (data) => {
        this.stats = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur dashboard admin:', err);
        this.chargement = false;
        this.cdr.detectChanges();
        if (err.status === 401 || err.status === 403) {
          localStorage.clear();
          this.router.navigate(['/login']);
        }
      }
    });
  }
}