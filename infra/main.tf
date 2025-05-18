provider "aws" {
  region = "ap-northeast-2"
}

resource "aws_security_group" "lb" {
  vpc_id = aws_vpc.this.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ecs_instance" {
  vpc_id = aws_vpc.this.id

  ingress {
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.lb.id]
  }

  ingress {
    description = "SSH access"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }


  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_ecs_cluster" "this" {
  name = "example-cluster"
}

resource "aws_ecs_task_definition" "this" {
  family                   = "example-task"
  requires_compatibilities = ["EC2"]
  network_mode            = "bridge"
  cpu                     = "256"
  memory                  = "512"
  execution_role_arn      = aws_iam_role.ecs_task_execution.arn

  container_definitions = jsonencode([
    {
      name      = "nginx",
      image     = "nginx:stable-alpine3.21-perl",
      essential = true,
      portMappings = [
        {
          containerPort = 80,
          hostPort      = 80,
          protocol      = "tcp"
        }
      ]
    }
  ])
}

# ALB
resource "aws_lb" "this" {
  name               = "example-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.lb.id]
  subnets            = aws_subnet.public[*].id
}

resource "aws_lb_target_group" "this" {
  name        = "example-tg"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = aws_vpc.this.id
  target_type = "instance"
}

resource "aws_lb_listener" "this" {
  load_balancer_arn = aws_lb.this.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.this.arn
  }
}

# ECS Service
resource "aws_ecs_service" "this" {
  name            = "example-service"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.this.arn
  desired_count   = 1
  launch_type     = "EC2"

  load_balancer {
    target_group_arn = aws_lb_target_group.this.arn
    container_name   = "nginx"
    container_port   = 80
  }

  depends_on = [aws_lb_listener.this]
}

# ECS Optimized AMI
data "aws_ami" "ecs_optimized" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-ecs-hvm-*-x86_64-ebs"]
  }
}

resource "aws_key_pair" "ecs" {
  key_name   = "ecs-key"
  public_key = file("~/.ssh/ecs-key.pub")
}

# Launch Template
resource "aws_launch_template" "ecs" {
  name_prefix   = "ecs-template-"
  image_id      = data.aws_ami.ecs_optimized.id
  instance_type = "t2.micro"

  key_name = aws_key_pair.ecs.key_name

  iam_instance_profile {
    name = aws_iam_instance_profile.ecs_profile.name
  }

  vpc_security_group_ids = [aws_security_group.ecs_instance.id]

  user_data = base64encode(<<-EOF
    #!/bin/bash
    echo "ECS_CLUSTER=${aws_ecs_cluster.this.name}" >> /etc/ecs/ecs.config
  EOF
  )

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "ECS AutoScaled Instance"
    }
  }
}

# Auto Scaling Group
resource "aws_autoscaling_group" "ecs" {
  name                      = "ecs-asg"
  max_size                  = 4
  min_size                  = 1
  desired_capacity          = 1
  vpc_zone_identifier       = aws_subnet.public[*].id
  health_check_type         = "EC2"

  launch_template {
    id      = aws_launch_template.ecs.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "ECS EC2 Instance"
    propagate_at_launch = true
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Auto Scaling Policies
resource "aws_autoscaling_policy" "scale_out" {
  name                   = "scale-out"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = aws_autoscaling_group.ecs.name
}

resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1 // 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Average"
  threshold           = 50
  alarm_description   = "Alarm when CPU exceeds 50%"
  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.ecs.name
  }
  alarm_actions = [aws_autoscaling_policy.scale_out.arn]
}

resource "aws_autoscaling_policy" "scale_in" {
  name                   = "scale-in"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 60
  autoscaling_group_name = aws_autoscaling_group.ecs.name
}

resource "aws_cloudwatch_metric_alarm" "cpu_low" {
  alarm_name          = "cpu-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1 // 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Average"
  threshold           = 30
  alarm_description   = "Alarm when CPU drops below 30%"
  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.ecs.name
  }
  alarm_actions = [aws_autoscaling_policy.scale_in.arn]
}
