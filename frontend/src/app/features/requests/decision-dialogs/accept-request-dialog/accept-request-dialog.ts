import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-accept-request-dialog',
  imports: [MatButtonModule],
  templateUrl: './accept-request-dialog.html',
  styleUrl: './accept-request-dialog.scss',
})
export class AcceptRequestDialog {
  private readonly dialogRef = inject(MatDialogRef<AcceptRequestDialog, boolean>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
