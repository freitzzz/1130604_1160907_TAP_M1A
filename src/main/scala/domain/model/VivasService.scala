package domain.model

object VivasService {

  /**
    * Given a vivas list, vivas that share resources as jury are differentiated from those vivas that do not share.
    * The output is a tuple of tuple, in which the first tuple tuple corresponds
    * to the pair of the vivas that do not share resources and the corresponding resources that are not shared
    * and the second tuple tuple the pair of the vivas that share resources and the corresponding resources that are shared
    */
  def differAndIntersect(
    vivas: List[Viva]
  ): ((Set[Viva], List[Resource]), (Set[Viva], List[Resource])) = {

    val vivasResources =
      vivas.flatMap(x => x.jury.asResourcesSet.toList)

    val count = vivasResources.groupBy(identity).view.mapValues(_.size)

    val intersects = count.filter(x => x._2 > 1).keys.toList
    val differences = count.filter(x => x._2 == 1).keys.toList

    // TODO: Optimize
    val vivasWhichResourcesIntersect =
      vivas.filter(_.jury.asResourcesSet.exists(r => intersects.contains(r)))

    (
      (vivas.diff(vivasWhichResourcesIntersect).toSet, differences),
      (vivasWhichResourcesIntersect.toSet, intersects)
    )
  }

  def findVivasThatShareTheSameJury(
    vivas: List[Viva]
  ): Map[Set[Resource], List[Viva]] = {

    val vivasPerResourcesSet = vivas.groupBy(viva => viva.jury.asResourcesSet)

    vivasPerResourcesSet.filter(_._2.size != 1)

  }
}
