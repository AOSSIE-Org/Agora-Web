package models.security

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import models.Question

@ApiModel(description = "SignUp object")
case class SignUp(
  @ApiModelProperty(value = "user unique identifier", required = true, example = "james.bond") identifier: String,
  @ApiModelProperty(required = true, example = "this!Password!Is!Very!Very!Strong!") password: String,
  @ApiModelProperty(value = "e-mail address unique", required = true, example = "james.bond@test.com") email: String,
  @ApiModelProperty(value = "user first name", required = true, example = "James") firstName: String,
  @ApiModelProperty(value = "user last name", required = true, example = "Bond") lastName: String,
  @ApiModelProperty(value = "user security question") securityQuestion: Question)