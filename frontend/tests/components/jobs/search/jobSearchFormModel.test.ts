import { describe, expect, it } from 'vitest';

import {
  toJobSearchParams,
  type JobSearchFormValues,
} from '../../../../src/components/jobs/search/jobSearchFormModel';

describe('jobSearchFormModel', () => {
  it('trims and converts submitted search values to API query parameters', () => {
    const values: JobSearchFormValues = {
      q: '  garden work  ',
      category: 'GARDENING',
      minHourlyRate: '20',
      maxHourlyRate: '50.5',
      sort: 'hourlyRate,desc',
      latitude: '46.77',
      longitude: '23.59',
      maxDistanceKm: '25',
    };

    expect(toJobSearchParams(values, 2, 24)).toEqual({
      page: 2,
      size: 24,
      sort: 'hourlyRate,desc',
      q: 'garden work',
      category: 'GARDENING',
      minHourlyRate: 20,
      maxHourlyRate: 50.5,
      latitude: 46.77,
      longitude: 23.59,
      maxDistanceKm: 25,
    });
  });
});
