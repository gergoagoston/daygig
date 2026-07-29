import { useMutation } from '@tanstack/react-query';
import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { deleteCurrentUserAvatar, uploadCurrentUserAvatar } from '../../api/usersApi';
import type { CurrentUserUpdate } from '../../contexts/AuthContext';
import type { UserOutDto } from '../../dtos/user';
import UserAvatar from './UserAvatar';

const MAX_AVATAR_SIZE_BYTES = 2 * 1024 * 1024;

type AvatarEditorProps = {
  onUserUpdated: (update: CurrentUserUpdate) => void;
  user: UserOutDto;
};

function addAvatarCacheKey(avatarUrl: string | null): string | null {
  if (!avatarUrl) {
    return null;
  }

  const separator = avatarUrl.includes('?') ? '&' : '?';
  return `${avatarUrl}${separator}v=${Date.now()}`;
}

function AvatarEditor({ onUserUpdated, user }: AvatarEditorProps) {
  const { t } = useTranslation();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [validationError, setValidationError] = useState<string | null>(null);

  const uploadMutation = useMutation({
    mutationFn: uploadCurrentUserAvatar,
    onSuccess: (updatedUser) => {
      setValidationError(null);
      onUserUpdated((currentUser) => ({
        ...currentUser,
        avatarUrl: addAvatarCacheKey(updatedUser.avatarUrl),
      }));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteCurrentUserAvatar,
    onSuccess: () => {
      setValidationError(null);
      onUserUpdated((currentUser) => ({ ...currentUser, avatarUrl: null }));
    },
  });

  const isBusy = uploadMutation.isPending || deleteMutation.isPending;
  const hasRequestError = uploadMutation.isError || deleteMutation.isError;

  const handleFileChange = (file: File | undefined) => {
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }

    uploadMutation.reset();
    deleteMutation.reset();

    if (!file) {
      return;
    }

    if (file.size > MAX_AVATAR_SIZE_BYTES) {
      setValidationError(t('profile.avatar.errors.size'));
      return;
    }

    setValidationError(null);
    uploadMutation.mutate(file);
  };

  const handleDelete = () => {
    if (!window.confirm(t('profile.avatar.delete_confirm'))) {
      return;
    }

    uploadMutation.reset();
    deleteMutation.reset();
    deleteMutation.mutate();
  };

  return (
    <div className="flex flex-col items-center gap-4 sm:items-start">
      <UserAvatar
        avatarUrl={user.avatarUrl}
        firstName={user.firstName}
        lastName={user.lastName}
        size="large"
      />

      <input
        ref={fileInputRef}
        className="sr-only"
        type="file"
        accept=".jpg,.jpeg,.png,image/jpeg,image/png"
        disabled={isBusy}
        onChange={(event) => handleFileChange(event.target.files?.[0])}
      />

      <div className="flex flex-wrap justify-center gap-2 sm:justify-start">
        <button
          className="navbar-gomb"
          type="button"
          disabled={isBusy}
          onClick={() => fileInputRef.current?.click()}
        >
          {uploadMutation.isPending
            ? t('profile.avatar.uploading')
            : user.avatarUrl
              ? t('profile.avatar.replace')
              : t('profile.avatar.upload')}
        </button>

        {user.avatarUrl ? (
          <button
            className="inline-flex items-center justify-center rounded-lg border border-danger bg-danger-soft px-3 py-2 text-sm font-bold text-danger transition-colors hover:bg-danger hover:text-white disabled:opacity-65"
            type="button"
            disabled={isBusy}
            onClick={handleDelete}
          >
            {deleteMutation.isPending ? t('profile.avatar.deleting') : t('profile.avatar.delete')}
          </button>
        ) : null}
      </div>

      <p className="max-w-72 text-center text-sm text-body sm:text-left">
        {t('profile.avatar.help')}
      </p>

      {validationError || hasRequestError ? (
        <p className="text-sm font-semibold text-danger" role="alert">
          {validationError ?? t('profile.avatar.errors.generic')}
        </p>
      ) : null}
    </div>
  );
}

export default AvatarEditor;
