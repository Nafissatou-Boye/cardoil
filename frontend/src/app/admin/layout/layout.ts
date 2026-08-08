import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './layout.html',
  styleUrl: './layout.css'
})
export class LayoutComponent implements OnInit {

  nom = localStorage.getItem('nom');
  prenom = localStorage.getItem('prenom');
  pageTitle = 'Dashboard';

  // ✅ Nouveau — état du menu mobile (tiroir hors-écran par défaut,
  // affiché en overlay quand ouvert). Voir layout.css / layout.html.
  menuMobileOuvert = false;

  constructor(
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.updateTitle();
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateTitle();
 
      this.menuMobileOuvert = false;
      this.cdr.detectChanges();
    });
  }

  ouvrirMenuMobile(): void {
    this.menuMobileOuvert = true;
  }

  fermerMenuMobile(): void {
    this.menuMobileOuvert = false;
  }

  private updateTitle(): void {
    let route = this.router.routerState.root;
    while (route.firstChild) {
      route = route.firstChild;
    }
    this.pageTitle = route.snapshot.data['title'] || 'Dashboard';
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}