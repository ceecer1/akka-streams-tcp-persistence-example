package io.kodeasync.example.manager

import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

/**
  * Created by shishir on 8/8/16.
  */
class MergeSortTest extends FlatSpecLike with BeforeAndAfterAll {

  "A merge sort method" should
    "sort supplied string in descending order" in {

    val testString = "hello world"
    val mergeSorted = MergeSort.doSort(testString.toVector).mkString
    println(mergeSorted)

    assert("wroolllhed " == mergeSorted)

  }

}
