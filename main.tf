resource "aws_security_group" "this" {
  for_each    = var.security_groups
  name        = each.value.name
  description = each.value.desciption
  vpc_id      = var.vpc_id

  dynamic "ingress" {
    for_each = each.value.ingress
    content {
      from_port       = try(ingress.value.port, ingress.value.from_port)
      to_port         = try(ingress.value.port, ingress.value.to_port)
      protocol        = ingress.value.protocol
      cidr_blocks     = try(ingress.value.cidr_blocks, null)
      prefix_list_ids = try(ingress.value.prefix_list_ids, null)
      security_groups = try(ingress.value.security_groups, null)
      self            = try(ingress.value.self, null)
    }
  }

  dynamic "egress" {
    for_each = each.value.egress
    content {
      from_port       = try(ingress.value.port, ingress.value.from_port)
      to_port         = try(ingress.value.port, ingress.value.to_port)
      protocol        = ingress.value.protocol
      cidr_blocks     = try(ingress.value.cidr_blocks, null)
      prefix_list_ids = try(ingress.value.prefix_list_ids, null)
      security_groups = try(ingress.value.security_groups, null)
      self            = try(ingress.value.self, null)
    }
  }

  lifecycle {
    create_before_destroy = true
  }

  tags = merge(
    var.common_tags,
    {
      Name = each.value.name
    }
  )
}
