# Redis 기반 분산 예약 대기열 시스템


## A. 프로젝트 명
Redis 기반 예약 대기열 시스템: 티켓팅 시나리오


## B. 프로젝트 멤버 이름 및 멤버 별 담당 파트

- 편경찬
  - jmeter 부하 테스트 환경 구축
  - Spring filter/interceptor 이용한 접속자 쿠키 발급
- 송승우
  - AWS Fargate 기반 ECS 서비스 구성 및 배포
  - Terraform을 활용한 전체 인프라 자동화 구축
  - AWS 주요 서비스(VPC, Subnet, ALB, CloudWatch, IAM 등) 설정 및 연동
  - ECS 오토스케일링 및 보안 정책 구성
- 하창민
  - ㅇㅇ


## C. 프로젝트 소개
본 프로젝트는 Redis의 다양한 자료구조와 기능을 활용해 확장성 있는 예약 대기열 시스템을 구현한다.

티켓팅 시스템의 대규모 트래픽 처리 문제를 해결하기 위해 AWS EC2 로드 밸런싱, 오토스케일링, 그리고 Redis 기반 상태 공유 시스템을 결합한 사용자 경험 최적화 아키텍처를 구현한다.

수많은 사용자가 동시에 접속하는 환경에서 대기열 설계, 트래픽 분산, 세션 공유, 스마트 라우팅, 오토스케일링까지 하나의 통합된 아키텍처로 구성되었으며, Spring Boot 기반 WAS와 Redis, AWS EC2 인프라를 중심으로 운영된다.

주요 기능
- 실시간 방문자 추적 및 세션 관리
- 공정한 예약 대기열(FIFO) 운영
- 동적 시스템 설정 조정
- 분산 환경에서의 동시성 제어
- jmeter 이용한 부하 테스트
- load balancing 관련 내용 추가


## D. 프로젝트 필요성 소개

기존의 단일 인스턴스 기반 웹서비스 또는 세션 스티키 로드밸런싱 기반의 구조에서는 아래와 같은 문제가 발생한다:
- 세션을 유지하려면 같은 인스턴스를 계속 사용해야 하며, 이는 장애 발생 시 치명적
- 사용자 대기열이나 우선순위 처리를 인스턴스 로컬 메모리로 처리하면 스케일아웃 불가
- EC2 오토스케일링 시 새로운 인스턴스가 사용자 상태를 인식하지 못함

이를 해결하기 위해 Redis를 도입하여 인스턴스 간 상태 공유, 사용자 식별, 지능형 라우팅, 글로벌 대기열을 구현함으로써 다음과 같은 장점을 얻는다:
- 실시간 성능
- 공정한 대기열 
- 중복 요청 방지 
- 동적 설정 
- 분산 락


## E. 관련 기술/논문/특허 조사 내용 소개

- Redis Session Store
  - Spring Session + Redis 공식 문서
  - 다양한 인스턴스에서 동일한 세션 정보를 공유 가능

- Queueing Model for Ticketing Systems
  - IEEE: Designing Scalable Ticketing Systems for High-Concurrency Events
  - 사용자 상태 기반의 대기열 관리 방식 제안


## F. 프로젝트 개발 결과물 소개  (+다이어그램)

- 핵심 컴포넌트
    ```shell
    src/
    ├── config/                       # Redis/Spring 설정
    ├── controller/                   # API 엔드포인트
    ├── service/                      # 비즈니스 로직
    │   ├── BookingService.java       # 예약 처리
    │   └── RedisSettingsService.java # 설정 관리
    ├── filter/                       # 방문자 추적
    └── scheduler/                    # 대기열 처리
    ```


- 주요 기능
  1. 방문자 추적 및 관리 시스템
     - 모든 요청에 대해 필터링하여 방문자 쿠키 확인/생성
     - UUID로 생성된 고유 visitor_id를 쿠키와 세션에 저장
     - 방문자 정보를 Redis에 저장
  2. 예약 대기열 시스템
     - 3개의 주요 Redis 자료구조 사용:
       - booking:queue:list (List): 예약 요청 대기열 (FIFO)
       - booking:booked:set (Set): 예약 확정 사용자 집합
       - booking:queued:set (Set): 대기열 사용자 집합 (중복 방지)
     - 분산 락(booking:lock) 구현으로 동시성 제어 (5초 TTL)
     - maxBooking 설정값에 따라 예약 정원 관리 
  3. 동적 설정 관리 시스템
      - 애플리케이션 시작 시 기본 설정값 자동 생성:
        - maxBooking: 3
        - visitorTTLMinutes: 10
        - bookingLockTTLSeconds: 5
     - Redis Setting
       - 관리자 페이지(/system)에서 실시간으로 최대 예약자 수 변경 가능
  4. 트래픽 제어 시스템
     - "/" 및 "/ticketing" 경로에 대한 요청 인터셉트
     - 방문자 정보를 Redis에 저장 
  5. 관리자 대시보드
     - /system 경로로 설정값 조회/수정 기능 제공
     - RedisSettingsService와 연동하여 실시간 설정 관리
  6. Redis 사용 패턴 요약

     | Redis Key              | 용도                       | 자료구조              |
     | ---------------------- | ------------------------ | ----------------- |
     | `booking:settings`     | 시스템 전체 설정값 저장            | Hash              |
     | `booking:booked:set`   | 예약 확정된 사용자 목록            | Set               |
     | `booking:queued:set`   | 대기열에 등록된 사용자 목록          | Set               |
     | `booking:queue:list`   | 예약 요청 순서를 유지하기 위한 큐      | List              |
     | `booking:lock`         | 예약 처리에 대한 분산락            | String (with TTL) |

  7. 시스템 흐름
     1. 사용자 접속 → VisitorCountingFilter가 visitor_id 생성/확인
     2. 예약 요청(/apply) → BookingService가 대기열 등록
     3. 스케줄러가 최대 예약자 수 기반, 주기적 대기열 처리
     4. 관리자는 /system 페이지에서 실시간으로 시스템 설정 조정


- infra 구축
  - ㅇㅇ
  - ㅇㅇ


- 부하 테스트 시나리오
  - jmeter 셋팅
    - Threads : 5000
    - Ramp-Up Period : 600 
    - Loop Count : 200
    - Keep Alive : ON
   
![1](redisqueue/src/main/resources/static/images/1.png)

## G. 개발 결과물을 사용하는 방법 소개 (설치 방법, 동작 방법 등)


## H. 개발 결과물의 활용방안 소개

- 적용 가능 분야
  - 대규모 티켓팅 서비스
    - 인기 콘서트, 스포츠 경기 예매 시 수십만 명이 동시 접속해도 일관된 사용자 경험 보장
    - 사용자 순서 유지 + 재접속 지원 → 공정성과 유저 만족도 확보
  - 스마트 로드밸런싱 아키텍처
    - 유저 등급, 요청 유형, 응답 시간 기반 트래픽 분산 정책 설계에 활용
    - VIP 우선, 봇 차단, 대기 사용자 분산 등 정책 적용 가능
  - 간편한 오토스케일링 트리거 설계
    - Redis에 기록된 인스턴스 상태 데이터 기반으로 수동/자동 스케일링 구현 가능
    - Prometheus 등 외부 모니터링 시스템 없이도 자체 지표 수집 가능
  - 트래픽 통제 및 DDoS 완화
    - 사용자 요청 빈도와 TTL을 Redis로 관리하여 비정상적인 트래픽 감지 및 차단 가능

