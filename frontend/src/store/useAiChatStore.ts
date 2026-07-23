import { create } from 'zustand';
import { aiApi } from '../api/aiApi';

export interface ChatMessage {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  timestamp: number;
}

interface AiChatState {
  isOpen: boolean;
  loading: boolean;
  messages: ChatMessage[];
  toggleDrawer: () => void;
  openDrawer: () => void;
  closeDrawer: () => void;
  sendMessage: (text: string) => Promise<void>;
  clearMessages: () => void;
}

const INITIAL_GREETING: ChatMessage = {
  id: 'init-greeting',
  sender: 'bot',
  text: 'Xin chào! Tôi là **ERP Copilot** - Trợ lý AI hỏi đáp dữ liệu DB bằng Tiếng Việt.\n\nBạn có thể chọn các gợi ý bên dưới hoặc hỏi tôi bất kỳ thông tin nào về tiền lương, giảng viên và giảng dạy nhé!',
  timestamp: Date.now(),
};

export const useAiChatStore = create<AiChatState>((set) => ({
  isOpen: false,

  loading: false,
  messages: [INITIAL_GREETING],

  toggleDrawer: () => set((state) => ({ isOpen: !state.isOpen })),
  openDrawer: () => set({ isOpen: true }),
  closeDrawer: () => set({ isOpen: false }),

  clearMessages: () => set({ messages: [INITIAL_GREETING] }),

  sendMessage: async (text: string) => {
    if (!text.trim()) return;

    const userMessage: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: text.trim(),
      timestamp: Date.now(),
    };

    set((state) => ({
      messages: [...state.messages, userMessage],
      loading: true,
    }));

    try {
      const response = await aiApi.chat(text.trim());
      const botMessage: ChatMessage = {
        id: `bot-${Date.now()}`,
        sender: 'bot',
        text: response.reply,
        timestamp: response.timestamp || Date.now(),
      };
      set((state) => ({
        messages: [...state.messages, botMessage],
        loading: false,
      }));
    } catch (error: any) {
      const errorMsg =
        error?.response?.data?.reply ||
        'Xin lỗi, kết nối tới máy chủ AI gặp sự cố. Vui lòng kiểm tra lại cấu hình API key hoặc kết nối mạng.';
      const errorMessage: ChatMessage = {
        id: `bot-err-${Date.now()}`,
        sender: 'bot',
        text: errorMsg,
        timestamp: Date.now(),
      };
      set((state) => ({
        messages: [...state.messages, errorMessage],
        loading: false,
      }));
    }
  },
}));
