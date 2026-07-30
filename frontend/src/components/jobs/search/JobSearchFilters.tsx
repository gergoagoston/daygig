import { useFormContext } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { jobCategories } from '../../../types/job';
import Input from '../../ui/Input';
import Select from '../../ui/Select';
import JobLocationFilter from './JobLocationFilter';
import { jobSortOptions, type JobSearchFormValues } from './jobSearchFormModel';

type JobSearchFiltersProps = {
  isOpen: boolean;
  isSearching: boolean;
  onReset: () => void;
};

function JobSearchFilters({ isOpen, isSearching, onReset }: JobSearchFiltersProps) {
  const { t } = useTranslation();
  const {
    formState: { errors },
    register,
  } = useFormContext<JobSearchFormValues>();

  return (
    <div
      id="job-search-filters"
      className={`grid overflow-hidden transition-[grid-template-rows,opacity] duration-200 ease-out motion-reduce:transition-none ${
        isOpen ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'
      }`}
      aria-hidden={!isOpen}
      inert={!isOpen}
    >
      <div className="min-h-0 overflow-hidden">
        <div className="grid gap-4 border-t border-border pt-4">
          <div className="grid items-end gap-4 md:grid-cols-2 xl:grid-cols-4">
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

          <div className="flex flex-col gap-4 xl:flex-row xl:items-end">
            <JobLocationFilter isSearching={isSearching} />

            <div className="flex justify-end xl:shrink-0">
              <button
                className="rounded-lg border border-border bg-surface px-4.5 py-3 font-bold text-heading transition hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft"
                disabled={isSearching}
                type="button"
                onClick={onReset}
              >
                {t('jobs.search.actions.reset')}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default JobSearchFilters;
