import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import LocationPickerMap, { type SelectedLocation } from '../../map/LocationPickerMap';
import Button from '../../ui/Button';
import Modal from '../../ui/Modal';

type SearchLocationModalProps = {
  initialLocation: SelectedLocation | null;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (location: SelectedLocation) => void;
};

function SearchLocationModal({
  initialLocation,
  isOpen,
  onClose,
  onConfirm,
}: SearchLocationModalProps) {
  const { t } = useTranslation();
  const [draftLocation, setDraftLocation] = useState<SelectedLocation | null>(initialLocation);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={t('jobs.search.location.modal_title')}
      titleId="search-location-modal-title"
    >
      <div className="grid gap-5">
        <LocationPickerMap
          label={t('jobs.search.location.map_label')}
          labelVisuallyHidden
          selectedLocation={draftLocation}
          onSelectLocation={setDraftLocation}
        />

        <div className="flex flex-wrap justify-end gap-3">
          <button
            className="rounded-lg border border-border bg-surface px-4.5 py-3 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft"
            type="button"
            onClick={onClose}
          >
            {t('jobs.search.location.cancel')}
          </button>
          <Button
            className="!w-auto"
            disabled={!draftLocation}
            type="button"
            onClick={() => {
              if (draftLocation) {
                onConfirm(draftLocation);
              }
            }}
          >
            {t('jobs.search.location.confirm')}
          </Button>
        </div>
      </div>
    </Modal>
  );
}

export default SearchLocationModal;
