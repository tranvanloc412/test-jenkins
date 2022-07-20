variable "security_groups" {
  type        = any
  description = "List of security group with rules"
}

variable "vpc_id" {
  type        = string
  description = "VPC ID of the security groups"
}

variable "common_tags" {
  type        = map(any)
  description = "Common Tags Applied to Security Groups"
}
