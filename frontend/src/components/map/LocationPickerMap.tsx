import L from 'leaflet';
import { Marker, useMapEvents } from 'react-leaflet';

import BaseMap from './BaseMap';

type SelectedLocation = {
  latitude: number;
  longitude: number;
};

type LocationPickerMapProps = {
  label: string;
  selectedLocation: SelectedLocation | null;
  onSelectLocation: (location: SelectedLocation) => void;
};

type MapClickHandlerProps = {
  onSelectLocation: (location: SelectedLocation) => void;
};

const DEFAULT_CENTER: [number, number] = [46, 23];

const selectedLocationIcon = L.divIcon({
  className: '',
  html: '<span class="block h-4 w-4 rounded-full border-2 border-white bg-primary shadow-panel"></span>',
  iconAnchor: [8, 8],
  iconSize: [16, 16],
});

function MapClickHandler({ onSelectLocation }: MapClickHandlerProps) {
  useMapEvents({
    click(event) {
      onSelectLocation({
        latitude: event.latlng.lat,
        longitude: event.latlng.lng,
      });
    },
  });

  return null;
}

function LocationPickerMap({ label, selectedLocation, onSelectLocation }: LocationPickerMapProps) {
  const center: [number, number] = selectedLocation
    ? [selectedLocation.latitude, selectedLocation.longitude]
    : DEFAULT_CENTER;

  return (
    <section className="overflow-hidden rounded-lg border border-border bg-page">
      <div className="border-b border-border px-4 py-3">
        <h2 className="text-base font-bold text-heading">{label}</h2>
      </div>
      <BaseMap center={center} zoom={selectedLocation ? 15 : 13}>
        <MapClickHandler onSelectLocation={onSelectLocation} />
        {selectedLocation ? <Marker icon={selectedLocationIcon} position={center} /> : null}
      </BaseMap>
    </section>
  );
}

export default LocationPickerMap;
