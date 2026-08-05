import { createContext } from 'react';
import type { Client } from '@stomp/stompjs';

export type WebSocketContextValue = {
  client: Client | null;
  isConnected: boolean;
};

const WebSocketContext = createContext<WebSocketContextValue | undefined>(undefined);

export default WebSocketContext;
