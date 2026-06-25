import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { addWeeks, startOfWeek, toIsoDate } from '../../core/date-utils';
import { ShoppingListItemResponse } from '../../core/models';
import { ShoppingListService } from '../../core/shopping-list.service';

interface ShoppingCategory {
  name: string;
  items: ShoppingListItemResponse[];
}

@Component({
  selector: 'mp-shopping-list',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCheckboxModule, MatIconModule, MatProgressBarModule, MatSlideToggleModule],
  templateUrl: './shopping-list.component.html',
  styleUrl: './shopping-list.component.scss'
})
export class ShoppingListComponent implements OnInit {
  protected weekStart = startOfWeek();
  protected categories: ShoppingCategory[] = [];
  protected loading = true;
  protected error = '';

  constructor(private readonly shoppingLists: ShoppingListService) {}

  protected get weekIso(): string {
    return toIsoDate(this.weekStart);
  }

  ngOnInit(): void {
    this.load();
  }

  protected previousWeek(): void {
    this.weekStart = addWeeks(this.weekStart, -1);
    this.load();
  }

  protected nextWeek(): void {
    this.weekStart = addWeeks(this.weekStart, 1);
    this.load();
  }

  protected toggleChecked(item: ShoppingListItemResponse, checked: boolean): void {
    this.update(item, { checked, excluded: item.excluded });
  }

  protected toggleExcluded(item: ShoppingListItemResponse, excluded: boolean): void {
    this.update(item, { checked: item.checked, excluded });
  }

  protected itemCount(): number {
    return this.categories.reduce((total, category) => total + category.items.length, 0);
  }

  protected remainingCount(): number {
    return this.categories.reduce(
      (total, category) => total + category.items.filter((item) => !item.checked && !item.excluded).length,
      0
    );
  }

  private load(): void {
    this.loading = true;
    this.error = '';
    this.shoppingLists.getWeek(this.weekIso).subscribe({
      next: (list) => {
        this.categories = Object.entries(list.categories)
          .map(([name, items]) => ({ name, items }))
          .sort((a, b) => a.name.localeCompare(b.name));
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger la liste de courses.';
        this.loading = false;
      }
    });
  }

  private update(item: ShoppingListItemResponse, state: { checked: boolean; excluded: boolean }): void {
    this.shoppingLists.updateItem(this.weekIso, item.id, state).subscribe({
      next: (list) => {
        this.categories = Object.entries(list.categories).map(([name, items]) => ({ name, items }));
      },
      error: () => {
        this.error = 'Impossible de mettre a jour cet article.';
      }
    });
  }
}
