import { axiosClient } from './axiosClient';

export interface AiChatResponse {
  reply: string;
  timestamp: number;
}

export const aiApi = {
  chat: async (message: string): Promise<AiChatResponse> => {
    const response = await axiosClient.post<AiChatResponse>('/ai/chat', { message });
    return response.data;
  },
};
