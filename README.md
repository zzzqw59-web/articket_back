# 🎨 Articket (아티켓)

> **전시 정보 탐색부터 예약·결제, 관람 후 리뷰와 문의까지 이어지는 통합 전시 플랫폼**

Articket은 전국의 다양한 전시 정보를 탐색하고 전시 상세 정보를 확인한 뒤, 유료 전시의 티켓을 예약·결제할 수 있는 웹 서비스입니다. 회원별 이용 내역, 리뷰·문의, 전시 관계자 및 관리자 기능을 연결하여 **전시 탐색 → 예약·결제 → 관람 → 후기 공유**로 이어지는 경험을 제공하는 5인 팀 프로젝트입니다.

## 📌 목차

- [프로젝트 소개](#project-intro)
- [주요 기능](#features)
- [기술 스택](#tech-stack)
- [시스템 아키텍처](#architecture)
- [주요 서비스 흐름](#service-flows)
- [주요 설계 및 구현 내용](#design)
- [팀 구성 및 역할](#team)
- [프로젝트 구조](#project-structure)
- [로컬 실행 방법](#local-setup)
- [시연 데이터 및 테스트](#demo-tests)
- [프로젝트 저장소](#repositories)

<a id="project-intro"></a>

## 📖 프로젝트 소개

| 항목 | 내용 |
|---|---|
| 프로젝트명 | Articket (아티켓) |
| 프로젝트 유형 | 5인 팀 프로젝트 |
| 개발 기간 | 2026.09.10 ~ 2026.10.19 |
| 서비스 분야 | 전국 전시 정보 탐색 및 티켓 예약·결제 |
| Frontend | React, Vite |
| Backend | Java 17, Spring Boot |
| 로컬 데이터베이스 | Oracle Database 21c XE |
| 인증 및 권한 | Spring Security, JWT |
| 결제 서비스 | Toss Payments |
| 클라우드 배포 | AWS 인프라 구축 및 배포 진행 중 |

### 기획 배경 및 목표

전시 정보가 여러 채널에 분산되어 있어 원하는 전시를 찾아 비교하기 번거롭고, 정보 확인 이후 예약·결제와 후기 작성까지 이용 과정이 나뉘는 문제가 있습니다.

Articket은 전시 정보를 통합적으로 탐색하고, **전시 확인 → 예약 → 결제 → 관람 → 리뷰 작성**을 하나의 서비스에서 이용할 수 있도록 설계했습니다. 프론트엔드와 백엔드를 분리하고 REST API로 연결했으며, 예약·결제처럼 데이터 일관성이 중요한 기능은 서버에서 검증하고 처리합니다.

<a id="features"></a>

## ✨ 주요 기능

### 1. 회원 및 인증

- 이메일 중복 확인 및 **SOLAPI SMS 본인인증**을 통한 회원가입
- **Spring Security와 JWT** 기반 로그인·로그아웃
- **Access Token / Refresh Token**을 이용한 인증 및 토큰 재발급
- 일반 회원(`MEMBER`), 전시 관계자(`STAFF`), 관리자(`ADMIN`) 권한별 접근 제어
- **BCrypt** 비밀번호 해싱 및 **Google Tink** 기반 이름·전화번호 암호화
- 비밀번호 확인과 SMS 인증을 통한 회원정보 수정
- SMS 인증을 통한 비밀번호 재설정
- 회원 탈퇴 신청·철회 및 탈퇴 완료 시 개인정보 익명화
- 회원 제재 및 수동·자동 해제, 제재 상태에 따른 서비스 접근 제한
- 전시 관계자 담당 권한 신청 및 관리자의 승인·반려

### 2. 전시 및 전시장

- 문화 관련 공공데이터 API를 활용한 전국 전시 목록·상세 정보 제공
- 전시명·전시장명 등 조건에 따른 검색 및 필터링
- 전시 이미지, 관람 기간, 전시장 위치·연락처, 관람료 조회
- 전시장 위치 등 지도 관련 외부 서비스 연계
- 권한에 따른 전시 정보 관리 기능 제공
- 무료 전시는 정보 조회, 예약 가능한 유료 전시는 예약 화면으로 연결

### 3. 예약

- 유료 전시의 관람 날짜 및 예약 인원 선택
- 서버에서 티켓 가격과 인원을 기준으로 예약 금액 산정
- 예약 생성 시 고유 주문번호(`orderId`) 발급
- 예약 상태 관리: `PENDING` → `RESERVED` / `CANCELED`
- 본인 예약 목록·상세 조회 및 예약 취소
- 미결제 예약 정보 갱신 처리, 결제 완료 예약은 취소 후 재예약
- 예약 취소 사유 및 취소 일시 관리

### 4. 결제 및 환불

- **Toss Payments** 연동을 통한 결제 요청·승인
- `paymentKey`, `orderId`, 결제 금액 검증
- 서버에 저장된 예약 금액과 결제 요청·승인 금액 비교
- 결제 성공·실패 화면 및 결제 내역 제공
- 결제 상태 관리: `DONE` / `CANCELED`
- 예약 취소와 연계한 결제 취소 및 환불 금액 기록
- 취소 시점과 관람일에 따른 환불 기준 적용

### 5. 모바일 QR 티켓

- 결제 완료 예약 정보를 이용한 QR 코드 생성
- QR 코드 스캔 시 예약 확인 페이지 연결
- 예약 상세 화면에서 티켓 정보 조회

### 6. 리뷰 및 댓글

- 리뷰 등록·조회·수정·삭제, 검색·정렬·페이징
- 예약 상태와 관람일을 확인한 리뷰 작성 자격 검증
- 동일 회원의 동일 전시 중복 리뷰 작성 제한
- 리뷰 이미지 최대 **3장**, 파일당 **5MB 이하** 첨부
- JPG·JPEG·PNG·WEBP 이미지 지원
- 리뷰 조회수 관리 및 리뷰 댓글 기능
- 작성자·관리자 권한에 따른 수정·삭제

### 7. 문의 및 알림

- 문의 등록·조회·수정·삭제와 답변 댓글
- 공개 문의 및 비밀 문의의 권한별 열람 제어
- 담당 전시 관계자 및 관리자의 문의 답변 기능
- 댓글 등록, 게시물 관리, 회원 상태 변경 등 주요 이벤트 알림
- 회원별 알림 조회 및 삭제

### 8. 마이페이지

- 회원정보 조회·수정 및 탈퇴 신청·철회
- 위시리스트 및 본인 작성 게시물·댓글 내역 관리
- 예약·취소, 결제·환불 및 관람 내역 조회
- 관람 완료 전시의 리뷰 작성 기능 연계

### 9. 전시 관계자 및 관리자

- 전시 관계자 담당 권한 신청 및 관리자 승인·반려
- 전시 관계자의 담당 전시·리뷰·문의 정보 조회 및 관리
- 관리자 회원 권한·제재·탈퇴 및 게시물 관리
- 예약·취소·수익 현황 등 관리자·전시 관계자 통계 제공

<a id="tech-stack"></a>

## 🛠️ 기술 스택

### Frontend

| 기술 | 활용 |
|---|---|
| JavaScript, React | 컴포넌트 기반 UI 및 화면 로직 |
| Vite | 개발 서버 및 빌드 |
| React Router | 페이지 라우팅 |
| Axios | 백엔드 REST API 통신 |
| Tailwind CSS, Ant Design | 스타일 및 UI 컴포넌트 |
| GSAP | 화면 애니메이션 |
| Toss Payments SDK | 클라이언트 결제 요청 |
| qrcode.react | QR 코드 생성 |

### Backend

| 기술 | 활용 |
|---|---|
| Java 17, Spring Boot | 서버 및 비즈니스 로직 |
| Spring Web MVC | REST API 처리 |
| Spring Security, JWT | 인증·인가 및 토큰 기반 접근 제어 |
| Access Token / Refresh Token | 인증 유지 및 토큰 재발급 |
| Spring Data JPA, Hibernate | 데이터베이스 연동 |
| QueryDSL | 동적 검색 쿼리 |
| Lombok, MapStruct | 보일러플레이트 감소 및 객체 매핑 |
| Bean Validation | 요청값 검증 |
| WebClient | 외부 API 및 결제 통신 |
| Oracle JDBC | Oracle 데이터베이스 연결 |
| Springdoc OpenAPI (Swagger) | API 문서화 |
| BCrypt | 비밀번호 해싱 |
| Google Tink | 이름·전화번호 등 개인정보 암호화 |
| Gradle, JUnit 5 | 빌드 및 테스트 |

### 데이터베이스 및 외부 서비스

| 구분 | 기술 및 서비스 | 활용 |
|---|---|---|
| 로컬 DB | Oracle Database 21c XE | 개발 데이터 저장 |
| 운영 DB | Amazon RDS for Oracle | AWS 서비스 데이터 저장 |
| 결제 | Toss Payments | 결제 승인·취소·환불 연동 |
| 전시 정보 | 문화 관련 공공데이터 API | 전시 정보 수집·조회 |
| 지도 | Kakao API | 지도 관련 서비스 |
| 문자 발송 | SOLAPI | 회원가입·정보 변경 등의 SMS 인증 |

<a id="architecture"></a>

## 🏗️ 시스템 아키텍처

### 로컬 개발 환경

```text
[브라우저]
    |
    v
[React + Vite]
    | REST API
    v
[Spring Boot + Spring Security/JWT]
    | Controller → Service → Repository/JPA
    +--------------------------+
    |                          |
    v                          v
[Oracle 21c XE]         [외부 API / Toss Payments / SOLAPI]
```

### AWS 배포 환경

AWS에서는 프론트엔드, 백엔드, 데이터베이스를 분리하여 운영하는 구성을 적용합니다.

```text
               [사용자 브라우저]
                      |
                [Route 53 DNS]
                      |
          +-----------+-----------+
          |                       |
          v                       v
    [CloudFront]             [API 도메인]
          |                       |
          v                       v
     [Amazon S3]             [EC2 / Nginx]
    React 빌드 파일                |
                                  v
                           [Spring Boot]
                           JWT 인증 / API
                                  |
                                  v
                         [RDS for Oracle]
                                  |
                        (서버에서 외부 서비스 호출)
```

- **S3:** React 빌드 결과물 저장
- **CloudFront:** 프론트엔드 정적 콘텐츠 배포 및 HTTPS 제공
- **Route 53:** 도메인·DNS 관리
- **EC2:** Spring Boot 애플리케이션 구동
- **Nginx:** 리버스 프록시 및 백엔드 요청 전달
- **RDS for Oracle:** 회원·전시·예약·결제·게시물 데이터 저장

> **배포 현황:** AWS 인프라 구축 및 배포 진행 중. 서비스 주소와 최종 HTTPS·API 라우팅 구성은 배포 완료 후 갱신합니다.
>
> 위 그림은 목표 구성입니다. 도메인별 DNS 레코드, CloudFront 오리진·API 라우팅, 인증 및 네트워크 설정은 실제 배포 결과를 기준으로 확정합니다.

<a id="service-flows"></a>

## 🔄 주요 서비스 흐름

### 회원 가입 및 인증

```text
이메일 중복 확인
        ↓
SOLAPI SMS 인증
        ↓
회원정보 입력 및 서버 검증
        ↓
비밀번호 BCrypt 해싱 / 개인정보 암호화
        ↓
회원가입 완료
        ↓
로그인 → Access Token / Refresh Token 발급
        ↓
Spring Security + JWT 기반 요청 인증·권한 검증
```

### 예약 및 결제

```text
전시 상세 정보 확인
        ↓
관람 날짜 및 인원 선택
        ↓
서버 예약 생성·금액 산정·orderId 발급
        ↓
예약 상태 PENDING
        ↓
Toss Payments 결제 요청
        ↓
백엔드 결제 승인 요청 및 결과 검증
        ↓
결제 DONE / 예약 RESERVED
        ↓
결제 완료 화면 및 QR 티켓 제공
```

### 예약 취소 및 환불

```text
본인 예약 조회
        ↓
예약·결제 상태 및 취소 조건 확인
        ↓
환불 금액 계산
        ↓
Toss Payments 결제 취소 요청
        ↓
결제 CANCELED / 예약 CANCELED
        ↓
취소 사유·일시 및 환불 금액 기록
```

### 리뷰 작성

```text
로그인 사용자
        ↓
예약 상태 RESERVED + 관람일 경과 확인
        ↓
동일 전시 중복 리뷰 여부 검증
        ↓
리뷰 제목·본문·이미지 제출
        ↓
서버 유효성 검증 및 파일 저장
        ↓
리뷰 등록 완료
```

<a id="design"></a>

## 💡 주요 설계 및 구현 내용

### 인증 및 개인정보 보호

- Spring Security와 JWT를 이용해 사용자 인증 및 권한별 API 접근을 제어합니다.
- Access Token과 Refresh Token을 구분하고, 토큰 재발급 기능을 제공합니다.
- 서버의 로그인 세션 검증 로직을 통해 로그아웃 및 비밀번호 변경 시 기존 토큰을 무효화하도록 구성했습니다.
- 회원가입, 정보 수정, 비밀번호 재설정 과정에 SMS 인증을 적용했습니다.
- 비밀번호는 BCrypt로 해싱하고, 이름·전화번호는 Google Tink 기반 암호화 로직으로 보호합니다.
- 제재·탈퇴 상태에 따라 접근을 제한하고, 탈퇴 완료 시 개인정보 익명화 처리를 수행합니다.

### 예약·결제 데이터 정합성

- 예약 생성과 결제 승인을 서로 다른 단계로 관리합니다.
- 예약에 고유한 주문번호와 서버 산정 금액을 저장합니다.
- 결제 승인 전 `paymentKey`, `orderId`, 결제 금액과 예약 상태를 검증합니다.
- 예약과 결제의 상태를 각각 관리하고, 취소·환불 내역을 기록합니다.

### 리뷰 작성 자격 및 이미지 관리

- 리뷰 작성자는 결제 완료 예약과 관람일 조건을 충족해야 합니다.
- 동일 회원의 동일 전시 중복 리뷰를 방지합니다.
- 첨부 이미지의 개수·용량·형식을 검증하며 파일과 DB 저장 정보를 연결합니다.

### 사용자별 권한 및 데이터 접근

- 일반 회원과 전시 관계자, 관리자의 기능 접근을 구분합니다.
- 예약·결제와 비밀 문의 등 회원별 데이터는 접근 권한을 검증합니다.
- 전시 관계자는 관리자에게 담당 전시 권한을 신청하고 승인받도록 구성했습니다.

<a id="team"></a>

## 👥 팀 구성 및 역할

| 팀원 | 담당 기능 |
|---|---|
| 김성민 (팀장) | 예약·결제·환불·리뷰·리뷰 댓글 |
| 김태현 | 회원가입·JWT 인증·회원 및 권한 관리 |
| 육현승 | 전시·전시장 정보 및 외부 데이터 연동 |
| 최서현 | 아티켓 소개·전시 관계자·관리자 페이지 |
| 한진형 | 문의·문의 댓글·알림·마이페이지 |

<a id="project-structure"></a>

## 📂 프로젝트 구조

프론트엔드와 백엔드를 각각 별도의 GitHub 저장소로 관리합니다.

```text
articket_front/
├── public/
├── src/
│   ├── api/           # 백엔드 API 요청
│   ├── components/    # 공통 UI
│   ├── pages/         # 서비스 화면
│   └── ...
├── package.json
└── vite.config.js

articket_back/
├── src/
│   ├── main/
│   │   ├── java/com/project/articket/
│   │   │   ├── member/
│   │   │   ├── exhibition/
│   │   │   ├── reservation/
│   │   │   ├── payment/
│   │   │   ├── review/
│   │   │   └── ...
│   │   └── resources/
│   └── test/java/com/project/articket/demo/
├── build.gradle
└── gradlew.bat
```

*위 내용은 주요 디렉터리만 표시한 간략한 구조입니다.*

<a id="local-setup"></a>

## 🚀 로컬 실행 방법

### 사전 준비

- Java 17
- Node.js 및 npm (프로젝트 Vite 버전과 호환되는 환경)
- Oracle Database 및 개발용 스키마
- 필요한 API 키와 애플리케이션 환경변수

### 1. Backend

```bash
git clone https://github.com/zzzqw59-web/articket_back.git
cd articket_back
```

데이터베이스 연결 및 외부 서비스 인증 정보를 로컬 실행 환경에 설정한 뒤 실행합니다.

**Windows**

```bat
gradlew.bat bootRun
```

**macOS / Linux**

```bash
./gradlew bootRun
```

설정 시 확인할 항목:

- Oracle DB 접속 주소·계정 및 비밀번호
- JWT 서명용 비밀키 (`jwt.secret-key` 설정에 대응하는 값)
- Toss Payments 서버 비밀키
- SOLAPI 및 공공데이터·Kakao API 인증 정보
- 프론트엔드 주소 및 CORS 허용 설정
- 이미지 업로드 경로

> 환경변수 또는 로컬 설정 파일에서 실제 비밀키를 관리하고 Git에 커밋하지 않습니다. `.env`를 테스트에서 읽는 설정이 있더라도 `bootRun`에 같은 환경변수가 자동 적용되는지는 별도로 확인해야 합니다.

### 2. Frontend

```bash
git clone https://github.com/zzzqw59-web/articket_front.git
cd articket_front
npm install
npm run dev
```

빌드 및 코드 검사:

```bash
npm run build
npm run lint
```

백엔드 API 주소, Toss Payments 클라이언트 키, 인증 및 CORS 설정을 실제 실행 환경에 맞게 확인합니다.

<a id="demo-tests"></a>

## 🧪 시연 데이터 및 테스트

팀원별 로컬 환경에서 동일한 기능과 관리자 통계를 확인할 수 있도록 JUnit 기반 시연 데이터 테스트를 구성했습니다.

| 데이터 | 구성 |
|---|---:|
| 시연용 일반 회원 | 80명 |
| 예약·결제 | 각 500건 |
| 예약·결제 완료 | 400건 |
| 예약·결제 취소 | 100건 |
| 리뷰 | 50건 |
| 리뷰 이미지 | 39장 |
| 리뷰 댓글 | 35건 |

주요 테스트 코드:

- `DemoAllMemberSetupTest` — 회원 및 회원 상태별 시나리오 데이터
- `DemoReservationDeleteTest` / `DemoReservationCreateTest` — 예약·결제 초기화 및 생성
- `DemoReviewDeleteTest` / `DemoReviewCreateTest` — 리뷰·이미지·댓글 초기화 및 생성
- `DemoReviewPhotoSupport` — 리뷰 시연용 이미지 생성 지원

> 테스트에서 생성하는 회원·결제·리뷰는 **개발·발표용 가상 데이터**이며 실제 거래가 아닙니다. 생성·삭제 테스트는 실제 로컬 DB를 변경하므로 필요한 메서드만 개별 실행해야 합니다. AWS 운영 환경에서는 실행하지 않습니다.

<a id="repositories"></a>

## 🔗 프로젝트 저장소

- **Frontend:** [zzzqw59-web/articket_front](https://github.com/zzzqw59-web/articket_front)
- **Backend:** [zzzqw59-web/articket_back](https://github.com/zzzqw59-web/articket_back)

---

**Articket — 전시를 찾는 순간부터 관람의 기록까지.**
