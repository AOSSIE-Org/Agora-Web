package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel(description = "A message", value = "Response")
case class ResponseMessage(@ApiModelProperty(value = "Message", required = true)message: String)
