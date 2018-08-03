package controllers

import jp.t2v.lab.play2.auth.test.Helpers._
import org.scalatest._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test._
import scalikejdbc.PlayModule
import services.{MockUserService, UserService}

class HomeControllerSpec extends FunSpec
  with MustMatchers
  with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[PlayModule]
      .overrides(bind[UserService].to[MockUserService]) // 追加
      .build()

  object Config extends AuthConfigSupport {

    override val userService: UserService = app.injector.instanceOf[UserService]

  }


  describe("HomeController") {
    describe("route of HomeController#index") {
      it("should be valid") {
        val result = route(app, FakeRequest(GET, routes.HomeController.index().toString)).get
        status(result) mustBe OK
      }
      it("should be valid when logged in") { // ログイン時のテスト
        val email = "test@test.com"
        val result =
          route(
            app,
            FakeRequest(GET, routes.HomeController.index().toString)
              .withLoggedIn(Config)(email)
          ).get
        status(result) mustBe OK
        contentAsString(result) must include(email)
      }
    }
  }
}