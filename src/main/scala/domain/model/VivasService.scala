package domain.model

object VivasService {

  def differAndIntersect(vivas: List[Viva]) = {

    val vivasResources =
      vivas.flatMap(x => x.jury.asResourcesSet.toList.map(y => y.id.s))

    println("print vivas resources")
    println(vivasResources)

    val count = vivasResources.groupBy(identity).view.mapValues(_.size)

    val intersects = count.filter(x => x._2 > 1).keys

    val differences = count.filter(x => x._2 == 1).keys

    (
      differences
        .flatMap(
          x =>
            vivas.find(
              y => !y.jury.asResourcesSet.exists(resource => resource.id.s == x)
          )
        )
        .toSet,
      intersects
        .flatMap(
          x =>
            vivas.find(
              y => y.jury.asResourcesSet.exists(resource => resource.id.s == x)
          )
        )
        .toSet
    )

    //(differences, intersects)
  }
}
