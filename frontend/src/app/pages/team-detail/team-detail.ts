import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { TeamService } from '../../services/team';
import { Navbar } from '../navbar/navbar';

@Component({
  selector: 'app-team-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, Navbar],
  templateUrl: './team-detail.html'
})
export class TeamDetail implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private teamService = inject(TeamService);
  private cdr = inject(ChangeDetectorRef);

  teamId!: number;
  team: any = null;
  members: any[] = [];
  tasks: any[] = [];

  loadingTeam = false;
  loadingMembers = false;
  loadingTasks = false;

  ngOnInit() {
    this.teamId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadTeam();
    this.loadMembers();
    this.loadTasks();
  }

  loadTeam() {
    this.loadingTeam = true;
    this.teamService.getTeam(this.teamId)
      .pipe(finalize(() => {
        this.loadingTeam = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => this.team = data,
        error: () => this.router.navigate(['/teams'])
      });
  }

  loadMembers() {
    this.loadingMembers = true;
    this.teamService.getTeamMembers(this.teamId)
      .pipe(finalize(() => {
        this.loadingMembers = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => this.members = data,
        error: (err) => console.error(err)
      });
  }

  loadTasks() {
    this.loadingTasks = true;
    this.teamService.getTeamTasks(this.teamId)
      .pipe(finalize(() => {
        this.loadingTasks = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (data) => this.tasks = data,
        error: (err) => console.error(err)
      });
  }

  getStatusClass(status: string) {
    switch (status) {
      case 'TODO':        return 'bg-secondary';
      case 'IN_PROGRESS': return 'bg-warning text-dark';
      case 'DONE':        return 'bg-success';
      default:            return 'bg-light';
    }
  }

  getPriorityClass(priority: string) {
    switch (priority) {
      case 'HIGH':   return 'bg-danger';
      case 'MEDIUM': return 'bg-warning text-dark';
      case 'LOW':    return 'bg-success';
      default:       return 'bg-secondary';
    }
  }
}