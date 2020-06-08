package domain.model

object VivasService {

  def differAndIntersect(vivas: List[Viva]): (Set[Viva], Set[Viva]) = {

    val vivasResources =
      vivas.flatMap(x => x.jury.asResourcesSet.toList)

    val count = vivasResources.groupBy(identity).view.mapValues(_.size)

    val intersects = count.filter(x => x._2 > 1).keys

    // TODO: Optimize
    val vivasWhichResourcesIntersect = vivas.filter(
      _.jury.asResourcesSet.exists(r => intersects.toList.contains(r))
    )

    (
      vivas.diff(vivasWhichResourcesIntersect).toSet,
      vivasWhichResourcesIntersect.toSet
    )
  }
}
