CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE OR REPLACE FUNCTION calculate_distance_km(
    first_latitude NUMERIC,
    first_longitude NUMERIC,
    second_latitude NUMERIC,
    second_longitude NUMERIC
)
RETURNS DOUBLE PRECISION
LANGUAGE SQL
IMMUTABLE
PARALLEL SAFE
RETURN ST_DistanceSphere(
    ST_SetSRID(ST_MakePoint(first_longitude::DOUBLE PRECISION, first_latitude::DOUBLE PRECISION), 4326),
    ST_SetSRID(ST_MakePoint(second_longitude::DOUBLE PRECISION, second_latitude::DOUBLE PRECISION), 4326)
) / 1000.0;
