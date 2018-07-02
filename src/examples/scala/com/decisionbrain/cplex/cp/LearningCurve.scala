/*
 * Source file provided under Apache License, Version 2.0, January 2004,
 * http://www.apache.org/licenses/
 * (c) Copyright DecisionBrain SAS 2016,2018
 */

package com.decisionbrain.cplex.cp

import com.decisionbrain.cplex.cp.CpModel._
import ilog.concert.IloIntervalVar
import ilog.cp.IloCP

/**
  * This example solves a scheduling problem on two alternative heterogeneous machines. A set of tasks {a_1,...,a_n}
  * has to be executed on either one of the two machines. Different types of tasks are distinguished, the type of task
  * a_i is denoted tp_i.
  *
  * A machine m needs a sequence dependent setup time setup(tp,tp') to switch from a task of type tp to the next task
  * of type tp'. Furthermore some transitions tp->tp' are forbidden.
  *
  * The two machines are different: they process tasks with different speed and have different setup times and forbidden
  * transitions.
  *
  * The objective is to minimize the makespan.
  *
  * The model uses transition distances and noOverlap constraints to model machines setup times. The noOverlap
  * constraint is specified to enforce transition distance between immediate successors on the sequence. Forbidden
  * transitions are modeled with a very large transition distance.
  */

object LearningCurve {

  val NbTypes = 5

  val SetupM1 = Vector(
    Vector(0,  26,  8,  3, -1),
    Vector(22,  0, -1,  4, 22),
    Vector(28,  0,  0, 23,  9),
    Vector(29, -1, -1,  0,  8),
    Vector(26, 17, 11,  7,  0)
  )

  val SetupM2 = Vector(
    Vector(0,  5, 28, -1,  2),
    Vector(-1, 0, -1,  7, 10),
    Vector(19, 22,  0, 28, 17),
    Vector(7, 26, 13,  0, -1),
    Vector(13, 17, 26, 20, 0)
  )

  val NbTasks   = 50

  val TaskType = Vector(
    3, 3, 1, 1, 1, 1, 2, 0, 0, 2,
    4, 4, 3, 3, 2, 3, 1, 4, 4, 2,
    2, 1, 4, 2, 2, 0, 3, 3, 2, 1,
    2, 1, 4, 3, 3, 0, 2, 0, 0, 3,
    2, 0, 3, 2, 2, 4, 1, 2, 4, 3
  )

  val TaskIndex : Vector[Int]= (for (i <- 0 until NbTasks) yield i)(collection.breakOut)

  val TaskDurM1 = Vector(
    4, 17,  4,  7, 17, 14,  2, 14,  2,  8,
    11, 14,  4, 18,  3,  2,  9,  2,  9, 17,
    18, 19,  5,  8, 19, 12, 17, 11,  6,  3,
    13,  6, 19,  7,  1,  3, 13,  5,  3,  6,
    11, 16, 12, 14, 12, 17,  8,  8,  6,  6
  )

  val TaskDurM2 = Vector(
    12,  3, 12, 15,  4,  9, 14,  2,  5,  9,
    10, 14,  7,  1, 11,  3, 15, 19,  8,  2,
    18, 17, 19, 18, 15, 14,  6,  6,  1,  2,
    3, 19, 18,  2,  7, 16,  1, 18, 10, 14,
    2,  3, 14,  1,  1,  6, 19,  5, 17,  4
  )

  val InitialType1 = -1
  val InitialType2 = -1

  implicit var model: CpModel = _

  var s1 : IntervalSequenceVar = _
  var s2 : IntervalSequenceVar = _

  var si1 : IntervalSequenceVar = _
  var si2 : IntervalSequenceVar = _

  var o1 : Array[IntVar] = _
  var o2 : Array[IntVar] = _

  var IndexOfTask : Map[IloIntervalVar, Int] = _

