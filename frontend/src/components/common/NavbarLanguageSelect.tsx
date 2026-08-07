import { useTranslation } from 'react-i18next';

import { changeCurrentLanguage, getCurrentLanguage } from '../../i18n';

const languages = [
  { code: 'hu', label: 'HU' },
  { code: 'en', label: 'EN' },
  { code: 'ro', label: 'RO' },
] as const;

type NavbarLanguageSelectProps = {
  showLabel?: boolean;
};

function NavbarLanguageSelect({ showLabel = false }: NavbarLanguageSelectProps) {
  const { t } = useTranslation();
  const currentLanguage = getCurrentLanguage();

  return (
    <label className={showLabel ? 'grid gap-1 text-sm font-bold text-heading' : undefined}>
      <span className={showLabel ? undefined : 'sr-only'}>{t('navigation.language')}</span>
      <select
        className={`${showLabel ? 'w-full ' : ''}rounded-lg border border-border bg-surface px-3 py-2 text-sm font-bold text-heading outline-none hover:border-primary focus:border-primary`}
        value={currentLanguage.slice(0, 2)}
        onChange={(event) => changeCurrentLanguage(event.target.value)}
      >
        {languages.map((language) => (
          <option key={language.code} value={language.code}>
            {language.label}
          </option>
        ))}
      </select>
    </label>
  );
}

export default NavbarLanguageSelect;
