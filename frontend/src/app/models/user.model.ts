export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  email: string;
  role: string;
}

export interface User {
  userId: string;
  username: string;
  email: string;
  role: string;
}
