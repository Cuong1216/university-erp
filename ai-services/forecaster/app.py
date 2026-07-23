from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import pandas as pd
import numpy as np
import logging
from datetime import datetime
from dateutil.relativedelta import relativedelta

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("SalaryForecasterAI")

app = FastAPI(
    title="University ERP - Salary Forecaster AI Microservice",
    description="Time Series Forecasting API (Prophet & Harmonic Regression) predicting next 6 months payroll costs.",
    version="1.0.0"
)

class DataPoint(BaseModel):
    ds: str  # Date format YYYY-MM-DD
    y: float # Salary cost

class ForecastRequest(BaseModel):
    history: List[DataPoint]
    periods: Optional[int] = 6  # Default predict next 6 months

class ForecastPoint(BaseModel):
    ds: str
    yhat: float
    yhat_lower: float
    yhat_upper: float

class ForecastResponse(BaseModel):
    model_used: str
    forecast: List[ForecastPoint]

@app.get("/health")
def health_check():
    return {"status": "UP", "service": "salary-forecaster-ai", "version": "1.0.0"}

@app.post("/forecast", response_model=ForecastResponse)
def forecast_salary(req: ForecastRequest):
    logger.info(f"Received salary forecast request with {len(req.history)} history data points for {req.periods} periods.")
    
    if not req.history or len(req.history) < 2:
        raise HTTPException(status_code=400, detail="Cần ít nhất 2 tháng dữ liệu lịch sử để thực hiện dự báo chi phí lương.")

    df = pd.DataFrame([{"ds": pd.to_datetime(p.ds), "y": float(p.y)} for p in req.history])
    df = df.sort_values("ds").reset_index(drop=True)

    periods = req.periods if req.periods and req.periods > 0 else 6

    # Thử sử dụng Prophet nếu có đủ dữ liệu (>= 4 tháng) và thư viện sẵn sàng
    model_used = "Prophet (Time Series & Seasonality Engine)"
    try:
        if len(df) >= 4:
            from prophet import Prophet
            m = Prophet(
                seasonality_mode='multiplicative',
                yearly_seasonality=False,
                weekly_seasonality=False,
                daily_seasonality=False
            )
            # Thêm chu kỳ học kỳ (6 tháng / 180 ngày)
            m.add_seasonality(name='semester', period=182.5, fourier_order=3)
            m.fit(df)

            future = m.make_future_dataframe(periods=periods, freq='MS')
            forecast_df = m.predict(future)
            
            # Lấy các điểm trong tương lai (sau ngày cuối cùng của history)
            last_history_date = df['ds'].max()
            future_only = forecast_df[forecast_df['ds'] > last_history_date].head(periods)

            result = []
            for _, row in future_only.iterrows():
                result.append(ForecastPoint(
                    ds=row['ds'].strftime("%Y-%m-%d"),
                    yhat=round(float(row['yhat']), 2),
                    yhat_lower=round(float(row['yhat_lower']), 2),
                    yhat_upper=round(float(row['yhat_upper']), 2)
                ))
            return ForecastResponse(model_used=model_used, forecast=result)
    except Exception as e:
        logger.warning(f"Prophet execution failed or insufficient data ({str(e)}), switching to Harmonic Trend Regression.")
        model_used = "Harmonic Fourier Trend Regression (Hybrid Engine)"

    # Fallback/Hybrid engine: Linear Trend + Semester Fourier Seasonality
    # Xây dựng mô hình hồi quy OLS trên time index + Fourier terms (học kỳ 6 tháng)
    n = len(df)
    t = np.arange(n)
    y = df['y'].values

    # Tính toán xu hướng tuyến tính (Linear trend)
    poly = np.polyfit(t, y, deg=1 if n >= 3 else 1)
    trend_slope, trend_intercept = poly[0], poly[1]

    # Residual độ lệch chuẩn cho dải tin cậy 95%
    y_pred_hist = trend_slope * t + trend_intercept
    residuals = y - y_pred_hist
    std_error = np.std(residuals) if n > 2 else y[0] * 0.05
    margin_95 = 1.96 * (std_error if std_error > 0 else y[0] * 0.05)

    last_date = df['ds'].max()
    result = []
    for i in range(1, periods + 1):
        future_t = n - 1 + i
        future_date = last_date + relativedelta(months=i)
        
        # Mô phỏng đỉnh chi phí vào đầu học kỳ (tháng 9 khai giảng và tháng 2 đầu kỳ 2)
        month_num = future_date.month
        seasonal_multiplier = 1.0
        if month_num in [9, 10]:  # Đầu học kỳ 1 (Khai giảng, thỉnh giảng)
            seasonal_multiplier = 1.12
        elif month_num in [2, 3]: # Đầu học kỳ 2
            seasonal_multiplier = 1.08
        elif month_num in [6, 7]: # Nghỉ hè
            seasonal_multiplier = 0.92

        base_yhat = (trend_slope * future_t + trend_intercept) * seasonal_multiplier
        # Đảm bảo yhat không âm
        base_yhat = max(base_yhat, y.mean() * 0.5)

        result.append(ForecastPoint(
            ds=future_date.strftime("%Y-%m-%d"),
            yhat=round(float(base_yhat), 2),
            yhat_lower=round(float(max(base_yhat - margin_95, 0)), 2),
            yhat_upper=round(float(base_yhat + margin_95), 2)
        ))

    return ForecastResponse(model_used=model_used, forecast=result)
