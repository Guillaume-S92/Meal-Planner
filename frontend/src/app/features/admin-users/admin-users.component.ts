import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AdminUserService } from '../../core/admin-user.service';
import { AdminUserResponse } from '../../core/models';

@Component({
  selector: 'mp-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSlideToggleModule
  ],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['USER' as 'USER' | 'ADMIN', Validators.required],
    enabled: [true]
  });
  protected users: AdminUserResponse[] = [];
  protected loading = true;
  protected saving = false;
  protected error = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly usersApi: AdminUserService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  protected create(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving) {
      return;
    }
    this.saving = true;
    this.error = '';
    this.usersApi.create(this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset({ username: '', password: '', role: 'USER', enabled: true });
        this.saving = false;
        this.load();
      },
      error: () => {
        this.error = 'Creation impossible. Verifie le nom et le mot de passe.';
        this.saving = false;
      }
    });
  }

  protected toggleEnabled(user: AdminUserResponse, enabled: boolean): void {
    this.usersApi.update(user.id, { enabled }).subscribe({
      next: (updated) => {
        this.users = this.users.map((item) => item.id === updated.id ? updated : item);
      },
      error: () => {
        this.error = 'Modification impossible pour cet utilisateur.';
      }
    });
  }

  private load(): void {
    this.loading = true;
    this.usersApi.list().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: () => {
        this.error = 'Acces administrateur requis.';
        this.loading = false;
      }
    });
  }
}
