#cmd에서 cd C:\Users\jcw00\IdeaProjects\mote\src\main\resources\python 후에 python ocr_plate_reader.py 로 서버실행

from flask import Flask, request, jsonify
import easyocr
import re
import sys
import os
import warnings
from PIL import Image
import io
import cv2

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
warnings.filterwarnings("ignore", category=UserWarning)

app = Flask(__name__)
reader = easyocr.Reader(['ko', 'en'], gpu=False)


def resize_image(image_path, max_width=800):
    image = cv2.imread(image_path)
    if image is None:
        return image_path
    h, w = image.shape[:2]
    if w > max_width:
        scale = max_width / w
        resized = cv2.resize(image, (int(w * scale), int(h * scale)))
        temp_path = image_path.replace(".jpg", "_resized.jpg").replace(".png", "_resized.png")
        cv2.imwrite(temp_path, resized)
        return temp_path
    return image_path


def enhance_contrast(image_path):
    img = cv2.imread(image_path, cv2.IMREAD_COLOR)
    if img is None:
        return image_path
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    equalized = cv2.equalizeHist(gray)
    temp_path = image_path.replace(".jpg", "_contrast.jpg").replace(".png", "_contrast.png")
    cv2.imwrite(temp_path, equalized)
    return temp_path


def extract_plate_from_image(image_stream):
    try:
        image_bytes = image_stream.read()
        image = Image.open(io.BytesIO(image_bytes))
        image_path = "temp_image.jpg"
        image.save(image_path)

        # ▶ 전처리: 리사이징 → 대비 강화
        resized_path = resize_image(image_path)
        final_path = enhance_contrast(resized_path)

        # ▶ OCR 수행
        results = reader.readtext(final_path)
        pattern = re.compile(r'\d{2,3}[가-힣]\d{4}')

        for _, text, confidence in results:
            cleaned = re.sub(r'\s+', '', text)
            match = pattern.search(cleaned)
            if match:
                return {
                    "plate": match.group(),
                    "confidence": confidence,
                    "raw": cleaned
                }

        return {"plate": "NOT_FOUND"}

    except Exception as e:
        return {"plate": f"ERROR: {str(e)}"}

    finally:
        # 임시 파일 정리
        for f in ["temp_image.jpg", "temp_image_resized.jpg", "temp_image_resized_contrast.jpg",
                  "temp_image_contrast.jpg"]:
            if os.path.exists(f):
                os.remove(f)


@app.route('/ocr', methods=['POST'])
def ocr():
    if 'file' not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file']
    result = extract_plate_from_image(file.stream)
    return jsonify(result)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)