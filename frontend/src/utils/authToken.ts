import type { UserRole } from '../dtos/user';

export type AuthUser = {
  id: string;
  email: string;
  role: UserRole;
};

type JwtPayload = {
  sub?: string;
  uuid?: string;
  email?: string;
  role?: string;
  exp?: number;
};

function decodeBase64Url(value: string) {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');

  const decoded = atob(padded);

  return decodeURIComponent(
    Array.from(decoded, (char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`).join('')
  );
}

function isUserRole(value: string | undefined): value is UserRole {
  return value === 'ROLE_USER' || value === 'ROLE_ADMIN';
}

function isExpired(exp?: number): boolean {
  if (!exp) {
    return true;
  }

  return Date.now() >= exp * 1000;
}

export function decodeAuthToken(token: string): AuthUser | null {
  const parts = token.split('.');

  if (parts.length !== 3) {
    return null;
  }

  const payload = parts[1];

  try {
    const decodedPayload = JSON.parse(decodeBase64Url(payload)) as JwtPayload;

    if (
      !decodedPayload.email ||
      !isUserRole(decodedPayload.role) ||
      isExpired(decodedPayload.exp)
    ) {
      return null;
    }

    const id = decodedPayload.uuid ?? decodedPayload.sub;

    if (!id) {
      return null;
    }

    return {
      id,
      email: decodedPayload.email,
      role: decodedPayload.role,
    };
  } catch {
    return null;
  }
}
