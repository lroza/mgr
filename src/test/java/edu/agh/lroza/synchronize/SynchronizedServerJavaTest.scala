package edu.agh.lroza.synchronize

import org.scalatest.FunSuite
import edu.agh.lroza.common.FunSuiteServerBeahaviors
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import edu.agh.lroza.NoticeBoardServerJavaWrapper

@RunWith(classOf[JUnitRunner])
class SynchronizedServerJavaTest extends FunSuite with FunSuiteServerBeahaviors {
  def server = new NoticeBoardServerJavaWrapper(new SynchronizedServerJava())

  basicLogInLogOut(server)
  noticesManagement(server)
}