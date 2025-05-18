/*
EC2 인스턴스가 필요한 권한을 부여받음
*/
resource "aws_iam_role" "ecs_instance_role" {
  name = "ecs-instance-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })
}

/*
위의 역할에 ECS 인스턴스 전용 정책을 붙임
EC2 인스턴스가 ECS 클러스터에 등록되고 에이전트를 실행할 수 있도록 해줌
*/
resource "aws_iam_role_policy_attachment" "ecs_instance_policy" {
  role       = aws_iam_role.ecs_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

/*
IAM 역할을 EC2 인스턴스에 연결
*/
resource "aws_iam_instance_profile" "ecs_profile" {
  name = "ecs-instance-profile"
  role = aws_iam_role.ecs_instance_role.name
}

/*
ECS Task가 사용할 IAM 역할
컨테이너가 CloudWatch Log 등의 AWS 리소스에 접근할 수 있도록 해줌
*/
resource "aws_iam_role" "ecs_task_execution" {
  name = "ecsTaskExecutionRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Effect    = "Allow",
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_policy" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}
