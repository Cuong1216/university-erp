from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import pandas as pd
import numpy as np
import logging
from datetime import datetime
from dateutil.relativedelta import relativedelta
import json, os
import joblib

MODEL_STORE_DIR = "model_store"
PROPHET_MODEL_PATH = os.path.join(MODEL_STORE_DIR, "prophet_model.pkl")

# Global cache for the loaded model
_cached_prophet_model = Nonedef load_seasonal_config():
    return {
        "semester1_months": list(map(int, os.getenv("SEMESTER1_MONTHS", "9,10").split(","))),
        "semester1_multiplier": float(os.getenv("SEMESTER1_MULTIPLIER", "1.12")),
        "semester2_months": list(map(int, os.getenv("SEMESTER2_MONTHS", "2,3").split(","))),
        "semester2_multiplier": float(os.getenv("SEMESTER2_MULTIPLIER", "1.08")),
        "summer_months": list(map(int, os.getenv("SUMMER_MONTHS", "6,7").split(","))),
        "summer_multiplier": float(os.getenv("SUMMER_MULTIPLIER", "0.92")),
    }
SEASONAL_CONFIG = load_seasonal_config()

def get_seasonal_multiplier(month_num, config):
    if month_num in config["semester1_months"]:
        return config["semester1_multiplier"]
    elif month_num in config["semester2_months"]:
        return config["semester2_multiplier"]
    elif month_num in config["summer_months"]:
        return config["summer_multiplier"]
    return 1.0

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
    history: Optional[List[DataPoint]] = []
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

@app.get("/config")
def get_config():
    return {"seasonal_config": SEASONAL_CONFIG, "version": "1.0.0"}

@app.post("/forecast", response_model=ForecastResponse)
def forecast_salary(req: ForecastRequest):
    global _cached_prophet_model
    
    logger.info(f"Received salary forecast request for {req.periods} periods.")
    
    periods = req.periods if req.periods and req.periods > 0 else 6

    # Attempt to load the pre-trained Prophet model
    model_used = "Prophet (Time Series & Seasonality Engine)"
    try:
        if _cached_prophet_model is None:
            if not os.path.exists(PROPHET_MODEL_PATH):
                raise FileNotFoundError(f"Model file not found at {PROPHET_MODEL_PATH}")
            logger.info("Loading Prophet model from disk...")
            _cached_prophet_model = joblib.load(PROPHET_MODEL_PATH)
        
        m = _cached_prophet_model
        
        future = m.make_future_dataframe(periods=periods, freq='MS')
        forecast_df = m.predict(future)
        
        # Get future points (after the last date in the training data history)
        last_history_date = m.history_dates.max()
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
        
    except FileNotFoundError as e:
        logger.error(f"Prophet model not available: {str(e)}")
        # If we have history in the request, fallback to Harmonic Regression
        if not req.history or len(req.history) < 2:
            raise HTTPException(status_code=503, detail="Mô hình Prophet chưa sẵn sàng (đang train). Vui lòng cung cấp 'history' để dùng mô hình dự phòng, hoặc thử lại sau.")
        logger.warning("Switching to Harmonic Trend Regression due to missing Prophet model.")
        
    except Exception as e:
        logger.error(f"Prophet execution failed: {str(e)}")
        if not req.history or len(req.history) < 2:
            raise HTTPException(status_code=500, detail="Lỗi dự báo Prophet và không đủ dữ liệu history để dùng mô hình dự phòng.")
        logger.warning("Switching to Harmonic Trend Regression.")

    # Fallback/Hybrid engine: Linear Trend + Semester Fourier Seasonality
    model_used = "Harmonic Fourier Trend Regression (Hybrid Engine)"
    df = pd.DataFrame([{"ds": pd.to_datetime(p.ds), "y": float(p.y)} for p in req.history])
    df = df.sort_values("ds").reset_index(drop=True)

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
        seasonal_multiplier = get_seasonal_multiplier(month_num, SEASONAL_CONFIG)

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

