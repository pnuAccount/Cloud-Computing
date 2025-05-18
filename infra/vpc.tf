/*
VPC 생성
내부 네트워크 주소 범위를 10.0.0.0 ~ 10.0.255.255 로 설정
(사용할 수 있는 사설 IP 범위 지정)
*/
resource "aws_vpc" "this" {
  cidr_block = "10.0.0.0/16"
}

/*
사용 가능한 가용 영역(AZ) 목록 가져오기
서울 리전(ap-norteast-2)의 ap-northeast-2a, 2b 같은 AZ 이름을 자동으로 가져옴
*/
data "aws_availability_zones" "available" {}

/*
퍼블릭 서브넷 2개 생성
각각 다른 AZ에 생성
퍼블릭 ip 자동 할당
cidrsubnet으로 10.0.0.0/16을 2개의 서브넷으로 나눔
*/
resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.this.id
  cidr_block              = cidrsubnet(aws_vpc.this.cidr_block, 8, count.index)
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[count.index]
}

/*
인터넷 게이트웨이 (IGW) 생성
VPC에 부착하여 인터넷 연결 가능하게 함
*/
resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
}

/*
라우팅 테이블 생성
0.0.0.0/0 (전 세계 IP) -> 인터넷 게이트웨이로 라우팅
퍼블릭 서브넷의 인스턴스가 인터넷과 통신 가능하게 함
*/
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }
}

/*
서브넷에 라우팅 테이블 연결
*/
resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}
