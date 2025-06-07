# resource "aws_vpc" "this" {
#   cidr_block = "10.0.0.0/16"
# }
#
# # 퍼블릭 서브넷 생성
# resource "aws_subnet" "this" {
#   vpc_id                  = aws_vpc.this.id
#   cidr_block              = "10.0.1.0/24"
#   map_public_ip_on_launch = true
#   availability_zone       = "ap-northeast-2a"
#
#   tags = {
#     Name = "term-public-subnet"
#   }
# }
#
# # 인터넷 게이트웨이
# resource "aws_internet_gateway" "this" {
#   vpc_id = aws_vpc.this.id
#   tags = {
#     Name = "term-igw"
#   }
# }
#
# # 라우팅 테이블 (0.0.0.0/0 → IGW)
# resource "aws_route_table" "public" {
#   vpc_id = aws_vpc.this.id
#
#   route {
#     cidr_block = "0.0.0.0/0"
#     gateway_id = aws_internet_gateway.this.id
#   }
#
#   tags = {
#     Name = "term-public-rt"
#   }
# }
#
# # 라우팅 테이블과 서브넷 연결
# resource "aws_route_table_association" "public" {
#   subnet_id      = aws_subnet.this.id
#   route_table_id = aws_route_table.public.id
# }
#
# # 보안 그룹 (Redis, SSH, HTTP)
# resource "aws_security_group" "redis_sg" {
#   name        = "redis-security-group"
#   description = "Allow Redis, SSH, and HTTP access"
#   vpc_id      = aws_vpc.this.id
#
#   ingress {
#     description = "Redis access"
#     from_port   = 6379
#     to_port     = 6379
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }
#
#   ingress {
#     description = "SSH access"
#     from_port   = 22
#     to_port     = 22
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }
#
#   ingress {
#     description = "HTTP access"
#     from_port   = 80
#     to_port     = 80
#     protocol    = "tcp"
#     cidr_blocks = ["0.0.0.0/0"]
#   }
#
#   egress {
#     description = "Allow all outbound"
#     from_port   = 0
#     to_port     = 0
#     protocol    = "-1"
#     cidr_blocks = ["0.0.0.0/0"]
#   }
#
#   tags = {
#     Name = "redis-sg"
#   }
# }
#
# # EC2 인스턴스 (퍼블릭 IP 포함, 콘솔 연결 가능)
# resource "aws_instance" "this" {
#   ami                         = "ami-0d5bb3742db8fc264"
#   instance_type               = "t3.micro"
#   subnet_id                   = aws_subnet.this.id
#   associate_public_ip_address = true
#   vpc_security_group_ids      = [aws_security_group.redis_sg.id]
#
#   tags = {
#     Name = "redis-server"
#   }
# }