  def build(): CpModel = {

    model = CpModel("SchedSetup")

    // transition distance

    val setup1 = model.transitionDistance(NbTypes)
    val setup2 = model.transitionDistance(NbTypes)

    for (i <- 0 until NbTypes; j <- 0 until NbTypes) {
      val d1 = SetupM1(i)(j)
      setup1.setValue(i, j, if (d1 < 0) IloCP.IntervalMax else d1)
      val d2 = SetupM2(i)(j)
      setup2.setValue(i, j, if (d2 < 0) IloCP.IntervalMax else d2)
    }

    // interval variables

    val a: Array[IntervalVar] = (for (i <- 0 until NbTasks) yield
      model.intervalVar(name="A" + i + "_TP" + TaskType(i)))(collection.breakOut)
    val a1: Array[IntervalVar] = (for (i <- 0 until NbTasks) yield
      model.intervalVar(name="A" + i + "_M1_TP" + TaskType(i)))(collection.breakOut)
    val a2: Array[IntervalVar] = (for (i <- 0 until NbTasks) yield
      model.intervalVar(name="A" + i + "_M2_TP" + TaskType(i)))(collection.breakOut)

    // interval variables on machines are optional

    for (i <- 0 until NbTasks) {
      a1(i).setSizeMin(TaskDurM1(i))
      a1(i).setOptional()
      a2(i).setSizeMin(TaskDurM2(i))
      a2(i).setOptional()
      model.add(alternative(a(i), Array(a1(i), a2(i))))
    }

    IndexOfTask = (for (i <- 0 until NbTasks) yield (a1(i).getIloIntervalVar() -> i))(collection.breakOut)
    IndexOfTask  = IndexOfTask ++ (for (i <- 0 until NbTasks) yield (a2(i).getIloIntervalVar()-> i))(collection.breakOut)

    for (x <- a1) {
      val index = IndexOfTask.getOrElse(x.getIloIntervalVar(), -1)
      if (index < 0) {
        System.out.println("Cannot find index of interval " + x)
      }
    }
    for (x <- a2) {
      val index = IndexOfTask.getOrElse(x.getIloIntervalVar(), -1)
      if (index < 0) {
        System.out.println("Cannot find index of interval " + x)
      }
    }


    // sequence variables (one per machine)
    s1 = model.intervalSequenceVar(a1, TaskType.toArray)
    s2 = model.intervalSequenceVar(a2, TaskType.toArray)

    si1 = model.intervalSequenceVar(a1, TaskIndex.toArray)
    si2 = model.intervalSequenceVar(a2, TaskIndex.toArray)

    // no overlap constraints with setup times
    model.add(noOverlap(s1, setup1, true))
    model.add(noOverlap(s2, setup2, true))
    model.add(noOverlap(si1))
    model.add(noOverlap(si2))
    model.add(sameSequence(s1, si1))
    model.add(sameSequence(s2, si2))


    o1 = (for (i <- 0 until NbTasks) yield model.intVar(name="O" + i + "_M1_TP" + TaskType(i)))(collection.breakOut)
    o2 = (for (i <- 0 until NbTasks) yield model.intVar(name="O" + i + "_M2_TP" + TaskType(i)))(collection.breakOut)

    for (i <- 0 until NbTasks) {
      model.add((typeOfPrevious(s1, a1(i), InitialType1, TaskType(i)) != TaskType(i)) <= (o1(i) == startOf(a1(i))))
      model.add((typeOfPrevious(s2, a2(i), InitialType2, TaskType(i)) != TaskType(i)) <= (o2(i) == startOf(a2(i))))

      val expr1 =  model.element(o1.toArray[IntExpr], typeOfPrevious(si1, a1(i), i, i))
      model.add((typeOfPrevious(s1, a1(i), -1, -1) == TaskType(i)) <= (o1(i) == expr1 + endOfPrevious(s1, a1(i), 0) - startOf(a1(i))))
      val expr2 =  model.element(o2.toArray[IntExpr], typeOfPrevious(si2, a2(i), i))
      model.add((typeOfPrevious(s2, a2(i), -1, -1) == TaskType(i)) <= (o2(i) == expr2+ endOfPrevious(s2, a2(i), 0) - startOf(a2(i))))

    }

    // minimize makespan
    model.add(minimize(max(for (v <- a) yield endOf(v))))

    model.exportModel("learningcurve.cpo")

    model
  }

  def solve(): Boolean = {

    println(s"Solving model $model....")

    //    model.exportModel("SchedSetup.cpo")

    model.cp.setParameter(IloCP.IntParam.FailLimit, 1000000)
    val status = model.solve(logPeriod=10000)
    //    val status = model.solve()

    if (status) {
      println(s"Solution status: $status")
      println("Solution with objective " + model.getObjectiveValue())

      System.out.println("Machine 1: ")
      for (x <- s1) {
        System.out.println(model.getDomain(x))
      }

      System.out.println("Previous index on Machine 1: ")
      for (x <- si1) {
        System.out.print(model.getDomain(x))
        System.out.print("; previous : ")
        System.out.print(model.getValue(typeOfPrevious(si1, x, IndexOfTask(x.getIloIntervalVar()), IndexOfTask(x.getIloIntervalVar()))))
        System.out.print("; offset : ")
        val o = o1(IndexOfTask(x.getIloIntervalVar()))
        System.out.print(model.getMin(o) + ".." + model.getMax(o))
        System.out.println()
      }

      System.out.println("Machine 2: ")
      for (x <- s2) {
        System.out.println(model.getDomain(x))
      }

      System.out.println("Previous index and offset on Machine 2: ")
      for (x <- si2) {
        System.out.print(model.getDomain(x))
        System.out.print("; previous : ")
        System.out.print(model.getValue(typeOfPrevious(si2, x, IndexOfTask(x.getIloIntervalVar()), IndexOfTask(x.getIloIntervalVar()))))
        System.out.print("; offset : ")
        val o = o2(IndexOfTask(x.getIloIntervalVar()))
        System.out.print(model.getMin(o) + ".." + model.getMax(o))
        System.out.println()
      }

      System.out.println("Offsets")
      System.out.println("Machine 1: ")
      for (v <- o1) {
        System.out.print(v)
        System.out.print(" : ")
        System.out.println(model.getMin(v) + " .. " + model.getMax(v))
      }
      System.out.println("Machine 2: ")
      for (v <- o2) {
        System.out.print(v)
        System.out.print(" : ")
        System.out.println(model.getMin(v) + " .. " + model.getMax(v))
      }

    } else {
      System.out.println("No solution found.")
    }

    true
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
