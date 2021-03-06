/*
 * Source file provided under Apache License, Version 2.0, January 2004,
 *  http://www.apache.org/licenses/
 *  (c) Copyright DecisionBrain SAS 2016,2020
 *
 */

package com.decisionbrain.cplex.cp

import com.decisionbrain.cplex.IntExpr
import com.decisionbrain.cplex.Modeler._
import com.decisionbrain.cplex.cp.CpModel._
import ilog.cp.IloCP

/**
  * This is a problem of building five houses. The masonry, roofing, painting, etc. must be scheduled.  Some tasks must
  * necessarily take place before others and these requirements are expressed through precedence constraints.
  *
  * There are two workers and each task requires a specific worker.  The worker has a calendar of days off that must be
  * taken into account. The objective is to minimize the overall completion date.
  */
object SchedCalendar {

  val nbWorkers = 3

  val houses = List(31, 0, 90, 120, 90) // 5 houses with different release dates

  val tasks = List(
    ("masonry", 35), // pair of task name and duration
    ("carpentry", 15),
    ("plumbing", 40),
    ("ceiling", 15),
    ("roofing",  5),
    ("painting", 10),
    ("windows",  5),
    ("facade", 10),
    ("garden",  5),
    ("moving",  5)
  )



  implicit var model: CpModel = _

  var allTaskVars: Map[String, IntervalVar] = _
  var endVars: List[IntExpr] = _
  var joeTaskVars: List[IntervalVar] = _
  var jimTaskVars: List[IntervalVar] = _

  var joeCalendar: NumToNumStepFunction = _
  var jimCalendar: NumToNumStepFunction = _

  def makeHouse(id: Int, rd: Int): (IntExpr, Iterable[IntervalVar], Iterable[IntervalVar], Iterable[IntervalVar]) = {

    val taskVars: Map[String, IntervalVar] = (for (task <- tasks; (tname, tduration) = task)
          yield (tname , model.intervalVar(sizeMin = tduration, sizeMax = tduration, name = "H" + id + "-" + tname))).toMap

    // The lines below are equivalent
    model.add(taskVars("masonry") < taskVars("carpentry"))
    model.add(taskVars("masonry") < taskVars("plumbing"))
    model.add(taskVars("masonry") < taskVars("ceiling"))
    model.add(taskVars("carpentry") < taskVars("roofing"))
    model.add(taskVars("ceiling") < taskVars("painting"))
    model.add(taskVars("roofing") < taskVars("windows"))
    model.add(taskVars("roofing") < taskVars("facade"))
    model.add(taskVars("plumbing") < taskVars("facade"))
    model.add(taskVars("roofing") < taskVars("garden"))
    model.add(taskVars("plumbing") < taskVars("garden"))
    model.add(taskVars("windows") < taskVars("moving"))
    model.add(taskVars("facade") < taskVars("moving"))
    model.add(taskVars("garden") < taskVars("moving"))
    model.add(taskVars("painting") < taskVars("moving"))

    taskVars("masonry").setStartMin(rd)

    val joeTaskVars = List(taskVars("masonry")
      , taskVars("carpentry")
      , taskVars("roofing")
      , taskVars("facade")
      , taskVars("garden")
    )

    val jimTaskVars = taskVars.values.filterNot(v => joeTaskVars.contains(v))


    (model.endOf(taskVars("moving")), taskVars.values, joeTaskVars, jimTaskVars)
  }

  def build(): CpModel = {

    model = CpModel("SchedCumul")

    val results = for ((rd, index) <- houses.zipWithIndex) yield makeHouse(index, rd)

    endVars = results.map(_._1)
    allTaskVars = results.flatMap(_._2).map(v => (v.getName.getOrElse(""), v)).toMap
    joeTaskVars = results.flatMap(_._3)
    jimTaskVars = results.flatMap(_._4)

    model.add(noOverlap(joeTaskVars))
    model.add(noOverlap(jimTaskVars))


    joeCalendar = model.numToNumStepFunction
    joeCalendar.setValue(0, 2 * 365, 100)
    jimCalendar = model.numToNumStepFunction
    jimCalendar.setValue(0, 2 * 365, 100)

    /* WEEK ENDS. */
    for (w <- 0 until 2 * 52) {
      joeCalendar.setValue(5 + (7 * w), 7 + (7 * w), 0)
      jimCalendar.setValue(5 + (7 * w), 7 + (7 * w), 0)
    }

    /* HOLIDAYS. */
    joeCalendar.setValue(5, 12, 0)
    joeCalendar.setValue(124, 131, 0)
    joeCalendar.setValue(215, 236, 0)
    joeCalendar.setValue(369, 376, 0)
    joeCalendar.setValue(495, 502, 0)
    joeCalendar.setValue(579, 600, 0)
    jimCalendar.setValue(26, 40, 0)
    jimCalendar.setValue(201, 225, 0)
    jimCalendar.setValue(306, 313, 0)
    jimCalendar.setValue(397, 411, 0)
    jimCalendar.setValue(565, 579, 0)

    for (i <- joeTaskVars.indices) {
      joeTaskVars(i).setIntensity(joeCalendar)
      model.add(forbidStart(joeTaskVars(i), joeCalendar))
      model.add(forbidEnd(joeTaskVars(i), joeCalendar))
    }
    for (i <- jimTaskVars.indices) {
      jimTaskVars(i).setIntensity(jimCalendar)
      model.add(forbidStart(jimTaskVars(i), jimCalendar))
      model.add(forbidEnd(jimTaskVars(i), jimCalendar))
    }


    model.add(minimize(max(endVars)))

    model
  }

  def solve(): Boolean = {

    println(s"Solving model $model....")

//    model.exportModel("SchedCalendar.cpo")

    model.cp.setParameter(IloCP.IntParam.FailLimit, 10000)

    //    val status = model.solve(timeLimit=20, logPeriod=3000)
    val status = model.solve()

    if (status) {
      println(s"Solution status: $status")
      println("Solution with objective " + model.getObjectiveValue())
      println("Joe Calendar: " + joeCalendar)
      println("Jim Calendar: " + jimCalendar)
      println("Joe Schedule:")
      for (v <- SchedCalendar.joeTaskVars)
        println("\t" + model.getDomain(v))
      println("Jim Schedule:")
      for (v <- SchedCalendar.jimTaskVars)
        println("\t" + model.getDomain(v))
    }

    status
  }

  def run(): Boolean = {
    val model = build()
    val status = solve()
    model.end()
    status
  }

  def main(args: Array[String]): Unit = {
    run()
  }

}
