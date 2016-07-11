package tests.bloomfilter.mutable

import bloomfilter.mutable.UnsafeBitArray
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

class UnsafeBitArraySpec extends Properties("UnsafeBitArray") {

  val gen = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2)
    array = new UnsafeBitArray(size)
    index <- Gen.choose[Long](0, size)
  } yield (array, index)

  property("set") = forAll(gen) {
    case (sut: UnsafeBitArray, index: Long) =>
      sut.set(index)
      sut.get(index)
  }


  def genListElems[A](max: Int)(implicit aGen: Gen[A]): Gen[List[A]] = {
    Gen.posNum[Int].map(_ % max).flatMap(i => Gen.listOfN(i, aGen))
  }

  val numOfSetBits: Int = 100

  val genUnion = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2)
    array = new UnsafeBitArray(size)
    indices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
    thatArray = new UnsafeBitArray(size)
    thatIndices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
  } yield (array, indices, thatArray, thatIndices)

  val genIntersection = for {
    size <- Gen.oneOf[Long](1, 1000, Int.MaxValue, Int.MaxValue * 2)
    array = new UnsafeBitArray(size)
    indices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
    thatArray = new UnsafeBitArray(size)
    thatIndices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
    commonIndices <- genListElems[Long]((numOfSetBits % size).toInt)(arbitrary[Long])
  } yield (array, indices, thatArray, thatIndices, commonIndices)


  property("|") = forAll(genUnion) {
    case (array: UnsafeBitArray, indices: List[Long], thatArray: UnsafeBitArray, thatIndices: List[Long]) =>
      indices.foreach(array.set)
      thatIndices.foreach(thatArray.set)
      val sut = array | thatArray
      (indices ++ thatIndices).forall(sut.get)
  }

  property("&") = forAll(genIntersection) {
    case (array: UnsafeBitArray, indices: List[Long], thatArray: UnsafeBitArray, thatIndices: List[Long], commonIndices: List[Long]) =>
      indices.foreach(array.set)
      thatIndices.foreach(thatArray.set)
      commonIndices.foreach(x => { thatArray.set(x); thatArray.set(x) })
      val sut = array & thatArray
      commonIndices.forall(sut.get)
  }

}
