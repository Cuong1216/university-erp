import { axiosClient } from './axiosClient';

export interface TuitionResponse {
  maHocPhi: string;
  maSv: string;
  namHoc: string;
  hocKy: number;
  soTienPhaiNop: number;
  soTienDaNop: number;
  soTienConLai: number;
  trangThai: string; // CHUA_NOP, NOP_MOT_PHAN, DA_NOP_DU
  hanNop: string;
}

export interface CreatePaymentUrlRequest {
  maHocPhi: string;
  amount?: number;
  returnUrl?: string;
  ipAddress?: string;
}

export interface CreatePaymentUrlResponse {
  paymentUrl: string;
  vnpTxnRef: string;
  amount: number;
  message: string;
}

export const paymentApi = {
  getMyTuitions: async (): Promise<TuitionResponse[]> => {
    const response = await axiosClient.get<TuitionResponse[]>('/payment/tuitions/me');
    return response.data;
  },

  getAllTuitions: async (): Promise<TuitionResponse[]> => {
    const response = await axiosClient.get<TuitionResponse[]>('/payment/tuitions/all');
    return response.data;
  },

  createPaymentUrl: async (request: CreatePaymentUrlRequest): Promise<CreatePaymentUrlResponse> => {
    const response = await axiosClient.post<CreatePaymentUrlResponse>('/payment/create-url', request);
    return response.data;
  },
};
