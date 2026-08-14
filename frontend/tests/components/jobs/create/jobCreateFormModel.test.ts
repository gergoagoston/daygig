import { describe, expect, it } from 'vitest';

import {
  createJobSchema,
  toJobCreatePayload,
} from '../../../../src/components/jobs/create/jobCreateFormModel';

const t = (key: string) => key;

describe('jobCreateFormModel', () => {
  it('validates and converts a valid job creation form', () => {
    const values = {
      title: '  Garden help  ',
      description: '  Mow the lawn  ',
      category: 'GARDENING',
      hourlyRate: '35.5',
      locationName: '  Cluj-Napoca  ',
      latitude: '46.77',
      longitude: '23.59',
    };

    expect(createJobSchema(t).safeParse(values).success).toBe(true);
    expect(toJobCreatePayload(values)).toEqual({
      title: 'Garden help',
      description: 'Mow the lawn',
      category: 'GARDENING',
      hourlyRate: 35.5,
      locationName: 'Cluj-Napoca',
      latitude: 46.77,
      longitude: 23.59,
    });
  });
});
