export type JobCategory =
  | 'CLEANING'
  | 'MOVING'
  | 'GARDENING'
  | 'DELIVERY'
  | 'BABYSITTING'
  | 'PET_CARE'
  | 'HANDYMAN'
  | 'EVENT_HELP'
  | 'OTHER';

export type JobStatus = 'OPEN' | 'ASSIGNED' | 'COMPLETED' | 'CANCELLED';

export type Currency = 'RON' | 'EUR';

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type Job = {
  id: string;
  title: string;
  description: string;
  category: JobCategory;
  hourlyRate: number;
  currency: Currency;
  locationName: string;
  createdAt: string;
};

export type JobDetails = Job & {
  status: JobStatus;
  latitude: number;
  longitude: number;
  updatedAt: string;
};

export type JobsQueryParams = {
  page?: number;
  size?: number;
  sort?: string | string[];
};
