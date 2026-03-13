import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { TeamService } from '../../services/team';
import { AdminService, User } from '../../services/admin';
import { HasRoleDirective } from '../../directives/has-role';
import { Navbar } from '../navbar/navbar';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-teams',
  standalone: true,
  imports: [CommonModule, FormsModule, HasRoleDirective, Navbar, RouterModule],
  templateUrl: './teams.html'
})
export class Teams implements OnInit {

  private teamService = inject(TeamService);
  private adminService = inject(AdminService);
  private cdr = inject(ChangeDetectorRef);

  teams: any[] = [];
  loading = false;

  // ================= CREATE TEAM =================
  showCreateModal = false;
  creating = false;
  newTeam = { name: '', description: '' };
  createError = '';

  // ================= DELETE TEAM =================
  deleting = false;

  // ================= VIEW MEMBERS =================
  viewingTeamId: number | null = null;
  selectedTeamMembers: any[] = [];
  loadingMembers = false;

  // ================= ADD MEMBER =================
  showAddMemberModal = false;
  selectedTeam: any = null;
  allUsers: User[] = [];
  selectedUserId: number | null = null;
  addingMember = false;
  addMemberError = '';

  ngOnInit(): void {
    this.loadTeams();
  }

  // ================= LOAD TEAMS =================
  loadTeams() {
    this.loading = true;
    this.teamService.getTeams()
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (res) => {
          this.teams = res;
        },
        error: (err) => {
          console.error('Failed to load teams', err);
        }
      });
  }

  // ================= VIEW MEMBERS TOGGLE =================
  viewMembers(team: any) {

    // Toggle off if same team clicked
    if (this.viewingTeamId === team.id) {
      this.viewingTeamId = null;
      this.selectedTeamMembers = [];
      return;
    }

    this.viewingTeamId = team.id;
    this.loadingMembers = true;

    this.teamService.getTeamMembers(team.id)
      .pipe(finalize(() => {
        this.loadingMembers = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (members) => {
          this.selectedTeamMembers = members;
        },
        error: (err) => {
          console.error('Failed to load members', err);
        }
      });
  }

  // ================= OPEN/CLOSE CREATE MODAL =================
  openCreateModal() {
    this.newTeam = { name: '', description: '' };
    this.createError = '';
    this.showCreateModal = true;
  }

  closeCreateModal() {
    this.showCreateModal = false;
    this.createError = '';
  }

  // ================= CREATE TEAM =================
  createTeam() {
    if (!this.newTeam.name.trim()) {
      this.createError = 'Team name is required.';
      return;
    }

    this.creating = true;
    this.teamService.createTeam(this.newTeam)
      .pipe(finalize(() => {
        this.creating = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: (team) => {
          this.teams = [...this.teams, team];
          this.closeCreateModal();
        },
        error: (err) => {
          this.createError = 'Failed to create team. Try again.';
          console.error(err);
        }
      });
  }

  // ================= DELETE TEAM =================
  deleteTeam(team: any) {
    if (!confirm(`Are you sure you want to delete "${team.name}"?`)) return;

    this.deleting = true;
    this.teamService.deleteTeam(team.id)
      .pipe(finalize(() => {
        this.deleting = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.teams = this.teams.filter(t => t.id !== team.id);
          // Close members panel if deleted team was open
          if (this.viewingTeamId === team.id) {
            this.viewingTeamId = null;
            this.selectedTeamMembers = [];
          }
        },
        error: (err) => {
          alert('Failed to delete team.');
          console.error(err);
        }
      });
  }

  // ================= OPEN ADD MEMBER MODAL =================
  openAddMemberModal(team: any) {
    this.selectedTeam = team;
    this.selectedUserId = null;
    this.addMemberError = '';
    this.showAddMemberModal = true;

    this.teamService.getUsers().subscribe({
      next: (users) => {
        // ✅ Filter out ADMIN from dropdown
        this.allUsers = users.filter(u => u.role !== 'ADMIN');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load users', err);
      }
    });
  }

  // ================= CLOSE ADD MEMBER MODAL =================
  closeAddMemberModal() {
    this.showAddMemberModal = false;
    this.selectedTeam = null;
    this.selectedUserId = null;
    this.addMemberError = '';
  }

  // ================= ADD MEMBER =================
  addMember() {
    if (!this.selectedUserId) {
      this.addMemberError = 'Please select a user.';
      return;
    }

    this.addingMember = true;
    this.teamService.addMember(this.selectedTeam.id, this.selectedUserId)
      .pipe(finalize(() => {
        this.addingMember = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: () => {
          this.closeAddMemberModal();
          // ✅ Refresh members if panel is open
          if (this.viewingTeamId === this.selectedTeam?.id) {
            this.viewMembers(this.selectedTeam);
          }
          alert('Member added successfully!');
        },
        error: (err) => {
          // ✅ Handle specific errors
          if (err.status === 409) {
            this.addMemberError = 'User is already a member of this team.';
          } else if (err.status === 403) {
            this.addMemberError = 'You are not allowed to add members.';
          } else if (err.status === 500) {
            this.addMemberError = 'User is already a member of this team.';
          } else {
            this.addMemberError = 'Failed to add member. Try again.';
          }
          console.error(err);
        }
      });
  }

  // ================= REMOVE MEMBER =================
  removeMember(team: any, userId: number) {
    if (!confirm('Remove this member from the team?')) return;

    this.teamService.removeMember(team.id, userId)
      .subscribe({
        next: () => {
          this.selectedTeamMembers = this.selectedTeamMembers
            .filter(m => m.id !== userId);
          this.cdr.detectChanges();
        },
        error: (err) => {
          alert('Failed to remove member.');
          console.error(err);
        }
      });
  }
}