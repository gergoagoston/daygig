import { useState } from 'react';
import { useFormContext, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import type { SelectedLocation } from '../../map/LocationPickerMap';
import SearchLocationModal from './SearchLocationModal';
import type { JobSearchFormValues } from './jobSearchFormModel';

type JobLocationFilterProps = {
  isSearching: boolean;
};

type LocationSource = 'browser' | 'map';

const DEFAULT_DISTANCE_KM = '25';

function geolocationErrorTranslationKey(error: GeolocationPositionError) {
  switch (error.code) {
    case error.PERMISSION_DENIED:
      return 'jobs.search.location.errors.permission_denied';
    case error.POSITION_UNAVAILABLE:
      return 'jobs.search.location.errors.position_unavailable';
    case error.TIMEOUT:
      return 'jobs.search.location.errors.timeout';
    default:
      return 'jobs.search.location.errors.generic';
  }
}

function JobLocationFilter({ isSearching }: JobLocationFilterProps) {
  const { t } = useTranslation();
  const [isLocating, setIsLocating] = useState(false);
  const [isMapOpen, setIsMapOpen] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [locationSource, setLocationSource] = useState<LocationSource | null>(null);
  const {
    clearErrors,
    control,
    formState: { errors },
    getValues,
    register,
    setValue,
  } = useFormContext<JobSearchFormValues>();
  const [latitude, longitude, maxDistanceKm] = useWatch({
    control,
    name: ['latitude', 'longitude', 'maxDistanceKm'],
  });
  const selectedLocation =
    latitude !== '' &&
    longitude !== '' &&
    Number.isFinite(Number(latitude)) &&
    Number.isFinite(Number(longitude))
      ? {
          latitude: Number(latitude),
          longitude: Number(longitude),
        }
      : null;

  function applyLocation(location: SelectedLocation, source: LocationSource) {
    setValue('latitude', String(location.latitude), { shouldValidate: true });
    setValue('longitude', String(location.longitude), { shouldValidate: true });
    setValue('maxDistanceKm', getValues('maxDistanceKm') || DEFAULT_DISTANCE_KM, {
      shouldValidate: true,
    });
    clearErrors(['latitude', 'longitude', 'maxDistanceKm']);
    setLocationError(null);
    setLocationSource(source);
  }

  function handleUseCurrentLocation() {
    if (!navigator.geolocation) {
      setLocationError(t('jobs.search.location.errors.unavailable'));
      return;
    }

    setIsLocating(true);
    setLocationError(null);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        applyLocation(
          {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          },
          'browser'
        );
        setIsLocating(false);
      },
      (error) => {
        setLocationError(t(geolocationErrorTranslationKey(error)));
        setIsLocating(false);
      },
      {
        enableHighAccuracy: false,
        maximumAge: 300_000,
        timeout: 10_000,
      }
    );
  }

  function handleClearLocation() {
    setValue('latitude', '');
    setValue('longitude', '');
    setValue('maxDistanceKm', '');
    clearErrors(['latitude', 'longitude', 'maxDistanceKm']);
    setLocationError(null);
    setLocationSource(null);
  }

  return (
    <div className="grid min-w-0 flex-1 gap-3">
      <div className="flex min-w-0 flex-wrap items-end gap-3 xl:flex-nowrap">
        <div className="min-w-40" aria-live="polite">
          <p className="font-semibold text-heading">{t('jobs.search.location.label')}</p>
          <p className="text-sm text-body">
            {locationSource
              ? t(`jobs.search.location.status.${locationSource}`)
              : t('jobs.search.location.status.none')}
          </p>
        </div>

        {selectedLocation ? (
          <button
            className="whitespace-nowrap rounded-lg border border-border bg-surface px-3 py-2.5 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft"
            disabled={isLocating || isSearching}
            type="button"
            onClick={handleClearLocation}
          >
            {t('jobs.search.location.clear')}
          </button>
        ) : null}
        <button
          className="whitespace-nowrap rounded-lg border border-border bg-surface px-3 py-2.5 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft disabled:opacity-65"
          disabled={isLocating || isSearching}
          type="button"
          onClick={handleUseCurrentLocation}
        >
          {isLocating ? t('jobs.search.location.locating') : t('jobs.search.location.use_current')}
        </button>
        <button
          className="whitespace-nowrap rounded-lg border border-border bg-surface px-3 py-2.5 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft disabled:opacity-65"
          disabled={isLocating || isSearching}
          type="button"
          onClick={() => setIsMapOpen(true)}
        >
          {t('jobs.search.location.select_map')}
        </button>

        {selectedLocation ? (
          <div className="grid w-52 shrink-0 gap-2">
            <div className="flex items-center justify-between gap-3">
              <label className="font-semibold text-heading" htmlFor="job-search-distance">
                {t('jobs.search.location.distance')}
              </label>
              <output className="font-bold text-heading" htmlFor="job-search-distance">
                {t('jobs.search.location.distance_value', {
                  distance: maxDistanceKm,
                })}
              </output>
            </div>
            <input
              id="job-search-distance"
              className="w-full cursor-grab accent-primary active:cursor-grabbing"
              max="200"
              min="5"
              step="5"
              type="range"
              {...register('maxDistanceKm')}
            />
            {errors.maxDistanceKm ? (
              <p className="text-sm text-danger">{errors.maxDistanceKm.message}</p>
            ) : null}
          </div>
        ) : null}
      </div>

      {locationError ? <p className="text-sm text-danger">{locationError}</p> : null}

      {isMapOpen ? (
        <SearchLocationModal
          initialLocation={selectedLocation}
          isOpen
          onClose={() => setIsMapOpen(false)}
          onConfirm={(location) => {
            applyLocation(location, 'map');
            setIsMapOpen(false);
          }}
        />
      ) : null}

      <input type="hidden" {...register('latitude')} />
      <input type="hidden" {...register('longitude')} />
    </div>
  );
}

export default JobLocationFilter;
