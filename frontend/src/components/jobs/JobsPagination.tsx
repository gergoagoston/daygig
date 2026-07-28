import { useTranslation } from 'react-i18next';

const pageSizes = [12, 24, 48] as const;

type JobsPaginationProps = {
  isFetching: boolean;
  page: number;
  size: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
};

function JobsPagination({
  isFetching,
  page,
  size,
  totalPages,
  onPageChange,
  onPageSizeChange,
}: JobsPaginationProps) {
  const { t } = useTranslation();
  const isFirstPage = page <= 0;
  const isLastPage = page >= totalPages - 1;

  return (
    <nav
      className="flex flex-col gap-4 pt-5 sm:flex-row sm:items-center sm:justify-between"
      aria-label={t('jobs.pagination.label')}
    >
      <label
        className="flex items-center gap-3 text-sm font-semibold text-heading"
        htmlFor="jobs-page-size"
      >
        {t('jobs.pagination.page_size')}
        <select
          id="jobs-page-size"
          className="rounded-lg border border-border bg-surface px-3 py-2 text-heading outline-none focus:border-primary focus:ring-2 focus:ring-primary-soft"
          disabled={isFetching}
          value={size}
          onChange={(event) => onPageSizeChange(Number(event.target.value))}
        >
          {pageSizes.map((pageSize) => (
            <option key={pageSize} value={pageSize}>
              {pageSize}
            </option>
          ))}
        </select>
      </label>

      <div className="flex items-center justify-between gap-3 sm:justify-end">
        <button
          className="rounded-lg border border-border bg-surface px-4 py-2.5 font-bold text-heading transition enabled:hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft disabled:cursor-not-allowed disabled:opacity-50"
          disabled={isFetching || isFirstPage}
          type="button"
          onClick={() => onPageChange(page - 1)}
        >
          {t('jobs.pagination.previous')}
        </button>

        <p className="min-w-28 text-center text-sm font-semibold text-body" aria-live="polite">
          {t('jobs.pagination.page_status', {
            current: page + 1,
            total: totalPages,
          })}
        </p>

        <button
          className="rounded-lg border border-border bg-surface px-4 py-2.5 font-bold text-heading transition enabled:hover:border-primary focus:outline-none focus:ring-2 focus:ring-primary-soft disabled:cursor-not-allowed disabled:opacity-50"
          disabled={isFetching || isLastPage}
          type="button"
          onClick={() => onPageChange(page + 1)}
        >
          {t('jobs.pagination.next')}
        </button>
      </div>
    </nav>
  );
}

export default JobsPagination;
