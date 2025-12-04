# 🚗 OCR 기반 효율적인 주차 관리 서비스 차곡차곡
<img width="1920" height="1080" alt="Image" src="https://github.com/user-attachments/assets/3e3f1e06-3732-44c8-8ea4-94eba1a03359" />

## 프로젝트 개요
* **기획 배경**

   - 일반적인 차단기와 차량 인식 장치 등을 통한 주차관리 시스템은 고비용(약 1,100만원)으로, 일부 소규모 주차장 및 간이 주차장을 이를 갖추기 어렵워 사람이 직접 차량을 기록하고 관리하는 비효율적인 방식을 사용하고 있다. 이러한 방식은 인력 낭비와 요금 계산 오류, 차량 위치 혼동 등 다양한 문제를 유발할 수 있다.
  
  - 기존의 수기 관리 및 단순 차단기 시스템의 비효율성을 해결하고, 관리자에게는 데이터 기반의 매출 분석을, 방문자에게는 실시간 주차 정보를 제공하기 위해 개발했다.

 * **제안 전략**
   
   - 차량 사진을 촬영하거나 갤러리에서 업로드 후 OCR을 활용하여 입차하는 과정으로, 스마트폰 하나로 이뤄지는 통합적인 주차 관리.
   - 주차장 이용자는 자신의 주차 정보를 손쉽게 조회.

* **팀원**

   - 정찬우(BE), 최재우(BE), 최용훈(FE), 이재유(FE), 장재혁(FE)
 

## 기술 스택 
### Frontend
* **Language**: Javascript, HTML5, CSS3

* **Library & Framework**: React

* **Deploy**: Google Cloud Flatform

### Backend

* **Language**: Java, Python

* **Library & Framework**: Spring Boot, Flask, EasyOCR
 
* **Database**: MySQL, Redis
  
* **Deploy**: Google Cloud Flatform

* **Collaboration**: Github, Discord, Notion

## 시스템 아키텍처
<img width="1247" height="747" alt="Image" src="https://github.com/user-attachments/assets/ce1c19c7-8e36-4c62-a2aa-b8146d7bbd6f" />

## ERD
<img width="1425" height="737" alt="Image" src="https://github.com/user-attachments/assets/123a934a-bbcd-4f5a-be26-a86dc3fb5934" />

## 주요 기능
### 관리자 

#### [회원가입/로그인]

* 계정 정보와 주차장 정보를 기입하고 전화번호 인증을 통한 회원가입, Session 방식의 로그인
* ID 찾기와 PW 재설정 기능

#### [차량 등록]

* 카메라/앨범을 통해 업로드 된 차량 이미지를 OCR을 통해 차량 번호 인식 후 사진에서 GPS 정보와 업로드 시간과 함께 리스트에 등록
* 차량 번호를 통한 수동 입차 가능

#### [입차리스트]

* 주차 현황 조회 (입차 수/주차 가능수, 각 차량 별 차량 번호, 요금, 주차 시간, 입차시간)
* 주차 차량 관리 (차량 번호 수정, 블랙리스트 등록, 출차)

#### [블랙리스트]

* 블랙리스트 차량 관리 (등록, 조회, 삭제)
  - 블랙리스트는 차량 번호와 사유를 입력하여 등록
  - 블랙리스트 차량 OCR 시 등록하기 전 경고

#### [정기권 관리]

-
#### [주차 이력 조회]

#### [매출·통계]

### 방문자

#### [주차 정보 조회]

#### [주변 주차장·번호판 미 인식 차량 조회]

#### [입차 요청]
