import './App.css';
import { useTranslation } from 'react-i18next';

function App() {
  const { t } = useTranslation();

  return (
    <>
      <h1>{t('auth.login.title')}</h1>
      <h1>{t('auth.login.submit_button')}</h1>
    </>
  );
}

export default App;
