export type Role = 'USER' | 'LIBRARIAN' | 'ADMIN';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface Profile extends User {
  phone: string | null;
  pseudonym: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  consent: boolean;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone: string;
  pseudonym: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
