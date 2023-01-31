package test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest._
import flatspec._
import matchers._

trait BaseSpec extends AnyFlatSpec with should.Matchers {}
