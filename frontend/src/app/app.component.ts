import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from './core/auth.service';

interface NavLink {
  label: string;
  icon: string;
  path: string;
  adminOnly?: boolean;
}

@Component({
  selector: 'mp-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatButtonModule, MatIconModule, MatToolbarModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  constructor(
    protected readonly auth: AuthService,
    private readonly router: Router
  ) {}

  protected readonly navLinks: NavLink[] = [
    { label: 'Accueil', icon: 'dashboard', path: '/dashboard' },
    { label: 'Recettes', icon: 'restaurant_menu', path: '/recipes' },
    { label: 'Planning', icon: 'calendar_month', path: '/planner' },
    { label: 'Courses', icon: 'shopping_cart', path: '/shopping' },
    { label: 'Cuisine', icon: 'restaurant', path: '/cook' },
    { label: 'Admin', icon: 'admin_panel_settings', path: '/admin/users', adminOnly: true }
  ];

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.auth.me().subscribe({ error: () => this.auth.clearSession() });
    }
  }

  protected visibleNavLinks(): NavLink[] {
    return this.navLinks.filter((link) => !link.adminOnly || this.auth.currentUser()?.role === 'ADMIN');
  }

  protected logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
