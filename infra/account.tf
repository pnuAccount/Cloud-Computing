# # IAM User 생성
# resource "aws_iam_user" "user" {
#   name = "cloud-computing-termproject-user"
# }
#
# # 정책 연결
# resource "aws_iam_user_policy_attachment" "admin_policy_attach" {
#   user       = aws_iam_user.user.name
#   policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
# }
