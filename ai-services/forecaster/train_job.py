import os
import time
import logging
import pandas as pd
import joblib
from datetime import datetime
from apscheduler.schedulers.blocking import BlockingScheduler

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("TrainJob")

MODEL_STORE_DIR = "model_store"
PROPHET_MODEL_PATH = os.path.join(MODEL_STORE_DIR, "prophet_model.pkl")

# Ensure model directory exists
os.makedirs(MODEL_STORE_DIR, exist_ok=True)

def fetch_training_data_from_db():
    """
    Mock function to fetch training data from the database.
    Replace this with your actual DB connection code (e.g., SQLAlchemy/psycopg2).
    """
    logger.info("Fetching training data from database...")
    # Example:
    # engine = create_engine(os.getenv("DATABASE_URL"))
    # query = "SELECT date as ds, cost as y FROM salary_history"
    # df = pd.read_sql(query, engine)
    
    # Mock data for demonstration purposes
    import numpy as np
    dates = pd.date_range(start='2020-01-01', periods=36, freq='MS')
    costs = np.random.normal(loc=50000, scale=5000, size=36)
    df = pd.DataFrame({'ds': dates, 'y': costs})
    
    return df

def train_prophet_model():
    logger.info("Starting Prophet model training job...")
    try:
        df = fetch_training_data_from_db()
        
        if len(df) < 4:
            logger.warning("Not enough data to train Prophet model (needs at least 4 data points).")
            return
            
        from prophet import Prophet
        m = Prophet(
            seasonality_mode='multiplicative',
            yearly_seasonality=False,
            weekly_seasonality=False,
            daily_seasonality=False
        )
        # Add semester seasonality (6 months / 182.5 days)
        m.add_seasonality(name='semester', period=182.5, fourier_order=3)
        m.fit(df)
        
        # Save model to disk
        joblib.dump(m, PROPHET_MODEL_PATH)
        logger.info(f"Model successfully trained and saved to {PROPHET_MODEL_PATH}")
        
    except Exception as e:
        logger.error(f"Failed to train Prophet model: {e}")

if __name__ == "__main__":
    logger.info("Starting APScheduler for training job...")
    
    # Run once immediately on startup
    train_prophet_model()
    
    # Setup scheduler
    scheduler = BlockingScheduler()
    # Schedule to run every day at midnight
    scheduler.add_job(train_prophet_model, 'cron', hour=0, minute=0)
    
    try:
        scheduler.start()
    except (KeyboardInterrupt, SystemExit):
        logger.info("Scheduler stopped.")
