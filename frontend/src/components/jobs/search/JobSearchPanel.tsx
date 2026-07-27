import { useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import type { SelectedLocation } from '../../map/LocationPickerMap';
import { jobCategories } from '../../../types/job';
import Button from '../../ui/Button';
import Input from '../../ui/Input';
import Select from '../../ui/Select';
import SearchLocationModal from './SearchLocationModal';
import {
  createJobSearchSchema,
  defaultJobSearchValues,
  jobSortOptions,
  type JobSearchFormValues,
} from './jobSearchFormModel';

type JobSearchPanelProps = {
  isSearching: boolean;
  onSearch: (values: JobSearchFormValues) => void;
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

function JobSearchPanel({ isSearching, onSearch }: JobSearchPanelProps) {
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
    handleSubmit,
    register,
    setValue,
  } = useForm<JobSearchFormValues>({
    defaultValues: defaultJobSearchValues,
    resolver: zodResolver(createJobSearchSchema(t)),
  });
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

  function handleReset() {
    setValue('q', defaultJobSearchValues.q);
    setValue('category', defaultJobSearchValues.category);
    setValue('minHourlyRate', defaultJobSearchValues.minHourlyRate);
    setValue('maxHourlyRate', defaultJobSearchValues.maxHourlyRate);
    setValue('sort', defaultJobSearchValues.sort);
    setValue('latitude', defaultJobSearchValues.latitude);
    setValue('longitude', defaultJobSearchValues.longitude);
    setValue('maxDistanceKm', defaultJobSearchValues.maxDistanceKm);
    clearErrors();
    setLocationError(null);
    setLocationSource(null);
    onSearch(defaultJobSearchValues);
  }

  return (
    <section className="border-b border-border pb-6" aria-label={t('jobs.search.title')}>
      <form className="grid gap-4" noValidate onSubmit={handleSubmit(onSearch)}>
        <div className="grid items-end gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-[minmax(240px,2fr)_minmax(180px,1fr)_minmax(170px,1fr)_minmax(170px,1fr)_minmax(190px,1.15fr)]">
          <Input
            id="job-search-query"
            label={t('jobs.search.fields.query')}
            maxLength={100}
            placeholder={t('jobs.search.fields.query_placeholder')}
            type="search"
            error={errors.q?.message}
            {...register('q')}
          />

          <Select
            id="job-search-category"
            label={t('jobs.search.fields.category')}
            error={errors.category?.message}
            {...register('category')}
          >
            <option value="">{t('jobs.search.fields.all_categories')}</option>
            {jobCategories.map((category) => (
              <option key={category} value={category}>
                {t(`jobs.categories.${category}`)}
              </option>
            ))}
          </Select>

          <Input
            id="job-search-min-hourly-rate"
            label={t('jobs.search.fields.min_hourly_rate')}
            inputMode="decimal"
            min="0"
            step="1"
            type="number"
            error={errors.minHourlyRate?.message}
            {...register('minHourlyRate')}
          />

          <Input
            id="job-search-max-hourly-rate"
            label={t('jobs.search.fields.max_hourly_rate')}
            inputMode="decimal"
            min="0"
            step="1"
            type="number"
            error={errors.maxHourlyRate?.message}
            {...register('maxHourlyRate')}
          />

          <Select
            id="job-search-sort"
            label={t('jobs.search.fields.sort')}
            error={errors.sort?.message}
            {...register('sort')}
          >
            {jobSortOptions.map((sort) => (
              <option key={sort} value={sort}>
                {t(`jobs.search.sort.${sort.replace(',', '_')}`)}
              </option>
            ))}
          </Select>
        </div>

        <div className="border-t border-border pt-4">
          <div className="flex flex-col gap-4 xl:flex-row xl:items-end">
            <div className="flex min-w-0 flex-1 flex-wrap items-end gap-3 xl:flex-nowrap">
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
                {isLocating
                  ? t('jobs.search.location.locating')
                  : t('jobs.search.location.use_current')}
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

            <div className="flex flex-wrap justify-end gap-3 xl:shrink-0">
              <button
                className="rounded-lg border border-border bg-surface px-4.5 py-3 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft"
                disabled={isSearching}
                type="button"
                onClick={handleReset}
              >
                {t('jobs.search.actions.reset')}
              </button>
              <Button className="!w-auto" isLoading={isSearching} type="submit">
                {isSearching ? t('jobs.search.actions.searching') : t('jobs.search.actions.search')}
              </Button>
            </div>
          </div>

          {locationError ? <p className="mt-3 text-sm text-danger">{locationError}</p> : null}

          <input type="hidden" {...register('latitude')} />
          <input type="hidden" {...register('longitude')} />
        </div>
      </form>

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
    </section>
  );
}

export default JobSearchPanel;
