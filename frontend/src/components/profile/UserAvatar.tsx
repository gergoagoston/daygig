import { useState } from 'react';

type UserAvatarSize = 'small' | 'medium' | 'large';

type UserAvatarProps = {
  avatarUrl: string | null;
  firstName: string;
  lastName: string;
  size?: UserAvatarSize;
};

const sizeClasses: Record<UserAvatarSize, string> = {
  small: 'h-10 w-10 text-sm',
  medium: 'h-16 w-16 text-xl',
  large: 'h-28 w-28 text-3xl',
};

function getInitials(firstName: string, lastName: string) {
  const initials = [firstName, lastName]
    .map((name) => name.trim().charAt(0))
    .filter(Boolean)
    .join('')
    .toUpperCase();

  return initials || '?';
}

type AvatarImageProps = {
  alt: string;
  avatarUrl: string;
  className: string;
  initials: string;
};

function AvatarImage({ alt, avatarUrl, className, initials }: AvatarImageProps) {
  const [hasError, setHasError] = useState(false);

  if (hasError) {
    return (
      <span className={className} aria-label={alt} role="img">
        {initials}
      </span>
    );
  }

  return (
    <img
      className={`${className} object-cover`}
      src={avatarUrl}
      alt={alt}
      onError={() => setHasError(true)}
    />
  );
}

function UserAvatar({ avatarUrl, firstName, lastName, size = 'medium' }: UserAvatarProps) {
  const initials = getInitials(firstName, lastName);
  const fullName = `${firstName} ${lastName}`.trim();
  const commonClasses = `${sizeClasses[size]} inline-flex shrink-0 items-center justify-center overflow-hidden rounded-full border border-border bg-primary-soft font-extrabold text-primary`;
  const alt = fullName || initials;

  if (avatarUrl) {
    return (
      <AvatarImage
        key={avatarUrl}
        alt={alt}
        avatarUrl={avatarUrl}
        className={commonClasses}
        initials={initials}
      />
    );
  }

  return (
    <span className={commonClasses} aria-label={alt} role="img">
      {initials}
    </span>
  );
}

export default UserAvatar;
