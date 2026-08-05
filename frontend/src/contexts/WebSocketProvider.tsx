import { Client } from '@stomp/stompjs';
import { type ReactNode, useEffect, useMemo, useState } from 'react';

import useAuth from '../hooks/useAuth';
import WebSocketContext from './WebSocketContext';

type WebSocketProviderProps = {
  children: ReactNode;
};

function websocketUrl(): string {
  const configuredUrl = import.meta.env.VITE_WEBSOCKET_URL;
  if (configuredUrl) {
    return configuredUrl;
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/api/ws`;
}

function WebSocketProvider({ children }: WebSocketProviderProps) {
  const { isAuthenticated, isInitializing } = useAuth();
  const [isConnected, setIsConnected] = useState(false);
  const enabled = isAuthenticated && !isInitializing;

  const client = useMemo(() => {
    if (!enabled) {
      return null;
    }

    return new Client({
      brokerURL: websocketUrl(),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => setIsConnected(true),
      onDisconnect: () => setIsConnected(false),
      onWebSocketClose: () => setIsConnected(false),
      onStompError: () => setIsConnected(false),
    });
  }, [enabled]);

  useEffect(() => {
    if (!client) {
      return;
    }

    client.activate();
    return () => {
      setIsConnected(false);
      void client.deactivate();
    };
  }, [client]);

  return (
    <WebSocketContext.Provider value={{ client, isConnected }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export default WebSocketProvider;
