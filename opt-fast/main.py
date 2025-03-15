from fastapi import FastAPI, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from conf.database import get_db  # ✅ DB 세션 가져오기
from services.kafka_consumer_service import KafkaConsumerService
from flask_app.app import app as flask_app
from starlette.middleware.wsgi import WSGIMiddleware
import asyncio
import logging

# ✅ FastAPI 애플리케이션 생성
fastapi_app = FastAPI()

# ✅ Flask 앱을 FastAPI에 마운트
fastapi_app.mount("/flask", WSGIMiddleware(flask_app))

# ✅ 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ✅ Kafka Consumer 인스턴스 초기화 (전역 변수)
kafka_consumer_service = None

@fastapi_app.on_event("startup")
async def startup_event():
    """FastAPI 시작 시 Kafka Consumer 실행"""
    global kafka_consumer_service

    # ✅ `async with` 사용하여 `AsyncSession`을 가져옴
    async for db in get_db():  
        kafka_consumer_service = KafkaConsumerService(
            bootstrap_servers="kafka:9092",
            group_id="business_license_group",
            db=db  # ✅ db 인자 추가
        )
        break  # ✅ 한 번만 실행되도록 `break` 추가

    await kafka_consumer_service.start()  # Kafka Producer 실행

    # ✅ 여러 개의 토픽을 동시에 실행
    topics = ["business_license_request", "certificate_request"]
    asyncio.create_task(kafka_consumer_service.start_consumer(topics))


@fastapi_app.on_event("shutdown")
async def shutdown_event():
    """FastAPI 종료 시 Kafka Consumer 정리"""
    global kafka_consumer_service

    if kafka_consumer_service:
        await kafka_consumer_service.stop()  # ✅ Kafka 종료
