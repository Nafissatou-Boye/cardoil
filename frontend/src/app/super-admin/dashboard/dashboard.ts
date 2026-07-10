import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit {

  stats: any = null;
  chargement = true;

  constructor(
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.http.get(`${environment.apiUrl}/super-admin/dashboard`).subscribe({
      next: (data) => {
        this.stats = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur dashboard:', err);
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