package util

import zio._

object Implicits {
  implicit class RichZIO[R, E <: Throwable, A](eff: ZIO[R, E, A]) {
    def catchErr = eff.sandbox.catchAll(Console.printError(_))

    def run(implicit runtime: Runtime[R]): A = Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(eff).getOrThrow
    }

    def succes(implicit runtime: Runtime[R]) = Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(eff).getOrThrowFiberFailure
    }

  }
}
