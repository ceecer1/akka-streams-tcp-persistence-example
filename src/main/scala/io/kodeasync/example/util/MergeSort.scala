package io.kodeasync.example.util

/**
  * Created by shishir on 8/4/16.
  */
object MergeSort {

  //Merge sort logic below
  def doMerge(left: Vector[Char], right: Vector[Char]): Vector[Char] = {
    var leftIndex = 0
    var rightIndex = 0
    var merged = Vector[Char]()
    while (leftIndex < left.length && rightIndex < right.length) {
      if (left(leftIndex) >= right(rightIndex)) {
        merged :+= left(leftIndex)
        leftIndex += 1
      } else {
        merged :+= right(rightIndex)
        rightIndex += 1
      }
    }
    if (leftIndex == left.length) {
      merged ++ right.slice(rightIndex, right.length)
    } else {
      merged ++ left.slice(leftIndex, left.length)
    }
  }

  def doSort(items: Vector[Char]): Vector[Char] = {
    items match {
      case Vector(_) => items
      case _ =>
        val (left, right) = items.splitAt(items.length / 2)
        doMerge(doSort(left), doSort(right))
    }
  }

}
