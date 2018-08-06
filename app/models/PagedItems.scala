package models

import skinny.Pagination

case class PagedItems[+T](pagination: Pagination, totalCount: Long, items : Seq[T]) {

  lazy val currentPage: Int = pagination.pageNo

  lazy val minPage: Int = 1
  lazy val maxPage: Int = Math.ceil(totalCount.toDouble / pagination.pageSize.toDouble).toInt max 1

  lazy val hasPrevious: Boolean = currentPage > minPage
  lazy val hasNext: Boolean = currentPage < maxPage

}