import { Circle } from 'react-leaflet';

import BaseMap from './BaseMap';

type ApproximateLocationMapProps = {
  fallback: string;
  label: string;
  latitude: number;
  longitude: number;
  radiusMeters: number;
};

function hasValidLocation(latitude: number, longitude: number, radiusMeters: number) {
  return (
    Number.isFinite(latitude) &&
    Number.isFinite(longitude) &&
    Number.isFinite(radiusMeters) &&
    radiusMeters > 0
  );
}

function getZoomForRadius(radiusMeters: number) {
  if (radiusMeters >= 3000) {
    return 12;
  }

  if (radiusMeters >= 1500) {
    return 13;
  }

  return 14;
}

function ApproximateLocationMap({
  fallback,
  label,
  latitude,
  longitude,
  radiusMeters,
}: ApproximateLocationMapProps) {
  if (!hasValidLocation(latitude, longitude, radiusMeters)) {
    return (
      <section className="rounded-lg border border-border bg-page p-4">
        <h2 className="mb-2 text-base font-bold text-heading">{label}</h2>
        <p className="text-body">{fallback}</p>
      </section>
    );
  }

  const center: [number, number] = [latitude, longitude];

  return (
    <section className="overflow-hidden rounded-lg border border-border bg-page">
      <div className="border-b border-border px-4 py-3">
        <h2 className="text-base font-bold text-heading">{label}</h2>
      </div>
      <BaseMap center={center} zoom={getZoomForRadius(radiusMeters)}>
        <Circle
          center={center}
          pathOptions={{
            color: '#aa3bff',
            fillColor: '#aa3bff',
            fillOpacity: 0.16,
            opacity: 0.7,
            weight: 2,
          }}
          radius={radiusMeters}
        />
      </BaseMap>
    </section>
  );
}

export default ApproximateLocationMap;
