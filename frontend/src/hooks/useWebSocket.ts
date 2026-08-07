import { useContext } from 'react';

import WebSocketContext from '../contexts/WebSocketContext';

function useWebSocket() {
  const context = useContext(WebSocketContext);

  if (!context) {
    throw new Error('useWebSocket must be used within WebSocketProvider.');
  }

  return context;
}

export default useWebSocket;
