# Release Notes of cplex-scala: A Scala library for IBM ILOG CPLEX

## cplex-scala v1.7.0

  * Port to CPLEX 12.10 and Scala 2.13
  * Port to Gradle 5.6.2
  * Method `getObjectiveValue` is now obsolete and replaced by method `getObjValue`
  * Add API for the CPLEX annotations, see methods `newLongAnnotation`, `setAnnotation`, `getAnnotation`...
  * Add example `Benders` that applies a Benders decomposition using the CPLEX annotations
  * Add functional programming API for piecewise linear functions.
  * Add example `SailcoPW` that uses of a piecewise linear function to create the objective.
  * Add example `Transport.scala`
  * Add optional parameter when creating a set of variables to specify a default value
  * Add API for MIP start, read and write solutions to file
  * Add API to redirect CPLEX info, warnings and errors to output stream
  * Add API for CPLEX aborter
  * Add API to get the MIP relative gap
  * Add functional programming API for MIP information callbacks

## cplex-scala v1.6.0

  * Add API for KPIs in class `CpModel`: see example `PlantLocation.scala`
  * Add methods `quot` in class `CpModel` for division and define operators '/' in class NumExpr 
  * Move methods `add` from `CpModel` and `MpModel` to class `Modeler`
  * Add methods `remove` in class `Modeler` to remove object(s) from the optimization model
  * Add method `square` in class `Modeler` for quadratic programming. 
  * Add methods to get the reduced cost(s), the dual(s) and the slack(s)
  * Add an first example of quadratic programming problem, see example `IndefQPex1`

## cplex-scala v1.5.2

This is a hotfix:

  * Update build.gradle: use default values for environment variables for Nexus
  
The library cplex-scala.jar is the same as the same as in release 1.5.0. 

## cplex-scala v1.5.1

This is a hotfix:

  * Update release notes for release 1.5.0
  * Fix issues in Jenkinsfile

The library cplex-scala.jar is the same as the same as in release 1.5.0. 
 

## cplex-scala v1.5.0

  * Update Gradle wrapper to release 5.4
  * Add API for multi-objective in MpModel: see example DietMultiObj.scala
  * Define method apply in classes `NumArray`, `IntArray` and `IntExprArray` for element constraint: this allows to 
  write constructs like `cost = costs(supplier)` where costs is an array of integer, supplier is an integer variable 
  and `cost` is an integer expression that represents the cost of the chosen supplier. See Facility.scala for a complete example..
  * Add operator * on classes `NumArray`, `IntArray`, `NumVarArray` and `IntVarArray`for scalar product.
  * Refactoring:
    * Add class Modeler to build optimization model: CpModel and MpModel now inherits from Modeler
    * Move many classes such as `NumExpr`, `IntExpr`, `NumVar`, `IntVar`, `Objective` from package com.decisoinbrain.cplex.mp and com.decisionbrain.cplex.cp to com.decisionbrain.cplex. 
    * These entities are now shared and can be used to build either a mathematical programming model or a constraint 
    programming model.
  * Update file `build.gradle`: replace dependency with `oplall.jar` by dependencies with `cplex.jar` and 
  `ILOG.CP.jar` instead.

## cplex-scala v1.4.0

  *  New examples `LearningCurve.scala` and `LearningCurve2`: a scheduling problem on two alternative heterogeneous machines with sequence 
  dependent setup times, forbidden transitions and a learning curve when changing of type
  * Fix bugs with parameters timeLimit and solutionLimit in method solve of CpModel
  * Port to CPLEX 12.9
  * Update copyright notice

## cplex-scala v1.3.0

  * Functional programming: add iterators for cumul function expression, state function, step function and piecewise 
  linear function. Furthermore, these functions are now iterables and this allow to iterate like 
  this:
  ```scala
     val f = model.numToNumStepFunction()
     ...
     for (s <- f) {
        println("[" + s.start + " .. " + s.end + ") -> " + s.value)
     }
  ```
  * Add type `IntSet`, factory methods and iterator.
  * Add scala API for inverse constraint and add example `Talent.scala`.
  * Port to CPLEX 12.8
  * Port to Scala 2.12.6
  * Add new signature for method alternative with only two parameters
  * Add method getFirst, getLast, getNext and getPrev in class CpModel to get rescpectively the first interval variable
  in the sequence, the last interval variable in the sequence, the next interval variable in the sequence, the previous
  interval variable in the sequence.
  * IntervalSequenceVariable is now a proper class. It implements the Trait Iterable: one can write something like this:
  ```scala
    val seq = model.intervalSequenceVar()
    ...
    for (x <- seq) {
      System.out.println(model.getDomain(x))
    }
  ```
  * Add example `SchedSetup.scala`: a scheduling problem on two alternative heterogeneous machines with sequence dependent setup
  times and forbidden transisions
  
  
## cplex-scala v1.2.0

### README

 * Add a link to IBM ILOG CPLEX Studio Community Edition download page in the README

### Constraint Programming

 * Add state function
 * and constraints `alwaysIn`, `alwaysEqual`, `alwaysConstant` and `alwaysNoState` on state functions
 * Add example `SchedState.scala`
 * Add missing precedence constraints in companion object of CpModel
 * Add constraints `alwaysEqual` on cumul functions
 * Add integer division on integer expression

## cplex-scala v1.1.0

### README

  * Use syntax highlighting for scala code blocks
  * Update README: specify version of Java JDK used for testing

### Constraint Programming

  * Add constraint forbidStart, forbidEnd, forbidExtent
  * Replace type NumToNumStepFunction with a class, define operators (+=, -=, *=, +, -...) and a proper toString method that prints the step function in a human readable format.
  * Add example SchedCalendar.
  
## cplex-scala v1.0.1

### README

  * Update math equation
  * Add support to IBM ILOG CPLEX 12.7. 
  * Update build.gradle to optionally configure maven repository.
  
## cplex-scala v1.0.0

Initial release of the scala library for IBM ILOG CPLEX.