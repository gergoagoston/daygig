import { useState } from 'react';
import { useTranslation } from 'react-i18next';

type StarRatingInputProps = {
  disabled?: boolean;
  onChange: (value: number) => void;
  value: number;
};

function StarRatingInput({ disabled = false, onChange, value }: StarRatingInputProps) {
  const { t } = useTranslation();
  const [hoveredValue, setHoveredValue] = useState<number | null>(null);
  const displayedValue = hoveredValue ?? value;

  return (
    <fieldset disabled={disabled}>
      <legend className="mb-2 font-bold text-heading">{t('ratings.form.stars')}</legend>
      <div
        className="flex gap-1"
        role="radiogroup"
        aria-label={t('ratings.form.stars')}
        onMouseLeave={() => setHoveredValue(null)}
      >
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            className={`h-11 w-11 text-4xl leading-none transition ${
              star <= displayedValue ? 'text-primary' : 'text-border'
            } focus:outline-none focus:ring-2 focus:ring-primary-soft`}
            type="button"
            role="radio"
            aria-checked={value === star}
            aria-label={t('ratings.form.star_value', { count: star })}
            onClick={() => onChange(star)}
            onMouseEnter={() => setHoveredValue(star)}
          >
            ★
          </button>
        ))}
      </div>
    </fieldset>
  );
}

export default StarRatingInput;
