import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import translationEN from './locales/en.json';
import translationHU from './locales/hu.json';

const resources = {
  en: { translation: translationEN },
  hu: { translation: translationHU },
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    lng: 'en', // <--- Sets English as the DEFAULT language
    fallbackLng: 'en', // <--- Sets English as the FALLBACK language too
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
