import { useEffect, type ReactNode } from 'react';
import type { LatLngExpression } from 'leaflet';
import { AttributionControl, MapContainer, TileLayer, useMap } from 'react-leaflet';

type BaseMapProps = {
  center: LatLngExpression;
  children?: ReactNode;
  className?: string;
  zoom?: number;
};

type MapCenterControllerProps = {
  center: LatLngExpression;
  zoom: number;
};

const OPENSTREETMAP_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';

function MapCenterController({ center, zoom }: MapCenterControllerProps) {
  const map = useMap();

  useEffect(() => {
    map.setView(center, zoom);
  }, [center, map, zoom]);

  return null;
}

function BaseMap({ center, children, className = '', zoom = 13 }: BaseMapProps) {
  return (
    <MapContainer
      attributionControl={false}
      center={center}
      className={`h-72 w-full ${className}`}
      scrollWheelZoom
      zoom={zoom}
    >
      <MapCenterController center={center} zoom={zoom} />
      <AttributionControl prefix={false} />
      <TileLayer
        attribution={OPENSTREETMAP_ATTRIBUTION}
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {children}
    </MapContainer>
  );
}

export default BaseMap;
