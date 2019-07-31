package com.politrons.server

import scala.scalanative.native._
import scala.util.Try

/**
  * We create a Scala native image using sbt template [scala-native.g8] (sbt new scala-native/scala-native.g8)
  *
  * Using [sbt run] with this scala Native code, what it happens is
  *
  * Compiling to native code:  NIR (Native Intermediate Representation)
  * Linking native code (immix gc) LLVM compiled it into assembly language
  * native code, and linked it into an executable binary.
  *
  * Finally the binary is executed.
  *
  * As you can see in this example Scala-native is perfectly fine to execute regular Scala code as you would do in "Just-in-time" mode.
  */
object Features extends App {

  runC_Code()
  regularOutput()
  collections()
  forComprehension()

  /**
    * Using [scalanative] we're able to execute C code in Scala.
    * Here we show how we can use the C standard libs that you can find here.
    * https://www.tutorialspoint.com/c_standard_library/index.htm
    *
    */
  def runC_Code(): Unit = {
    castingFromScalaToC_Types
    cStringAPI
    cTypeAPI
    cStdLibAPI
//    cStructs
//    cFunctionPointers
  }

  /**
    * Zones (also known as memory regions/contexts) are a technique for semi-automatic memory management.
    * Just like in go with the [defer clean] operator we will clean all memory in the function, once
    * the execution finish.
    *
    * In this example we use some of the C [String.h] Library using [string] factory.
    * We aso have to make the conversion from Scala types to C types to interoperate, we can use for string [toCString]
    */
  private def cStringAPI = Zone { implicit z =>

    val cString: Ptr[CChar] = toCString("From Scala string to C and back again")
    println(fromCString(cString))

    val concatCString: CString = string.strcat(string.strcat(toCString("hello"), toCString(" C")), toCString(" world"))
    println(fromCString(concatCString))

    val copyString: CString = string.strcpy(toCString(""), toCString("Copy this text from one pointer to another please"))
    println(fromCString(copyString))

  }

  /**
    * Here we use the [ctype.h] that provide the API to get information of characters in Ascii.
    * We try with operators:
    * * [isalnum] to check if character are alphanumeric
    * * [isDigit] to check if character is digit
    * * [isupper] to check if character is upper case
    */
  private def cTypeAPI = Zone { implicit z =>
    var isAlphanumeric = ctype.isalnum(63.cast[CInt]) //63 ascii ?
    println("? Is isAlphanumeric:" + isAlphanumeric.cast[Int]) //Should be 0 false
    isAlphanumeric = ctype.isalnum(97.cast[CInt]) //97 ascii "a"
    println("a Is isAlphanumeric:" + isAlphanumeric.cast[Int]) //Should be 1 true

    var isDigit = ctype.isdigit(97.cast[CInt]) //Ascii "a"
    println("a Is digit:" + isDigit.cast[Int]) //Should be 0 false
    isDigit = ctype.isdigit(49.cast[CInt]) //Ascii 1
    println("1 Is digit:" + isDigit.cast[Int]) //Should be 1 true

    var isUpper = ctype.isupper(97.cast[CInt]) //Ascii "a"
    println("a Is upper character:" + isUpper.cast[Int]) //Should be 0 false
    isUpper = ctype.isupper(65.cast[CInt]) //Ascii "A"
    println("A Is upper character:" + isUpper.cast[Int]) //Should be 1 true
  }

  /**
    * Example of use of [stdlib.h] using in this example the [rand] function of C
    * Also to use [printf] of C we can use [stdio]
    */
  private def cStdLibAPI = Zone { implicit z =>
    val rand1 = stdlib.rand()
    val rand2 = stdlib.rand()
    val rand3 = stdlib.rand().cast[Int]

    stdio.printf(toCString("Random value1 %d \n"), rand1)
    stdio.printf(toCString("Random value2 %d \n"), rand2)
    println("Random value3 " + rand3)
  }

//  private def cFunctionPointers(): Unit = Zone { implicit z =>
//    val value = CFunctionPtr.fromFunction0(() => "Hello function pointer")
//    println(value.apply())
//  }

//  private def cStructs: Unit = Zone { implicit z =>
//    type Vec3 = CStruct3[Double, Double, Double]
//    val vec = stackalloc[Vec3] // allocate c struct on stack
//    !vec._1 = 10.0 // initialize fields
//    !vec._2 = 20.0
//    !vec._3 = 30.0
//    //    println(vec._1.cast[Double])
//  }


  /**
    * Here we show how using [cast] implicit extension we can cast all scala types into C types.
    * This is something mandatory and pretty common when you have to move from one realm to another.
    */
  private def castingFromScalaToC_Types = {
    val scalaInt: Int = 10
    val cInt: CInt = scalaInt.cast[CInt]
    println(s"C int $cInt")

    val scalaDouble: Double = 10d
    val cDouble: CDouble = scalaDouble.cast[CDouble]
    println(s"C double $cDouble")

    val scalaString: String = "From Scala string"
    val cString: CString = scalaString.cast[CString]
    println(s"C string: $cString")

    val stringCByImplicit = c"hello C string using c implicit"
    println("C string:" + fromCString(stringCByImplicit))

  }

  /**
    * Normal output of a Scala program
    */
  private def regularOutput(): Unit = {
    val sentence = "Hello, world!"
    println(sentence.toUpperCase)
  }

  /**
    * Normal use of Scala collections
    */
  private def collections(): Unit = {
    val listType = List(1, 2, 3, 4)
    val mapType = Map("a" -> 1, "b" -> 2, "c" -> 3)
    val setType = Set(1, 1, 2, 2, 3, 3)
    println(s"listType=$listType, mapType=$mapType, setType=$setType")
  }

  /**
    * A simple for comprehension using Option and Try monad
    */
  private def forComprehension(): Unit = {
    val monadOption = for {
      x <- Some("Hello")
      y <- Some(x + " world")
      z <- Some(y + " option")

    } yield z

    val monadTry = for {
      a <- Try(1)
      b <- Try(2)
    } yield a + b

    println(s"monadOption=$monadOption, monadTry=$monadTry")
  }

}

