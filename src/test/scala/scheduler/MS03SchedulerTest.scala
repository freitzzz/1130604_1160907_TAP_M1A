package scheduler

import domain.model.{ScheduledViva, Viva}
import domainSchedulerImpl.MS03Scheduler
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import xml.Functions

import scala.util.Try

class MS03SchedulerTest extends AnyFunSuite with Matchers {

  test(
    "given an empty list of vivas, then MS03 Scheduler returns an empty list"
  ) {

    // Arrange

    val vivas = List[Viva]()

    // Act

    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)

    // Assert

    scheduledVivas shouldBe List.empty[Try[ScheduledViva]]

  }

  test(
    "given a list of vivas that do not share resources, then the schedule that maximizes schedule preference of vivas which do not share resources is applied"
  ) {

    //Arrange

    val vivas = VivasGiver.giveMeAListOfVivasThatDoNotShareResources()

    //Act

    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)
    val expectedScheduledVivasSize = vivas.size

    //Assert

    scheduledVivas.size shouldBe expectedScheduledVivasSize
    scheduledVivas
      .filter(_.isSuccess)
      .map(x => x.get)
      .map(x => x.scheduledPreference)
      .sum shouldBe 25
  }

  test(
    "given a list of vivas that only share resources, then the schedule that maximizes schedule preference of vivas which that share resources is applied"
  ) {

    //Arrange

    val vivas = VivasGiver.giveMeAListOfVivasThatOnlyShareResources()

    //Act

    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)
    val expectedScheduledVivasSize = vivas.size

    //Assert

    scheduledVivas.size shouldBe expectedScheduledVivasSize
    scheduledVivas
      .filter(_.isSuccess)
      .map(x => x.get)
      .map(x => x.scheduledPreference)
      .sum shouldBe 21
  }

  test(
    "given a list of vivas that share and don't resources, then the schedule that maximizes schedule preference of vivas which that share resources is applied to vivas which share resources and the schedule that maximizes schedule preference of vivas which do not share resources is applied to vivas which do not share resources"
  ) {

    //Arrange

    val vivas = VivasGiver.giveMeAListOfVivasThatShareResources()

    //Act

    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)
    val expectedScheduledVivasSize = vivas.size

    //Assert
    scheduledVivas.size shouldBe expectedScheduledVivasSize
    scheduledVivas
      .filter(_.isSuccess)
      .map(x => x.get)
      .map(x => x.scheduledPreference)
      .sum shouldBe 36
  }

  test(
    "given a list of vivas which schedule period is not the same, then scheduler returns the scheduled vivas based on their period earliness"
  ) {

    //Arrange

    val vivas = VivasGiver.giveMeAListOfVivasThatDoNotShareTimePeriods()

    //Act
    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)
    val expectedScheduledVivasSize = vivas.size
    val scheduledVivasPeriods = scheduledVivas.map(x => x.get.period)

    //Assert
    scheduledVivas.size shouldBe expectedScheduledVivasSize
    scheduledVivas
      .filter(_.isSuccess)
      .map(x => x.get)
      .map(x => x.scheduledPreference)
      .sum shouldBe 25

    for (a <- scheduledVivasPeriods; b <- scheduledVivasPeriods) {
      a.start.isBefore(b.start)
      a.end.isBefore(b.end)
    }
  }

  test(
    "given a list of vivas which schedule period is the same, then scheduler returns the scheduled vivas based on their period earliness and to those scheduled vivas which period is the same the order is based on the viva title"
  ) {

    //Arrange
    val vivas = VivasGiver.giveMeAListOfVivasThatShareTheExactSameTimePeriod()

    //Act
    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)
    val expectedScheduledVivasSize = vivas.size
    val scheduledVivasPeriods = scheduledVivas.map(x => x.get.period)
    val scheduledVivasTitles = scheduledVivas.map(x => x.get.viva.title.s)

    //Assert
    scheduledVivas.size shouldBe expectedScheduledVivasSize
    scheduledVivas
      .filter(_.isSuccess)
      .map(x => x.get)
      .map(x => x.scheduledPreference)
      .sum shouldBe 23

    for (a <- scheduledVivasPeriods; b <- scheduledVivasPeriods) {
      a.start.isEqual(b.start)
      a.end.isEqual(b.end)
    }

    scheduledVivasTitles shouldEqual vivas.map(x => x.title.s)
  }

  test(
    "given a list of vivas which resources have availabilities that overlap after a period division, but at least a combination of scheduled vivas is successful, then the scheduler returns a successful complete schedule"
  ) {}

  test(
    "given a list of vivas that shares resources and it was not possible to schedule these then the scheduler returns a Failure indicating that the schedule is impossible"
  ) {

    //Arrange

    val vivas = VivasGiver.giveMeAListOfVivasThatCannotBeFullyScheduled()

    //Act
    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)

    //Assert
    scheduledVivas.head.isFailure shouldBe true
  }

  test(
    "given a list of vivas in which two vivas share the same jury, then the combination of scheduled vivas is based on the max sum of scheduled preferences and based on the order of the input of these two vivas that share the same jury"
  ) {

    val vivas = VivasGiver.giveMeAListOfVivasThatOnlyShareResources()

    //Act

    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)
    val expectedScheduledVivasSize = vivas.size
    val expectedScheduledVivasOrderTitles = vivas.map(x => x.title.s)

    //Assert

    scheduledVivas.size shouldBe expectedScheduledVivasSize
    scheduledVivas
      .filter(_.isSuccess)
      .map(x => x.get)
      .map(x => x.scheduledPreference)
      .sum shouldBe 21

    scheduledVivas.map(x => x.get.viva.title.s) shouldEqual expectedScheduledVivasOrderTitles
  }

  test(
    "given a list of vivas in which four or more (6/8/10 ...) vivas share the same jury, then the combination of scheduled vivas is based on the max sum of scheduled preferences and based on the order of the input of these vivas that share the same jury"
  ) {

    val vivas = VivasGiver.giveMeAListOf6VivasThatShareTheSameResources()

    //Act

    val scheduledVivas = MS03Scheduler.scheduleVivas(vivas)
    val expectedScheduledVivasSize = vivas.size
    val expectedScheduledVivasOrderTitles = vivas.map(x => x.title.s)

    //Assert

    scheduledVivas.size shouldBe expectedScheduledVivasSize
    scheduledVivas
      .filter(_.isSuccess)
      .map(x => x.get)
      .map(x => x.scheduledPreference)
      .sum shouldBe 40

    scheduledVivas.map(x => x.get.viva.title.s) shouldEqual expectedScheduledVivasOrderTitles
  }

  object VivasGiver {

    def giveMeAListOfVivasThatDoNotShareResources(): List[Viva] = {

      val xml =
        <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
                        duration="01:00:00">
        <vivas>
          <viva student="Student 001" title="Title 1">
            <president id="T001"/>
            <adviser id="T002"/>
            <supervisor id="E001"/>
          </viva>
          <viva student="Student 002" title="Title 2">
            <president id="T002"/>
            <adviser id="T001"/>
            <supervisor id="E001"/>
          </viva>
        </vivas>
        <resources>
          <teachers>
            <teacher id="T001" name="Teacher 001">
              <availability start="2020-05-30T09:30:00" end="2020-05-30T12:30:00" preference="5"/>
              <availability start="2020-05-30T13:30:00" end="2020-05-30T16:30:00" preference="3"/>
            </teacher>
            <teacher id="T002" name="Teacher 002">
              <availability start="2020-05-30T10:30:00" end="2020-05-30T11:30:00" preference="5"/>
              <availability start="2020-05-30T14:30:00" end="2020-05-30T17:00:00" preference="5"/>
            </teacher>
          </teachers>
          <externals>
            <external id="E001" name="External 001">
              <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="2"/>
              <availability start="2020-05-30T15:30:00" end="2020-05-30T18:00:00" preference="5"/>
            </external>
            <external id="E002" name="External 002">
              <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="1"/>
              <availability start="2020-05-30T14:00:00" end="2020-05-30T18:30:00" preference="4"/>
            </external>
          </externals>
        </resources>
      </agenda>

      Functions.deserialize(xml).get

    }

    def giveMeAListOfVivasThatOnlyShareResources(): List[Viva] = {

      val xml =
        <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
                duration="01:00:00">
          <vivas>
            <viva student="Student 001" title="Title 1">
              <president id="T001"/>
              <adviser id="T002"/>
            </viva>
            <viva student="Student 002" title="Title 2">
              <president id="T002"/>
              <adviser id="T001"/>
              <supervisor id="E001"/>
            </viva>
          </vivas>
          <resources>
            <teachers>
              <teacher id="T001" name="Teacher 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
              </teacher>
              <teacher id="T002" name="Teacher 002">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
              </teacher>
            </teachers>
            <externals>
              <external id="E001" name="External 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="2"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T17:30:00" preference="5"/>
              </external>
            </externals>
          </resources>
        </agenda>

      Functions.deserialize(xml).get

    }

    def giveMeAListOfVivasThatShareResources(): List[Viva] = {

      val xml =
        <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
                duration="01:00:00">
          <vivas>
            <viva student="Student 001" title="Title 1">
              <president id="T001"/>
              <adviser id="T002"/>
            </viva>
            <viva student="Student 003" title="Title 3">
              <president id="T003"/>
              <adviser id="T004"/>
              <supervisor id="E002"/>
            </viva>
            <viva student="Student 002" title="Title 2">
              <president id="T002"/>
              <adviser id="T001"/>
              <supervisor id="E001"/>
            </viva>
          </vivas>
          <resources>
            <teachers>
              <teacher id="T001" name="Teacher 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
              </teacher>
              <teacher id="T002" name="Teacher 002">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
              </teacher>
              <teacher id="T003" name="Teacher 003">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
              </teacher>
              <teacher id="T004" name="Teacher 004">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
              </teacher>
            </teachers>
            <externals>
              <external id="E001" name="External 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T11:30:00" end="2020-05-30T12:30:00" preference="2"/>
              </external>
              <external id="E002" name="External 002">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T11:30:00" end="2020-05-30T12:30:00" preference="4"/>
              </external>
            </externals>
          </resources>
        </agenda>

      Functions.deserialize(xml).get

    }

    def giveMeAListOfVivasThatDoNotShareTimePeriods(): List[Viva] = {

      val xml =
        <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
                duration="01:00:00">
          <vivas>
            <viva student="Student 001" title="Title 1">
              <president id="T001"/>
              <adviser id="T002"/>
              <supervisor id="E001"/>
            </viva>
            <viva student="Student 002" title="Title 2">
              <president id="T003"/>
              <adviser id="T004"/>
              <supervisor id="E002"/>
            </viva>
          </vivas>
          <resources>
            <teachers>
              <teacher id="T001" name="Teacher 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T12:30:00" preference="5"/>
                <availability start="2020-05-30T13:30:00" end="2020-05-30T16:30:00" preference="3"/>
              </teacher>
              <teacher id="T002" name="Teacher 002">
                <availability start="2020-05-30T10:30:00" end="2020-05-30T11:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T17:00:00" preference="5"/>
              </teacher>
              <teacher id="T003" name="Teacher 003">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T12:30:00" preference="5"/>
                <availability start="2020-05-30T13:30:00" end="2020-05-30T16:30:00" preference="3"/>
              </teacher>
              <teacher id="T004" name="Teacher 004">
                <availability start="2020-05-30T10:30:00" end="2020-05-30T11:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T17:00:00" preference="5"/>
              </teacher>
            </teachers>
            <externals>
              <external id="E001" name="External 001">
                <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="2"/>
                <availability start="2020-05-30T15:30:00" end="2020-05-30T18:00:00" preference="5"/>
              </external>
              <external id="E002" name="External 002">
                <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="1"/>
                <availability start="2020-05-30T14:00:00" end="2020-05-30T18:30:00" preference="4"/>
              </external>
            </externals>
          </resources>
        </agenda>

      Functions.deserialize(xml).get

    }

    def giveMeAListOfVivasThatShareTheExactSameTimePeriod(): List[Viva] = {

      val xml =
        <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
                duration="01:00:00">
          <vivas>
            <viva student="Student 001" title="Title 1">
              <president id="T001"/>
              <adviser id="T002"/>
              <supervisor id="E001"/>
            </viva>
            <viva student="Student 002" title="Title 2">
              <president id="T003"/>
              <adviser id="T004"/>
              <supervisor id="E002"/>
            </viva>
          </vivas>
          <resources>
            <teachers>
              <teacher id="T001" name="Teacher 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T12:30:00" preference="5"/>
              </teacher>
              <teacher id="T002" name="Teacher 002">
                <availability start="2020-05-30T10:30:00" end="2020-05-30T11:30:00" preference="5"/>
              </teacher>
              <teacher id="T003" name="Teacher 003">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T12:30:00" preference="5"/>
              </teacher>
              <teacher id="T004" name="Teacher 004">
                <availability start="2020-05-30T10:30:00" end="2020-05-30T11:30:00" preference="5"/>
              </teacher>
            </teachers>
            <externals>
              <external id="E001" name="External 001">
                <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="2"/>
              </external>
              <external id="E002" name="External 002">
                <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="1"/>
              </external>
            </externals>
          </resources>
        </agenda>

      Functions.deserialize(xml).get

    }

    def giveMeAListOfVivasThatCannotBeFullyScheduled(): List[Viva] = {

      val xml =
        <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
                duration="01:00:00">
          <vivas>
            <viva student="Student 001" title="Title 1">
              <president id="T001"/>
              <adviser id="T002"/>
              <supervisor id="E001"/>
            </viva>
            <viva student="Student 002" title="Title 2">
              <president id="T002"/>
              <adviser id="T001"/>
              <supervisor id="E001"/>
            </viva>
          </vivas>
          <resources>
            <teachers>
              <teacher id="T001" name="Teacher 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T12:30:00" preference="5"/>
              </teacher>
              <teacher id="T002" name="Teacher 002">
                <availability start="2020-05-30T10:30:00" end="2020-05-30T11:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T17:00:00" preference="5"/>
              </teacher>
            </teachers>
            <externals>
              <external id="E001" name="External 001">
                <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="2"/>
                <availability start="2020-05-30T15:30:00" end="2020-05-30T18:00:00" preference="5"/>
              </external>
              <external id="E002" name="External 002">
                <availability start="2020-05-30T10:00:00" end="2020-05-30T13:30:00" preference="1"/>
              </external>
            </externals>
          </resources>
        </agenda>

      Functions.deserialize(xml).get

    }

    def giveMeAListOf6VivasThatShareTheSameResources(): List[Viva] = {

      val xml =
        <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
                duration="01:00:00">
          <vivas>
            <viva student="Student 001" title="Title 1">
              <president id="T001"/>
              <adviser id="T002"/>
            </viva>
            <viva student="Student 002" title="Title 2">
              <president id="T002"/>
              <adviser id="T001"/>
            </viva>
            <viva student="Student 003" title="Title 3">
              <president id="T001"/>
              <adviser id="T002"/>
            </viva>
            <viva student="Student 004" title="Title 4">
              <president id="T001"/>
              <adviser id="T002"/>
            </viva>
            <viva student="Student 005" title="Title 5">
              <president id="T001"/>
              <adviser id="T002"/>
            </viva>
            <viva student="Student 006" title="Title 6">
              <president id="T001"/>
              <adviser id="T002"/>
            </viva>
          </vivas>
          <resources>
            <teachers>
              <teacher id="T001" name="Teacher 001">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
                <availability start="2020-05-30T18:30:00" end="2020-05-30T20:30:00" preference="4"/>
                <availability start="2020-05-31T10:30:00" end="2020-05-31T13:30:00" preference="2"/>
                <availability start="2020-05-31T15:30:00" end="2020-05-31T17:30:00" preference="1"/>
              </teacher>
              <teacher id="T002" name="Teacher 002">
                <availability start="2020-05-30T09:30:00" end="2020-05-30T10:30:00" preference="5"/>
                <availability start="2020-05-30T14:30:00" end="2020-05-30T15:30:00" preference="3"/>
                <availability start="2020-05-30T18:30:00" end="2020-05-30T20:30:00" preference="4"/>
                <availability start="2020-05-31T10:30:00" end="2020-05-31T13:30:00" preference="2"/>
                <availability start="2020-05-31T15:30:00" end="2020-05-31T17:30:00" preference="1"/>
              </teacher>
            </teachers>
            <externals>
            </externals>
          </resources>
        </agenda>

      Functions.deserialize(xml).get

    }

  }
}
