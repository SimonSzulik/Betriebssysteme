#/******************************/#
#/*** Simon Szulik     1474315 */#
#/*** Betriebssysteme WS 24/25 */#
#/******************************/#
FROM python:3.10-slim

WORKDIR /app
COPY server.py requirements.txt ./
RUN pip install -r requirements.txt

CMD ["python", "server.py"]
