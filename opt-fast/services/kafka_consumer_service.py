from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from sqlalchemy.ext.asyncio import AsyncSession
from services.ocr_service import process_ocr
from services.business_validator import validate_business_info
from services.gym_finder import find_most_similar_gym
from services.document_aligner import scan_document
import json
import base64
import requests
import logging
import aiohttp
from io import BytesIO
import cv2
import numpy as np
import matplotlib.pyplot as plt
import requests
from google.cloud import documentai_v1beta3 as documentai
from fuzzywuzzy import process
import re
import json
import boto3
import uuid
import os

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
# ✅ AWS S3 설정
S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME")
S3_REGION = os.getenv("AWS_REGION")  # .env에서 불러오기
s3_client = boto3.client(
    "s3",
    aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
    aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY"),
    region_name=S3_REGION
)


class KafkaConsumerService:
    def __init__(self, bootstrap_servers, group_id, db):
        self.bootstrap_servers = bootstrap_servers
        self.group_id = group_id
        self.db = db
        self.consumer = None
        self.producer = AIOKafkaProducer(
            bootstrap_servers=self.bootstrap_servers,
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        )

    async def start(self):
        """Kafka Producer 시작"""
        await self.producer.start()

    async def stop(self):
        """Kafka Producer 종료"""
        await self.producer.stop()

    async def start_consumer(self, topics: list):
        """여러 개의 토픽을 소비하는 Consumer"""
        self.consumer = AIOKafkaConsumer(
            *topics,  # ✅ 여러 개의 토픽을 구독할 수 있도록 수정
            bootstrap_servers=self.bootstrap_servers,
            group_id=self.group_id
        )
        await self.consumer.start()

        logger.info(f"🚀 Kafka Consumer started and subscribed to topic: {topics}")

        try:
            async for message in self.consumer:
                await self.process_message(message)
        finally:
            await self.consumer.stop()

    async def process_message(self, message):
        """Kafka 메시지를 처리하는 함수 (토픽별 분기 처리)"""
        topic = message.topic  # ✅ 어떤 토픽에서 온 메시지인지 확인
        message_value = message.value.decode("utf-8")

        logger.info(f"📩 Received Kafka message from {topic}: {message_value}")

        try:
            data = json.loads(message_value)  # ✅ JSON 파싱

            if topic == "business_license_request":
                await self.handle_business_license_request(data)
            elif topic == "certificate_request":
                await self.handle_certificate_request(data)
            else:
                logger.warning(f"⚠️ Unknown topic: {topic}, message: {data}")

        except Exception as e:
            logger.error(f"❌ Error processing message from {topic}: {e}")

    async def handle_business_license_request(self, data) :
        """Kafka 메시지 처리 로직"""
        try:
            image_url = data["path"]
            user_id = int(data["id"])

            logger.info(f"🌍 S3에서 이미지 다운로드 시작 - URL: {image_url}, 사용자 ID: {user_id}")

            # 이미지 다운로드
            image_data = await self.download_image_from_s3(image_url)

            if not image_data:
                logger.error("❌ S3 이미지 다운로드 실패 - 데이터가 None입니다.")
                return

            logger.info(f"✅ S3 이미지 다운로드 완료 - 크기: {len(image_data)} bytes, 타입: {type(image_data)}")

            # Base64 디코딩 (불필요한 경우 제거 가능)
            try:
                file_bytes = BytesIO(image_data)
                logger.info(f"📂 디코딩 완료 타입: {type(file_bytes)}")
            except Exception as e:
                logger.error(f"❌ Base64 디코딩 실패: {e}")
                return

            # OCR 처리
            logger.info("🔍 OCR 처리 시작")
            ocr_result = await process_ocr(file_bytes)
            logger.info(f"🔍 OCR 처리 완료 - 결과: {ocr_result}")


            # 사업자등록 유효성 검증
            validated_result = validate_business_info([ocr_result])
            logger.info(f"✅ 사업자등록 검증 결과: {validated_result}")

            # 유효성 확인 및 로직 실행
            valid_status = validated_result["data"][0].get("valid", "")

            logger.info(f"📜 유효성 상태 코드: {valid_status}")

            if valid_status == "01":
                logger.info(f"✅ Gym 정보 찾기 시작! ")
                # 유효한 경우: Gym 정보 매칭
                # matched_gym = await find_most_similar_gym(ocr_result, self.db)
                matched_gym = await find_most_similar_gym(ocr_result, self.db)  # ✅ AsyncSession을 직접 사용


                if matched_gym:
                    gym_id = matched_gym.id  # Gym ID 설정
                logger.info(f"✅ 매칭된 Gym 정보: {matched_gym}")
                message = "트레이너 등록에 성공했습니다"

            elif valid_status == "02":
                # 폐업된 사업자 처리
                logger.warning("⚠️ 폐업된 사업자입니다.")
                message = "폐업한 사업자 입니다"
            else:
                # 유효하지 않은 데이터 처리
                logger.warning("⚠️ 유효하지 않은 사업자 등록 정보입니다.")
                message = "유효하지 않은 사업자 등록 정보입니다"
                        # ✅ Kafka 메시지 전송 (business_license_response)
            if matched_gym == None:
                gym_id = None
            response_message = {
                "user_id": user_id,
                "gym_id": gym_id,  # 매칭된 Gym이 없으면 None
                "message" : message
            }

            await self.send_kafka_message("business_license_response", response_message)
            logger.info(f"📤 Kafka 메시지 전송 완료: {response_message}")

        except Exception as e:
            logger.error(f"❌ Kafka 메시지 처리 중 오류 발생: {e}")

    async def handle_certificate_request(self, data):
        """Kafka 메시지 처리 로직 (자격증 OCR)"""
        try:
            image_url = data["path"]
            user_id = int(data["id"])

            logger.info(f"🌍 S3에서 이미지 다운로드 시작 - URL: {image_url}, 사용자 ID: {user_id}")

            # ✅ 이미지 다운로드
            image_data = await self.download_image_from_s3(image_url)

            if not image_data:
                logger.error("❌ S3 이미지 다운로드 실패 - 데이터가 None입니다.")
                return

            logger.info(f"✅ S3 이미지 다운로드 완료 - 크기: {len(image_data)} bytes, 타입: {type(image_data)}")

            # Base64 디코딩 (불필요한 경우 제거 가능)
            try:
                file_bytes = BytesIO(image_data)
                logger.info(f"📂 디코딩 완료 타입: {type(file_bytes)}")
            except Exception as e:
                logger.error(f"❌ Base64 디코딩 실패: {e}")
                return

            file_bytes.seek(0)  # 파일 포인터를 처음으로 이동
            file_bytes = np.frombuffer(file_bytes.read(), dtype=np.uint8)  # frombuffer 사용
            image = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)

            # ✅ scan_document() 호출 (파일 경로 대신 OpenCV 이미지 배열 전달)
            scanned_image = scan_document(image)
            if scanned_image is None:
                scanned_image = image
                # logger.error("❌ 문서 영역을 찾을 수 없습니다.")
                # final_result_with_id2 = {}
                # final_result_with_id2["status"] = "error"
                # final_result_with_id2["message"] = "문서를 인식할 수 없습니다. 문서가 전체가 보이도록 다시 촬영해주세요"
                # await self.send_kafka_message("certificate_response", final_result_with_id2)
                # return

            # ✅ OCR 수행
            # scanned_image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            logger.info('()()()()()()()()()()')
            ocr_result_document = self.run_ocr(scanned_image)
            logger.info(ocr_result_document.text)
            # logger.info(f"@@@@   {ocr_result_document}")
            ocr_result = ocr_result_document.text
            logger.info("+++++++++++++++++")
            logger.info(ocr_result)
            
            
            # logger.info(f"🔍 OCR 처리 완료 - 결과: {ocr_result}")

            # ✅ OCR 결과에서 자격증 정보 추출
            result = self.extract_certification_details(ocr_result)
            logger.info(f"🔍 OCR 결과 분석 완료: {result}")

            if result["status"] == "error":
                final_result_with_id3 = {}
                final_result_with_id3["status"] = "error"
                final_result_with_id3["message"] = result.get('message')
                await self.send_kafka_message("certificate_response", final_result_with_id3)
                return result

            masked_image = self.mask_first_certificate_number(scanned_image, ocr_result_document)
            # ✅ 자격증 정보 검증
            final_result = self.process_certification_result(result, masked_image)
            logger.info(f"✅ 최종 검증 결과: {final_result}")
            final_result_with_id = json.loads(final_result)  # JSON 문자열 → dict 변환
            final_result_with_id["id"] = user_id  # 사용자 ID 추가  
            final_result_with_id["path"] = image_url
            await self.send_kafka_message("certificate_response", final_result_with_id)
            # return final_result

        except Exception as e:
            logger.error(f"❌ handle_certificate_request 중 오류 발생: {e}")
            return {"status": "error", "message": str(e)}


    def order_points(pts):
        """
        4개의 좌표를 (좌상, 우상, 우하, 좌하) 순서로 정렬합니다.
        """
        pts = pts.reshape(4, 2)
        rect = np.zeros((4, 2), dtype="float32")
        
        # 좌표들의 x+y 값 합계를 계산하여, 가장 작은 값은 좌상, 가장 큰 값은 우하로 지정
        s = pts.sum(axis=1)
        rect[0] = pts[np.argmin(s)]  # 좌상
        rect[2] = pts[np.argmax(s)]  # 우하
        
        # 좌표들의 y-x 차이를 계산하여, 가장 작은 값은 우상, 가장 큰 값은 좌하로 지정
        diff = np.diff(pts, axis=1)
        rect[1] = pts[np.argmin(diff)]  # 우상
        rect[3] = pts[np.argmax(diff)]  # 좌하
        
        return rect
    
    def four_point_transform(self,image, pts):
        """
        입력 이미지와 4개 좌표(pts)를 받아 perspective 변환을 수행하고
        스캔된 문서 이미지를 반환합니다.
        """
        # 좌표 정렬
        rect = self.order_points(pts)
        (tl, tr, br, bl) = rect

        # 문서의 가로 길이를 계산 (좌측과 우측 상단/하단의 길이 중 큰 값)
        widthA = np.sqrt(((br[0] - bl[0]) ** 2) + ((br[1] - bl[1]) ** 2))
        widthB = np.sqrt(((tr[0] - tl[0]) ** 2) + ((tr[1] - tl[1]) ** 2))
        maxWidth = max(int(widthA), int(widthB))

        # 문서의 세로 길이를 계산 (상단과 하단 좌측/우측의 길이 중 큰 값)
        heightA = np.sqrt(((tr[0] - br[0]) ** 2) + ((tr[1] - br[1]) ** 2))
        heightB = np.sqrt(((tl[0] - bl[0]) ** 2) + ((tl[1] - bl[1]) ** 2))
        maxHeight = max(int(heightA), int(heightB))

        # 변환 후 대상 좌표 (좌상부터 시계방향)
        dst = np.array([
            [0, 0],
            [maxWidth - 1, 0],
            [maxWidth - 1, maxHeight - 1],
            [0, maxHeight - 1]
        ], dtype="float32")

        # 변환 행렬 계산 후 perspective transform 적용
        M = cv2.getPerspectiveTransform(rect, dst)
        warped = cv2.warpPerspective(image, M, (maxWidth, maxHeight))
        
        return warped

    # 프로젝트 정보 설정
    PROJECT_ID = "opt-ocr"  # GCP 프로젝트 ID
    LOCATION = "us"  # 프로세서 위치
    PROCESSOR_ID = "bfe9513a7655ad9e"  # 생성한 프로세서 ID

    # Document AI 클라이언트 생성
    client = documentai.DocumentProcessorServiceClient()

    def run_ocr(self, image):
        """ Google Document AI OCR 호출 (이미지 데이터를 직접 입력) """
        if image is None:
            print("🚨 OCR 실패: 이미지가 None입니다.")
            return None

        # OpenCV 이미지(Numpy 배열) → PNG 바이트 배열로 변환
        _, image_bytes = cv2.imencode(".png", image)  # PNG 형식으로 인코딩
        image_content = image_bytes.tobytes()  # 바이트 변환

        # Document AI 요청 생성
        request = documentai.ProcessRequest(
            name=f"projects/{self.PROJECT_ID}/locations/{self.LOCATION}/processors/{self.PROCESSOR_ID}",
            raw_document=documentai.RawDocument(content=image_content, mime_type="image/png"),
        )

        # OCR 실행
        result = self.client.process_document(request=request)
        logger.info("📝 OCR 결과:")
        logger.info(result.document.text)

        return result.document

    def extract_certification_details(self, text: str):
        # OCR 후처리를 위한 기준 키워드
        title_keywords = ["생활스포츠지도사", "스포츠지도사", "생활 스포츠"]
        
        # 자격증 종류 확인 (정확한 단어 매칭을 위해 정규식 사용)
        if not any(re.search(rf'\b{keyword}\b', text) for keyword in title_keywords):
            logger.info("❌ 생활스포츠지도사 자격증이 아닙니다.")
            return {"status": "error", "message": "생활스포츠지도사 자격증만 가능합니다."}

        lines = text.split('\n')
        cert_number = None
        name = None
        level = None
        category = None

        for line in lines:
            line = line.strip()
            if not line:
                continue

            logger.info(f"🔍 검사 중인 줄: '{line}'")
            
            # 자격증 번호 추출 (첫 번째 값만 저장)
            if cert_number is None:
                number_match = re.search(r'제\s*([\d\-]+)\s*호', line)
                if number_match:
                    cert_number = number_match.group(1)
                    logger.info(f"✅ 자격증 번호 추출됨: {cert_number}")

            # 이름 추출 (첫 번째 값만 저장) - 다양한 공백 패턴 허용
            if name is None:
                name_match = re.search(r'성\s*명\s*[:\s]*([가-힣]{2,4})', line)
                if name_match:
                    name = name_match.group(1)
                    logger.info(f"✅ 이름 추출됨: {name}")

            # 자격등급 추출 (첫 번째 값만 저장)
            if level is None:
                level_match = re.search(r'자격등급[:\s]*(\d+급)', line)
                if level_match:
                    level = level_match.group(1)
                    logger.info(f"✅ 자격등급 추출됨: {level}")

            # 자격종목 추출 (첫 번째 값만 저장)
            if category is None:
                category_match = re.search(r'자격종목[:\s]*([가-힣]+)', line)
                if category_match:
                    category = category_match.group(1)
                    logger.info(f"✅ 자격종목 추출됨: {category}")

        # 필수 조건 확인 및 누락된 필드 표시
        missing_fields = []
        if not cert_number:
            missing_fields.append("자격증 번호")
        if not name:
            missing_fields.append("이름")
        if not level:
            missing_fields.append("자격등급")
        if not category:
            missing_fields.append("자격종목")

        if missing_fields:
            logger.info(f"❌ 누락된 정보: {', '.join(missing_fields)}")
            return {"status": "error", "message": f"다음 정보가 누락되었습니다: {', '.join(missing_fields)}. 사진을 다시 찍어주세요."}
        
        logger.info("🎉 모든 정보가 정상적으로 추출되었습니다.")
        return {
            "status": "success", 
            "cert_number": cert_number, 
            "name": name, 
            "level": level, 
            "category": category
        }



    def fetch_certification_info(self, qf_no: str, srch_usr_nm: str):
        url = "https://sqms.kspo.or.kr/license/docTrueCheckActJs.kspo"
        
        # 요청 헤더 설정
        headers = {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
        }
        
        # 요청 페이로드 설정
        data = {
            "MENU_ID": "A05_B06",
            "CERTI_DIV_CD": "5",
            "APL_NO1": "",
            "APL_NO2": "",
            "QF_NO": qf_no,  # 자격증 번호
            "SRCH_USR_NM": srch_usr_nm,  # 이름
            # "SRCH_USR_NM": '유재겅',  # 이름
        }
        
        try:
            response = requests.post(url, headers=headers, data=data)
            response.raise_for_status()  # HTTP 오류 발생 시 예외 처리
            
            # JSON 응답 파싱
            data = response.json()
            print("응답 데이터:", data)
            return data
        except requests.exceptions.RequestException as e:
            print("데이터 요청 중 오류 발생:", e)
            return None

    def process_certification_result(self, result, image):
        data = self.fetch_certification_info(result.get('cert_number'), result.get('name'))
        if data is None or not isinstance(data, dict):
            return json.dumps({
                "status": "error", 
                "message": "API 응답이 없습니다. 서버 문제일 수 있습니다."
            }, ensure_ascii=False)
        
        if "msgCd" in data and data.get("msgCd") == "ERR_COMM_201":
            return json.dumps({
                "status": "error", 
                "message": "해당 데이터가 없습니다.", 
                "cert_number": result.get('cert_number'),
                "name": result.get('name')
            }, ensure_ascii=False)
        
        if "resultList" in data and data["resultList"]:
            cert_info = data["resultList"][0]
            url = self.upload_to_s3(image)
            return json.dumps({
                "status": "success",
                "cert_number": cert_info["QF_NO"],
                "name": cert_info["USR_NM"],
                "level": cert_info["QF_GRADE_NM"],
                "category": cert_info["QF_ITM_NM"],
                "acquisition_date": cert_info["AQ_DT"],
                "newPath": url
            }, ensure_ascii=False)
        
        return json.dumps({"status": "error", "message": "알 수 없는 오류가 발생했습니다."}, ensure_ascii=False)


    async def download_image_from_s3(self, url: str):
        print(1)
        async with aiohttp.ClientSession() as session:
            print(2)
            async with session.get(url) as response:
                print(3)
                if response.status == 200:
                    print(4)
                    image_data = await response.read()
                    print(5)
                    return image_data
                else:
                    raise Exception(f"Failed to download image: {response.status}")
    
    async def send_kafka_message(self, topic: str, message: dict):
        """Kafka 메시지 전송"""
        try:
            await self.producer.send_and_wait(topic, message)
            logger.info(f"📤 Kafka 메시지 전송 성공: {message}")
        except Exception as e:
            logger.error(f"❌ Kafka 메시지 전송 실패: {e}")

    def mask_first_certificate_number(self, image, ocr_document):
        """OCR 결과를 기반으로 '제 ~~호' 부분을 검은색 마스킹 처리"""
        logger.info('mask_first_certificate_number 시작')
        # ✅ 첫 번째 "제 ~~호" 찾으면 마스킹 후 즉시 반환
        for page in ocr_document.pages:
            for block in page.blocks:
                extracted_text = ""
                if block.layout.text_anchor.text_segments:
                    for segment in block.layout.text_anchor.text_segments:
                        start = int(segment.start_index) if segment.start_index else 0
                        end = int(segment.end_index) if segment.end_index else 0
                        extracted_text += ocr_document.text[start:end]

                # "제 ~~호" 패턴 찾기 (첫 번째만 처리)
                if "제" in extracted_text and "호" in extracted_text:
                    bounding_box = block.layout.bounding_poly.vertices
                    x_min = int(min([v.x for v in bounding_box])) - 10  # 좌우 여백 추가
                    y_min = int(min([v.y for v in bounding_box])) - 5   # 위 여백 추가
                    x_max = int(max([v.x for v in bounding_box])) + 10  # 좌우 여백 추가
                    y_max = int(max([v.y for v in bounding_box])) + 5   # 아래 여백 추가

                    # ✅ 검은색 마스킹 적용 (완전 블러)
                    cv2.rectangle(image, (x_min, y_min), (x_max, y_max), (0, 0, 0), -1)
                    print(f"✅ 첫 번째 '제 ~~호' 부분 검은색 블러 처리 완료: {extracted_text}")
                    return image  # ✅ 블러 처리된 이미지 반환

        return image  # ✅ 블러 적용 없이 원본 반환

    def upload_to_s3(self, image):
        s3_filename = f"{uuid.uuid4()}.png"
        """OpenCV 이미지(Numpy 배열)를 S3에 업로드 후 URL 반환"""
        _, image_bytes = cv2.imencode(".png", image)
        logger.info("s3 함수 실행")
        try:
            s3_client.put_object(
                Bucket=S3_BUCKET_NAME,
                Key=s3_filename,
                Body=image_bytes.tobytes(),
                ContentType="image/png",
                ACL="public-read"
            )
            s3_url = f"https://{S3_BUCKET_NAME}.s3.{S3_REGION}.amazonaws.com/{s3_filename}"
            logger.info(f"✅ S3 업로드 완료: {s3_url}")
            return s3_url
        except Exception as e:
            logger.info(f"❌ S3 업로드 실패: {e}")
            return None

    def scan_document(self, image_path):
        # 1. 이미지 로드 및 복사본 생성
        image = cv2.imread(image_path)
        if image is None:
            print("이미지를 불러올 수 없습니다:", image_path)
            return None
        orig = image.copy()

        # 2. 전처리: 그레이 스케일 변환, 가우시안 블러, Canny 엣지 검출
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        gray = cv2.GaussianBlur(gray, (5, 5), 0)
        edged = cv2.Canny(gray, 75, 200)

        # 필요에 따라 중간 결과 확인
        # cv2.imshow("Edged", edged)
        # cv2.waitKey(0)

        # 3. 컨투어 검출: 외곽선을 찾아 내림차순(면적 기준)으로 정렬
        contours, _ = cv2.findContours(edged.copy(), cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
        contours = sorted(contours, key=cv2.contourArea, reverse=True)[:5]

        # 4. 가장 큰 컨투어 중 꼭짓점이 4개인 컨투어를 찾아 문서 영역으로 간주
        docContour = None
        for c in contours:
            # 컨투어의 둘레 길이 계산
            peri = cv2.arcLength(c, True)
            # 컨투어 근사화: contour의 모양을 단순화
            approx = cv2.approxPolyDP(c, 0.02 * peri, True)

            if len(approx) == 4:
                docContour = approx
                break

        if docContour is None:
            print("문서 영역을 찾을 수 없습니다.")
            return None

        # 5. (선택 사항) 검출된 문서 영역을 원본 이미지에 그려 확인
        cv2.drawContours(image, [docContour], -1, (0, 255, 0), 2)
        # cv2.imshow("Document Contour", image)
        # cv2.waitKey(0)

        # 6. 검출한 4개 좌표를 이용해 perspective 변환하여 스캔된 이미지 생성
        scanned = self.four_point_transform(orig, docContour.reshape(4, 2))
        return scanned
