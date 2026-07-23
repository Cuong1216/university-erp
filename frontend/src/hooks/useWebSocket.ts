import { useEffect, useRef, useCallback } from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '../store/useAuthStore';

export interface WebSocketNotification {
  type: 'CHOT_LUONG_SUCCESS' | 'CHOT_LUONG_ERROR' | string;
  status: 'SUCCESS' | 'ERROR' | string;
  message: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  data?: any;
  timestamp: number;
}

interface UseWebSocketProps {
  onNotificationReceived: (notification: WebSocketNotification) => void;
}

export const useWebSocket = ({ onNotificationReceived }: UseWebSocketProps) => {
  const { accessToken, isLoggedIn } = useAuthStore();
  const stompClientRef = useRef<Client | null>(null);

  const connect = useCallback(() => {
    if (!isLoggedIn || !accessToken) return;

    const socket = new SockJS('http://localhost:8080/ws');

    const stompClient = new Client({
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      webSocketFactory: () => socket as any,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.debug('[STOMP]:', str);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = () => {
      console.log('✅ WebSocket/STOMP connected successfully!');

      stompClient.subscribe('/user/queue/notifications', (message: IMessage) => {
        if (message.body) {
          try {
            const parsedNotification: WebSocketNotification = JSON.parse(message.body);
            onNotificationReceived(parsedNotification);
          } catch (err) {
            console.error('Lỗi parse JSON notification từ WebSocket:', err);
          }
        }
      });
    };

    stompClient.onStompError = (frame) => {
      console.error('❌ Broker reported STOMP error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    stompClient.activate();
    stompClientRef.current = stompClient;
  }, [accessToken, isLoggedIn, onNotificationReceived]);

  useEffect(() => {
    connect();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        console.log('🛑 WebSocket/STOMP disconnected on unmount.');
      }
    };
  }, [connect]);

};
