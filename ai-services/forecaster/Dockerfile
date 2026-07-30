FROM python:3.11-slim

WORKDIR /app

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

ENV SEMESTER1_MONTHS="9,10" \
    SEMESTER1_MULTIPLIER="1.12" \
    SEMESTER2_MONTHS="2,3" \
    SEMESTER2_MULTIPLIER="1.08" \
    SUMMER_MONTHS="6,7" \
    SUMMER_MULTIPLIER="0.92"

# Install required OS dependencies for numpy/pandas/prophet build
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app.py .

EXPOSE 8001

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8001/health || exit 1

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8001"]
