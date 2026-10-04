import {Component, EventEmitter, Input, Output} from '@angular/core';

/**
 * 簡易分頁列，取代後台用的 mat-paginator。
 *
 * 只負責顯示頁碼與發出換頁事件，資料怎麼切由使用的頁面自己 slice ——
 * 頁面本來就握有完整陣列，不需要像 MatTableDataSource 那樣把資料交出去。
 */
@Component({
  selector: 'app-pager',
  template: `
    @if (total > pageSize) {
      <nav class="mt-3 flex items-center justify-between text-sm" aria-label="分頁">
        <button type="button" class="btn-secondary px-3 py-1.5" [disabled]="page === 0" (click)="go(page - 1)">
          上一頁
        </button>
        <span class="text-gray-600">{{ page + 1 }} / {{ pageCount }}</span>
        <button type="button" class="btn-secondary px-3 py-1.5" [disabled]="page >= pageCount - 1" (click)="go(page + 1)">
          下一頁
        </button>
      </nav>
    }
  `,
})
export class PagerComponent {
  @Input() total = 0;
  @Input() pageSize = 5;
  @Input() page = 0;
  @Output() pageChange = new EventEmitter<number>();

  get pageCount(): number {
    return Math.max(1, Math.ceil(this.total / this.pageSize));
  }

  go(page: number) {
    if (page < 0 || page >= this.pageCount) {
      return;
    }
    this.pageChange.emit(page);
  }
}
