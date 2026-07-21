import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EspaceDepartementService, EspaceDepartementInfo } from '../../core/services/espace-departement.service';

@Component({
  selector: 'app-departement-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  chargement = true;
  info: EspaceDepartementInfo | null = null;

  constructor(
    private espaceDepartementService: EspaceDepartementService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.espaceDepartementService.getInfo().subscribe({
      next: (data) => {
        this.info = data;
        this.chargement = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.chargement = false;
        this.cdr.detectChanges();
      }
    });
  }
}