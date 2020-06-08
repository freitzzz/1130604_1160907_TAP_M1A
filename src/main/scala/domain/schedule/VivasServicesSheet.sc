import domain.model._

val v1 = Set[String]("T1", "T2", "T3", "E1")

val v2 = Set[String]("T4", "T5", "T6", "E2")

val v3 = Set[String]("T7", "T1", "T4", "E3")

val v4 = Set[String]("T8", "T4", "T5", "E3")

val v5 = Set[String]("T9", "T10", "T11", "E4")

val intersect = v1.intersect(v2) ++ v2.intersect(v3) ++ v3.intersect(v4) ++ v4.intersect(v5)

val toList = v1.toList ++ v2.toList ++ v3.toList ++ v4.toList ++ v5.toList

val count = toList.groupBy(identity).view.mapValues(_.size)

val intersects = count.filter(x => x._2 > 1).keys

val excludes = count.filter(x => x._2 == 1).keys

val ast = List("v1", "v2", "v3", "v4", "v5")

val vivas = List(v1, v2, v3, v4, v5)
  .map(x => x.toList)
  .map(
    x =>
      Viva.create(
        NonEmptyString.create("x").get,
        NonEmptyString.create("x1").get,
        Jury
          .create(
            Teacher
              .create(
                NonEmptyString.create(x(0)).get,
                NonEmptyString.create("xxxx").get,
                List[Availability](),
                List(President())
              )
              .get,
            Teacher
              .create(
                NonEmptyString.create(x(1)).get,
                NonEmptyString.create("xxxx").get,
                List[Availability](),
                List(Adviser())
              )
              .get,
            List(External
              .create(
                NonEmptyString.create(x(3)).get,
                NonEmptyString.create("xxxx").get,
                List[Availability](),
                List(Supervisor())
              )
              .get),
            List(Teacher
              .create(
                NonEmptyString.create(x(2)).get,
                NonEmptyString.create("xxxx").get,
                List[Availability](),
                List(CoAdviser())
              )
              .get),
          )
          .get,
        Duration.create(java.time.Duration.ofHours(4)).get
      )
  ).zipWithIndex.map(x => Viva.create(x._1.title, NonEmptyString.create(ast(x._2)).get, x._1.jury, x._1.duration))

print(vivas.map(x => x.title))

vivas.forall(x => {
  println(x.jury.asResourcesSet)
  true
} )

val diffAndIntersect = VivasService.differAndIntersect(vivas)

//diffAndIntersect._1.map(x => x.title)

print(diffAndIntersect._1.size)

println(diffAndIntersect._1)

//diffAndIntersect._2.map(x => x.title)


print(diffAndIntersect._2.size)