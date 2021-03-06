package edu.agh.lroza.concurrent

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConcurrentServerScalaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = new ConcurrentServerScala();

  basicLogInLogOut(server)
  noticesManagement(server)
}