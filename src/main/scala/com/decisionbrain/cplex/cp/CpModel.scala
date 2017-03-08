/*
 * Source file provided under Apache License, Version 2.0, January 2004,
 * http://www.apache.org/licenses/
 * (c) Copyright DecisionBrain SAS 2016,2017
 */

package com.decisionbrain.cplex.cp

import com.decisionbrain.cplex.Addable
import ilog.concert._
import ilog.cp.IloCP
import com.decisionbrain.cplex.cp.CpModel._

import scala.reflect.ClassTag


/**
  * Created by dgodard on 11/02/2017.
  */
class CpModel(name: String=null) {

  val cp = new IloCP()

  private val trueConstraint = cp.trueConstraint()
  private val falseConstraint = cp.falseConstraint()

  private val numExprNumeric = NumExprNumeric(this)
  private val intExprNumeric = IntExprNumeric(this)
  private val cumulFunctionExprNumeric = CumulFunctionExprNumeric(this)

  //
  // Members
  //

  /**
    * Returns the numeric for numeric expressions. This allows to do things such as calling method sum on list of
    * numeric expressions. For instance:
    * <pre>
    *   <code>
    *     implicit val num = model.getNumExprNumeric()
    *     val exprs = List(model.numVar(), model.numVar())
    *     val sumExpr = exprs.sum
    *   </code>
    * </pre>
    * @return the numeric for numeric expression
    */
  def getNumExprNumeric(): Numeric[NumExpr] = numExprNumeric

  /**
    * Returns the numeric for numeric expressions. This allows to do things such as calling method sum on list of
    * numeric expressions. For instance:
    * <pre>
    *   <code>
    *     implicit val num = model.getIntExprNumeric()
    *     val exprs = List(model.intVar(), model.intVar())
    *     val sumExpr = exprs.sum
    *   </code>
    * </pre>
    * @return the numeric for integer expression
    */
  def getIntExprNumeric(): Numeric[IntExpr] = intExprNumeric

  /**
    * Returns the numeric for cumul function expressions. This allows to do things such as calling method sum on list of
    * numeric expressions. For instance:
    * <pre>
    *   <code>
    *     implicit val num = model.getCumulFunctionExprNumeric()
    *     val exprs = List(model.pulse(0, 10, 1), model.pulse(0, 10, 2))
    *     val sumExpr = exprs.sum
    *   </code>
    * </pre>
    * @return the numeric for cumul function expression
    */
  def getCumulFunctionExprNumeric(): Numeric[CumulFunctionExpr] = cumulFunctionExprNumeric

  /**
    * Returns the name of the mathematical programming model.
    *
    * @return the name of the model
    */
  def getName(): Option[String] = Option(name)

  /**
    * Create an integer variable.
    *
    * @param min is the lower bound
    * @param max is the upper bound
    * @param name is the name of the variable
    * @return a numeric variable
    */
  def intVar(min: Int=0, max: Int=Int.MaxValue, name: String=null): IntVar =
    IntVar(cp.intVar(min, max, name))(implicitly(this))

  /**
    * Creates and returns a integer variable and initialize its domain with the given integer values.
    *
    * @param values are the integer values used to initialize the domain of the variable
    * @param name is the name of the variable
    * @return a integer variable
    */
  def intVar(values: IntArray, name: String): IntVar =
    IntVar(cp.intVar(values.toArray, name))(implicitly(this))

  /**
    * Creates and returns a integer variable and initialize its domain with the given integer values.
    *
    * @param values are the integer values used to initialize the domain of the variable
    * @param name is the name of the variable
    * @return a integer variable
    */
  def intVar(values: Array[Int], name: String): IntVar =
    IntVar(cp.intVar(values, name))(implicitly(this))

  /**
    * Creates and returns a integer variable and initialize its domain with the given integer values.
    *
    * @param values are the integer values used to initialize the domain of the variable
    * @return a integer variable
    */
  def intVar(values: IntArray): IntVar =
    IntVar(cp.intVar(values.toArray))(implicitly(this))

  /**
    * Creates and returns a integer variable and initialize its domain with the given integer values.
    *
    * @param values are the integer values used to initialize the domain of the variable
    * @return a integer variable
    */
  def intVar(values: Array[Int]): IntVar =
    IntVar(cp.intVar(values))(implicitly(this))

  /**
    * Create a numeric variable for each element in the set and add it in a dictionary
    * where the key is element of the set and the value is the numeric variable.
    *
    * @param set is the set
    * @param min is the minimum value
    * @param max is the maximum value
    * @param namer is a function that is used to set the name of a numeric variable
    * @tparam T it the type of the elements in the set
    * @return a dictionary of numeric variables indexed by the element of the set
    */
  def intVars[T](set: Iterable[T],
                 min: Int = 0,
                 max: Int= Int.MaxValue,
                 namer: (T) => String = (t: T) => "") : Map[T, IntVar] = {
    val dict: Map[T, IntVar] = set.map(t => {
      val v: IntVar = IntVar(cp.intVar(min, max, namer(t)))(implicitly(this))
      (t, v)
    })(collection.breakOut)
    dict
  }

  /**
    * Creates and returns a list of integer variables.
    *
    * @param count is the number of integer variables to be created
    * @param min is the minimum value of the integer variables
    * @param max is the maximum value of the integer variables
    * @param namer is a function to set the name of the integer variables
    * @return a list of integer variables
    */
  def intVars(count: Int,
              min: Int,
              max: Int,
              namer: (Int) => String) : List[IntVar] = {
    val vars = for (i <- 0 until count)
      yield IntVar(cp.intVar(min, max, namer(i)))(implicitly(this))
    vars.toList
  }

  /**
    * Creates and returns a list of integer variables.
    *
    * @param count is the number of integer variables to be created
    * @param min is the minimum value of the integer variables
    * @param max is the maximum value of the integer variables
    * @return a list of integer variables
    */
  def intVars(count: Int,
              min: Int,
              max: Int) : List[IntVar] = {
    val vars = for (i <- 0 until count)
      yield IntVar(cp.intVar(min, max))(implicitly(this))
    vars.toList
  }

  /**
    * This method creates an interval variable. By default, the start, the end and the size of the new interval variable
    * range from '0' to to 'IloCP.IntervalMax'. By default, the created interval variable is present but it can be made
    * optional by passing a value 'true' for parameter 'optional'. As no intensity function is specified, the size of
    * the interval variable will be equal to its length.
    *
    * @param startMin is the earliest start time of the interval variable
    * @param startMax is the latest start time of the interval variable
    * @param endMin is the earliest start time of the interval variable
    * @param endMax is the latest end time of the interval variable
    * @param sizeMin is the minimum size of the interval variable
    * @param sizeMax is the maximum size of the interval variable
    * @param optional is the optional status ot the interval variable
    *
    * @param name is the name of the inverval variable
    * @return an interval variable
    */
  def intervalVar(startMin: Int = 0,
                  startMax: Int = IloCP.IntervalMax,
                  endMin: Int = 0,
                  endMax: Int = IloCP.IntervalMax,
                  sizeMin: Int = 0,
                  sizeMax: Int = IloCP.IntervalMax,
                  optional: Boolean=false, name: String="") = {
    val v = cp.intervalVar
    v.setStartMin(startMin)
    v.setStartMax(startMax)
    v.setEndMin(endMin)
    v.setEndMax(endMax)
    v.setSizeMin(sizeMin)
    v.setSizeMax(sizeMax)
    if (optional) v.setOptional()
    v.setName(name)
    IntervalVar(v)(implicitly(this))
  }

  /**
    * This method creates an interval variable. The start and end of the new interval variable range from 0 to the
    * constant IloCP.IntervalMax. The size of the new interval variable is fixed and equal to 'size'. The created
    * interval variable is present; if you need an optional interval variable, you need to use the member function
    * setOptional. As no intensity function is specified, the size of the interval variable will be equal to its length.
    *
    * @param size is the size of the interval variable
    * @return a new interval variable
    */
  def intervalVar(size: Int): IntervalVar =
    IntervalVar(cp.intervalVar(size))(implicitly(this))

  /**
    * This method creates an interval variable. The start and end of the new interval variable range from 0 to the
    * constant IloCP.IntervalMax. The size of the new interval variable is fixed and equal to 'size'. The created
    * interval variable is present; if you need an optional interval variable, you need to use the member function
    * setOptional. As no intensity function is specified, the size of the interval variable will be equal to its length.
    *
    * @param size is the size of the interval variable
    * @param name is the name of the interval variable
    * @return a new interval variable
    */
  def intervalVar(size: Int, name: String): IntervalVar =
    IntervalVar(cp.intervalVar(size, name))(implicitly(this))

  /**
    * Creates a dictionary of interval variables indexed by objects of type T.
    *
    * @param keys is a set of objects of type T
    * @param startMin is the earliest start time of the interval variables
    * @param startMax is the latest start time of the interval variables
    * @param endMin is the earlist end time of the interval variables
    * @param endMax is the latest end time of the interval variables
    * @param sizeMin is the minimum size of the interval variables
    * @param sizeMax is the maximum size of the interval variables
    * @param optional is a boolean, if true the interval variables are optional
    * @param namer is a function that takes as parameter an object of type t and return a character string
    * @tparam T is a type of the object
    * @return a dictionary of interval variables
    */
  def intervalVars[T](keys: Iterable[T],
                      startMin: Int = 0,
                      startMax: Int = IloCP.IntervalMax,
                      endMin: Int = 0,
                      endMax: Int = IloCP.IntervalMax,
                      sizeMin: Int = 0,
                      sizeMax: Int = IloCP.IntervalMax,
                      optional: Boolean = false,
                      namer: (T) => String = (t: T) => "") : Map[T, IntervalVar] = {
    val dict: Map[T, IntervalVar] = keys.map(t => {
      val v: IntervalVar = intervalVar(startMin, startMax, endMin, endMax, sizeMin, sizeMax, optional, namer(t))
      (t, v)
    })(collection.breakOut)
    dict
  }

  /**
    * Returns a constraint that is always true or false.
    *
    * @return a constraint that is always true
    */
  def constraint(value: Boolean=true): Constraint = {
    if (value)
      Constraint(trueConstraint)(implicitly(this))
    else
      Constraint(falseConstraint)(implicitly(this))
  }

  /**
    * This method creates an interval variable. The start and end of the new interval variable range from 0 to the
    * constant IloCP.IntervalMax. The size of the new interval variable ranges from szmin to szmax. By default, the
    * created interval variable is present but it can be made optional by passing a true value (true) for opt. The
    * intensity function is intensity. This intensity function is an integer step function expressed as a percentage
    * (and so must bounded in the range [0,100]).
    *
    * @param sizeMin is the minimum size of the interval variable
    * @param sizeMax is the maximum size of the interval variable
    * @param optional is the optional status of the interval variable
    * @param intensity is the intensity function of the interval variable
    * @param granularity is the granularity of the intensity function
    * @return a new interval variable
    */
  def intervalVar(sizeMin: Int,
                  sizeMax: Int,
                  optional: Boolean,
                  intensity: IloNumToNumStepFunction,
                  granularity: Int): IntervalVar =
    IntervalVar(cp.intervalVar(sizeMin, sizeMax, optional, intensity, granularity))(implicitly(this))

  /**
    * Return the sum of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the sum of the numeric expressions
    */
  def sum(exprs: NumExprArray) : NumExpr = {
    NumExpr(cp.sum(exprs.toIloArray))(implicitly(this))
  }

  /**
    * Return the sum of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the sum of the numeric expressions
    */
  def sum(exprs: Array[NumExpr]) : NumExpr = {
    NumExpr(cp.sum(exprs.map(e => e.getIloNumExpr())))(implicitly(this))
  }

  /**
    * Return the sum of integer expressions.
    *
    * @param exprs is a sequence of integer variables
    * @return a numeric expression that represents the sum of the integer expressions
    */
  def sum(exprs: Array[IntExpr]) : IntExpr = {
    IntExpr(cp.sum(exprs.map(e => e.getIloIntExpr)))(implicitly(this))
  }

  /**
    * Returns the integer sum of integer expressions.
    *
    * @param exprs is a sequence of integer variables
    * @return an integer expression that represents the sum of the integer expressions
    */
  def sumi(exprs: IntExprArray) : IntExpr = {
    IntExpr(cp.sum(exprs.toIloArray))(implicitly(this))
  }

  /**
    * Returns the maximum of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the maximum of the numeric expressions
    */
  def max(exprs: NumExprArray) : NumExpr = {
    NumExpr(cp.max(exprs.toIloArray))(implicitly(this))
  }

  /**
    * Returns the minimum of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the minimum of the numeric expressions
    */
  def min(exprs: NumExprArray) : NumExpr = {
    NumExpr(cp.max(exprs.toIloArray))(implicitly(this))
  }

  /**
    * This function creates a new constrained integer expression equal to the number of integer variables that are
    * fixed to the value v.
    *
    * @param exprs is the array of integer expressions
    * @param v is the value
    * @return an integer expression equal to the number of variables equal to the given value
    */
  def count(exprs: IntExprArray, v: Int): IntExpr = {
    IntExpr(cp.count(exprs.toIloArray, v))(implicitly(this))
  }

  /**
    * This function creates a new constrained integer expression equal to the number of integer variables that are
    * fixed to the value v.
    *
    * @param exprs is the array of integer expressions
    * @param v is the value
    * @return an integer expression equal to the number of variables equal to the given value
    */
  def count(exprs: Array[IntExpr], v: Int): IntExpr = {
    IntExpr(cp.count(exprs.map(e => e.getIloIntExpr()), v))(implicitly(this))
  }

  /**
    * Creates and returns an integer linear expression representing the scalar product of the given integer values
    * with the given integer variables.
    *
    * @param values is the sequence of values
    * @param vars is the sequence of variables
    * @return the scalar product of integer values with integer variables
    */
  def scalarProduct(values: IntArray, vars: IntVarArray): IntExpr =
    IntExpr(cp.scalProd(vars.toIloArray, values.toArray))(implicitly(this))

  /**
    * Creates and returns an integer linear expression representing the scalar product of the given integer values
    * with the given integer variables.
    *
    * @param values is the sequence of values
    * @param vars is the sequence of variables
    * @return the scalar product of integer values with integer variables
    */
  def scalarProduct(values: Array[Int], vars: Array[IntVar]): IntExpr =
    IntExpr(cp.scalProd(vars.map(v => v.getIloIntVar()), values))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of values and exps, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param exprs is the sequence of integer expressions
    * @param values is the sequence of integer values
    * @return the scalar product of integer values with integer expressions
    */
  def prod(values: IntArray, exprs: IntExprArray): IntExpr =
    IntExpr(cp.prod(exprs.toIloArray, values.toArray))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of values and exps, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param exprs is the sequence of integer expressions
    * @param values is the sequence of integer values
    * @return the scalar product of integer values with integer expressions
    */
  def prod(values: Array[Int], exprs: Array[IntExpr]): IntExpr =
    IntExpr(cp.prod(exprs.map(e => e.getIloIntExpr()), values))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps1 is the array of integer expressions
    * @param exps2 is the array of integer expressions
    * @return the scalar product of two arrays of integer expressions
    */
  def prod(exps1: IntExprArray, exps2: IntExprArray): IntExpr =
    IntExpr(cp.prod(exps1.toIloArray, exps2.toIloArray))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps1 is the array of integer expressions
    * @param exps2 is the array of integer expressions
    * @return the scalar product of two arrays of integer expressions
    */
  def prod(exps1: Array[IntExpr], exps2: Array[IntExpr]): IntExpr =
    IntExpr(cp.prod(exps1.map(e => e.getIloIntExpr()), exps2.map(e => e.getIloIntExpr())))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps is the array of integer expressions
    * @param values is the array of integer values
    * @return the scalar product of integer expressions with integer values
    */
  def prod(exps: IntExprArray, values: IntArray): IntExpr =
    IntExpr(cp.prod(exps.toIloArray, values.toArray))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps is the array of integer expressions
    * @param values is the array of integer values
    * @return the scalar product of integer expressions with integer values
    */
  def prod(exps: Array[IntExpr], values: Array[Int]): IntExpr =
    IntExpr(cp.prod(exps.map(e => e.getIloIntExpr()), values))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is the array of integer values
    * @param exps is the array of integer expressions
    * @return the scalar product of integer expressions with integer values
    */
  def prod(values: NumArray, exps: NumExprArray): NumExpr =
    NumExpr(cp.prod(values.toArray, exps.toIloArray))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is the array of integer values
    * @param exps is the array of integer expressions
    * @return the scalar product of integer expressions with integer values
    */
  def prod(values: Array[Double], exps: Array[NumExpr]): NumExpr =
    NumExpr(cp.prod(values, exps.map(e => e.getIloNumExpr)))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is the array of numeric values
    * @param exps is the array of numeric expressions
    * @return the scalar product of numeric expressions with numeric values
    */
  def prod(exps: NumExprArray, values: NumArray): NumExpr =
    NumExpr(cp.prod(exps.toIloArray, values.toArray))(implicitly(this))

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is the array of numeric values
    * @param exps is the array of numeric expressions
    * @return the scalar product of numeric expressions with numeric values
    */
  def prod(exps: Array[NumExpr], values: Array[Double]): NumExpr =
    NumExpr(cp.prod(exps.map(e => e.getIloNumExpr), values))(implicitly(this))

  /**
    *  Creates and returns a new integer expression equal to values[index] where index is an integer expression.
    *
    * @param values is an array of integer values
    * @param index is an integer expression
    * @return a integer expression equals to values[index]
    */
  def element(values: IntArray, index: IntExpr): IntExpr =
    IntExpr(cp.element(values.toArray, index.getIloIntExpr()))(implicitly(this))

  /**
    *  Creates and returns a new integer expression equal to values[index] where index is an integer expression.
    *
    * @param values is an array of integer values
    * @param index is an integer expression
    * @return a integer expression equals to values[index]
    */
  def element(values: Array[Int], index: IntExpr): IntExpr =
    IntExpr(cp.element(values, index.getIloIntExpr()))(implicitly(this))

  /**
    *  Creates and returns a new integer expression equal to exprs[index] where index is an integer expression.
    *
    * @param exprs is an array of integer expressions
    * @param index is an integer expression of the index
    * @return the integer expression equals to exprs[index]
    */
  def element(exprs: IntExprArray, index: IntExpr): IntExpr =
    IntExpr(cp.element(exprs.toIloArray, index.getIloIntExpr()))(implicitly(this))

  /**
    *  Creates and returns a new integer expression equal to exprs[index] where index is an integer expression.
    *
    * @param exprs is an array of integer expressions
    * @param index is an integer expression of the index
    * @return the integer expression equals to exprs[index]
    */
  def element(exprs: Array[IntExpr], index: IntExpr): IntExpr =
    IntExpr(cp.element(exprs.map(e => e.getIloIntExpr()), index.getIloIntExpr()))(implicitly(this))

  /**
    *  Creates and returns a new integer expression equal to exprs[index] where index is an integer expression.
    *
    * @param values is an array of numeric values
    * @param index is an integer expression of the index
    * @return the numeric expression equals to exprs[index]
    */
  def element(values: NumArray, index: IntExpr): NumExpr =
    NumExpr(cp.element(values.toArray, index.getIloIntExpr()))(implicitly(this))

  /**
    * Creates and returns a new constraint stating that the integer expression must all take different values.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param exprs is an array of integer expressions
    * @return a new 'all different' constraint
    */
  def allDiff(exprs: IntExprArray): Constraint =
    Constraint(cp.allDiff(exprs.toIloArray))(implicitly(this))

  /**
    * Creates and returns a new constraint stating that the integer expression must all take different values.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param exprs is an array of integer expressions
    * @return a new 'all different' constraint
    */
  def allDiff(exprs: Array[IntExpr]): Constraint =
    Constraint(cp.allDiff(exprs.map(e => e.getIloIntExpr()).toArray))(implicitly(this))

  /**
    * Creates and returns a new constraint based on explicitly stating the allowed assignments for a small group of
    * variables. The allowed assignments are the values that satisfy the constraint; the argument table specifies the
    * combinations of allowed values of the variables, exps. The order of the constrained variables in the array vars
    * is important because the same order is respected in the set. To avoid exceptions, the size of vars must be the
    * same as the arity of the set.
    *
    * @param vars is an array of integer variables
    * @param values is a set of integer values
    * @return a new 'allowed-assignment' constraint
    */
  def allowedAssignments(vars: IntVarArray, values: Iterable[IntArray])(implicit model: CpModel) = {
    val table = cp.intTable(vars.size)
    for (t <- values) cp.addTuple(table, t.toArray)
    Constraint(cp.allowedAssignments(vars.toIloArray, table))(implicitly(this))
  }

  /**
    * Creates and returns a new constraint based on explicitly stating the allowed assignments for a small group of
    * variables. The allowed assignments are the values that satisfy the constraint; the argument table specifies the
    * combinations of allowed values of the variables, exps. The order of the constrained variables in the array vars
    * is important because the same order is respected in the set. To avoid exceptions, the size of vars must be the
    * same as the arity of the set.
    *
    * @param exps is an array of integer variables
    * @param values is a set of integer values
    * @return a new 'allowed-assignment' constraint
    */
  def allowedAssignments(exps: Array[IntVar], values: Iterable[Array[Int]])(implicit model: CpModel) = {
    val vars = exps.map(v => v.getIloIntVar())
    val table = cp.intTable(vars.length)
    for (t <- values) cp.addTuple(table, t)
    Constraint(cp.allowedAssignments(vars, table))(implicitly(this))
  }

  /**
    * Creates and returns a pack constraint which maintains the load of a set of containers or bins, given a set of
    * weighted items and an assignment of items to containers. Consider that we have n items and m containers. Each
    * item i has an integer weight weight[i] and a constrained integer variable where[i] associated with it, indicating
    * in which container (numbered contiguously from 0) item i is to be placed. No item can be split up, and so an item
    * can go in only one container. Associated with each container j is an integer variable load[j] representing the
    * load in that container; that is, the sum of the weights of the items which have been assigned to that container.
    * A capacity can be set for each container placing an upper bound on this load variable. The constraint also ensures
    * that the total sum of the loads of the containers is equal to the sum of the weights of the items being placed.
    *
    * Finally, the number, or indeed the set of containers used can be specified by the integer expression used. A
    * container is used if at least one item is placed in the container in question.
    *
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param load
    * @param where
    * @param weights
    * @param used
    * @return
    */
  def pack(load: IntExprArray, where: IntExprArray, weights: IntArray, used: IntExpr): Constraint = {
    val l = load.toIloArray
    val w = where.toIloArray
    val v = weights.toArray
    val u = used.getIloIntExpr()
    Constraint(cp.pack(l, w, v, u))(implicitly(this))
  }

  /**
    * Creates and returns a pack constraint which maintains the load of a set of containers or bins, given a set of
    * weighted items and an assignment of items to containers. Consider that we have n items and m containers. Each
    * item i has an integer weight weight[i] and a constrained integer variable where[i] associated with it, indicating
    * in which container (numbered contiguously from 0) item i is to be placed. No item can be split up, and so an item
    * can go in only one container. Associated with each container j is an integer variable load[j] representing the
    * load in that container; that is, the sum of the weights of the items which have been assigned to that container.
    * A capacity can be set for each container placing an upper bound on this load variable. The constraint also ensures
    * that the total sum of the loads of the containers is equal to the sum of the weights of the items being placed.
    *
    * Finally, the number, or indeed the set of containers used can be specified by the integer expression used. A
    * container is used if at least one item is placed in the container in question.
    *
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param load
    * @param where
    * @param weights
    * @param used
    * @return
    */
  def pack(load: Array[IntExpr], where: Array[IntExpr], weights: Array[Int], used: IntExpr): Constraint = {
    val l = load.map(e => e.getIloIntExpr()).toArray
    val w = where.map(e => e.getIloIntExpr()).toArray
    val v = weights.toArray
    val u = used.getIloIntExpr()
    Constraint(cp.pack(l, w, v, u))(implicitly(this))
  }

  /**
    * This function returns a constraint that states that interval variable a is present. Typically, this constraint is
    * used in combination with other constraints.
    *
    * @param v
    * @return
    */
  def presenceOf(v: IntervalVar): Constraint =
    Constraint(cp.presenceOf(v.getIloIntervalVar()))(implicitly(this))

  /**
    * This function returns an integer expression that represents the start of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value absVal.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the start of the interval variable
    */
  def startOf(a: IntervalVar, absValue: Int=0): IntExpr =
    IntExpr(cp.startOf(a.getIloIntervalVar(), absValue))(implicitly(this))

  /**
    * This function returns an integer expression that represents the end of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the end of the interval variable
    */
  def endOf(a: IntervalVar, absValue: Int=0): IntExpr =
    IntExpr(cp.endOf(a.getIloIntervalVar(), absValue))(implicitly(this))

  /**
    * This function returns an integer expression that represents the length of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the length of the interval variable
    */
  def lengthOf(a: IntervalVar, absValue: Int=0): IntExpr =
    IntExpr(cp.lengthOf(a.getIloIntervalVar(), absValue))(implicitly(this))

  /**
    * This function returns an integer expression that represents the size of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the size of the interval variable
    */
  def sizeOf(a: IntervalVar, absValue: Int=0): IntExpr =
    IntExpr(cp.sizeOf(a.getIloIntervalVar(), absValue))(implicitly(this))

  /**
    * This function returns an integer expression that represents the type of the interval variable that is next to
    * interval 'a' in sequence variable 'seq'. When interval 'a' is present and is the last interval of sequence 'seq',
    * it returns the constant integer value 'lastVal'. When interval a is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last interval variable present in the sequence
    * @param absVal is the value returned is the interval variable is absent
    * @return an interger expression that represents the type of the next interval variable in the sequence
    */
  def typeOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.typeOfNext(seq, a.getIloIntervalVar(), lastVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the type of the interval variable that is previous to
    * interval 'a' in sequence variable 'seq'. When interval 'a' is present and is the first interval of sequence 'seq',
    * it returns the constant integer value 'firstVal'. When interval a is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the first interval variable present in the
    *                 sequence
    * @param absVal is the value returned is the interval variable is absent
    * @return an interger expression that represents the type of the previous interval variable in the sequence
    */
  def typeOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.typeOfPrevious(seq, a.getIloIntervalVar(), firstVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the start of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the start of the next interval variable in the sequence
    */
  def startOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.startOfNext(seq, a.getIloIntervalVar(), lastVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the start of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the start of the previous interval variable in the sequence
    */
  def startOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.startOfPrevious(seq, a.getIloIntervalVar(), firstVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the end of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the end of the next interval variable in the sequence
    */
  def endOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.endOfNext(seq, a.getIloIntervalVar(), lastVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the end of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the end of the previous interval variable in the sequence
    */
  def endOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.endOfPrevious(seq, a.getIloIntervalVar(), firstVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the size of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the size of the next interval variable in the sequence
    */
  def sizeOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.sizeOfNext(seq, a.getIloIntervalVar(), lastVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the size of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the size of the previous interval variable in the sequence
    */
  def sizeOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.sizeOfPrevious(seq, a.getIloIntervalVar(), firstVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the length of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the length of the next interval variable in the sequence
    */
  def lengthOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.lengthOfNext(seq, a.getIloIntervalVar(), lastVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the length of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the length of the previous interval variable in the sequence
    */
  def lengthOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0): IntExpr =
    IntExpr(cp.lengthOfPrevious(seq, a.getIloIntervalVar(), firstVal, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the length of the overlap of interval variable a1
    * and interval variable a2 whenever interval variables a1 and a2 are present. When interval variable a1 or a2 is
    * absent, the function returns the constant integer value absVal.
    *
    * @param a1 is the first interval variable
    * @param a2 is the second interval variable
    * @param absVal is the value returned if interval variable a1 or a1 is absent
    * @return an integer expression of the overlap of two interval variables
    */
  def overlapLength(a1: IntervalVar, a2: IntervalVar, absVal: Int=0): IntExpr =
    IntExpr(cp.overlapLength(a1.getIloIntervalVar(), a2.getIloIntervalVar(), absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the length of the overlap of interval variable a and
    * constant interval [start, end) whenever interval variable a is present. When interval variable a is absent, the
    * function returns the constant integer value absVal.
    *
    * @param a is the interval variable
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @param absVal is the value returned if the inverval variable is absent
    * @return an integer expression of the overlap of an interval variable and a constant interval
    */
  def overlapLength(a: IntervalVar, start: Int, end: Int, absVal: Int): IntExpr =
    IntExpr(cp.overlapLength(a.getIloIntervalVar(), start, end, absVal))(implicitly(this))

  /**
    * This function returns an integer expression that represents the length of the overlap of interval variable a and
    * constant interval [start, end) whenever interval variable a is present. When interval variable a is absent, the
    * function returns the constant integer value 0.
    *
    * @param a is the interval variable
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @return an integer expression of the overlap of an interval variable and a constant interval
    */
  def overlapLength(a: IntervalVar, start: Int, end: Int): IntExpr =
    IntExpr(cp.overlapLength(a.getIloIntervalVar(), start, end))(implicitly(this))

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the start of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the start of the interval variable
    */
  def startEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0): NumExpr =
    NumExpr(cp.startEval(a.getIloIntervalVar(), f, absVal))(implicitly(this))

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the end of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the end of the interval variable
    */
  def endEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0): NumExpr =
    NumExpr(cp.endEval(a.getIloIntervalVar(), f, absVal))(implicitly(this))

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the length of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the length of the interval variable
    */
  def lengthEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0): NumExpr =
    NumExpr(cp.lengthEval(a.getIloIntervalVar(), f, absVal))(implicitly(this))

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the size of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the size of the interval variable
    */
  def sizeEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0): NumExpr =
    NumExpr(cp.sizeEval(a.getIloIntervalVar(), f, absVal))(implicitly(this))

  /**
    * This method creates a no-overlap constraint on the set of interval variables defined by array a.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param vars is the sef of interval variables
    * @return a no-overlap constraint
    */
  def noOverlap(vars: IntervalVarArray): Constraint =
    Constraint(cp.noOverlap(vars.toIloArray))(implicitly(this))

  /**
    * This method creates a no-overlap constraint on the set of interval variables defined by array a.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param vars is the sef of interval variables
    * @return a no-overlap constraint
    */
  def noOverlap(vars: Array[IntervalVar]): Constraint =
    Constraint(cp.noOverlap(vars.map(v => v.getIloIntervalVar())))(implicitly(this))


  /**
    * This method creates a no-overlap constraint on the sequence variable seq. This constraint states that the
    * interval variables of the sequence do not overlap and that the order of intervals in the sequence is the order
    * implied by the relative position of the start and end points of the non-overlapping intervals. A transition
    * distance tdist is used to specify a minimal distance between two interval variables in the sequence.
    * The transition distance holds between an interval and all its successors in the sequence.
    *
    * @param seq is the sequence variable
    * @param tdist is the transition distance
    * @return a no-overlap constraint
    */
  def noOverlap(seq: IntervalSequenceVar, tdist: TransitionDistance=null, direct: Boolean=false): Constraint =
    if (Option(tdist).isEmpty)
      Constraint(cp.noOverlap(seq))(implicitly(this))
    else
      Constraint(cp.noOverlap(seq, tdist, direct))(implicitly(this))

  /**
    * This method creates a span constraint between interval variable a and the set of interval variables bs.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is the interval variable
    * @param bs is the set of interval variables
    * @return a span constraint
    */
  def span(a: IntervalVar, bs: IntervalVarArray): Constraint =
    Constraint(cp.span(a.getIloIntervalVar(), bs.toIloArray))(implicitly(this))

  /**
    * This method creates a span constraint between interval variable a and the set of interval variables bs.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is the interval variable
    * @param bs is the set of interval variables
    * @return a span constraint
    */
  def span(a: IntervalVar, bs: Array[IntervalVar]): Constraint =
    Constraint(cp.span(a.getIloIntervalVar(), bs.map(b=>b.getIloIntervalVar())))(implicitly(this))

  /**
    * This method creates a constant cumul function expression everywhere equal to 0 that the user can modify
    * subsequently with the functions add, sub.
    *
    * @return a cumul funciton expression
    */
  def cumulFunctionExpr(v: Int=0, name: String=null): CumulFunctionExpr = {
    val expr = cp.cumulFunctionExpr(name)
    if (v != 0) {
      val expr = cp.pulse(IloCP.IntervalMin, IloCP.IntervalMax, v)
      expr.setName(name)
      CumulFunctionExpr(expr)(implicitly(this))
    }
    else
      CumulFunctionExpr(cp.cumulFunctionExpr(name))(implicitly(this))
  }

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance start(b)-start(a) between the start of interval a and the start of interval b must be greater than or
    * equal to z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the start of b and the start of a
    * @return a precedence constraint
    */
  def startBeforeStart(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.startBeforeStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance start(b)-start(a) between the start of interval a and the start of interval b must be greater than or
    * equal to z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the start of b and the start of a
    * @return a precedence constraint
    */
  def startBeforeStart(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.startBeforeStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance end(b)-start(a) between the end of interval a and the start of interval b must be greater than or
    * equal to z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the start of b and the start of a
    * @return a precedence constraint
    */
  def startBeforeEnd(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.startBeforeEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance end(b)-start(a) between the start of interval a and the end of interval b must be greater than or
    * equal to z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the start of b and the start of a
    * @return a precedence constraint
    */
  def startBeforeEnd(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.startBeforeEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance start(b)-end(a) between the start of interval a and the end of interval b must be greater than or
    * equal to z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the end of b and the start of a
    * @return a precedence constraint
    */
  def endBeforeStart(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.endBeforeStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance start(b)-end(a) between the end of interval a and the start of interval b must be greater than or
    * equal to integer expression z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the start of b and the end of a
    * @return a precedence constraint
    */
  def endBeforeStart(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.endBeforeStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance end(b)-end(a) between the end of interval a and the start of interval b must be greater than or
    * equal to z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the end of b and the end of a
    * @return a precedence constraint
    */
  def endBeforeEnd(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.endBeforeEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance end(b)-end(a) between the end of interval a and the start of interval b must be greater than or
    * equal to z.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @param z is the minimum delay the end of b and the end of a
    * @return a precedence constraint
    */
  def endBeforeEnd(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.endBeforeEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present, the
    * distance start(b)-start(a)between the start of interval a and the start of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.

    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def startAtStart(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.startAtStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present, the
    * distance start(b)-start(a)between the start of interval a and the start of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.

    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def startAtStart(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.startAtStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present,
    * the distance end(b)-start(a)between the start of interval a and the end of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.

    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def startAtEnd(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.startAtEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present,
    * the distance end(b)-start(a)between the start of interval a and the end of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.

    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def startAtEnd(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.startAtEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present, the
    * distance start(b)-end(a)between the end of interval a and the start of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.

    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def endAtStart(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.endAtStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present, the
    * distance start(b)-end(a)between the end of interval a and the start of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def endAtStart(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.endAtStart(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present, the
    * distance end(b)-end(a)between the end of interval a and the end of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.

    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def endAtEnd(a: IntervalVar, b: IntervalVar, z: Int = 0): Constraint =
    Constraint(cp.endAtEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z))(implicitly(this))

  /**
    * This function returns a constraint that states that whenever both interval variables a and b are present, the
    * distance end(b)-end(a)between the end of interval a and the end of interval b must be equal to z.
    *
    * Note: This constraint cannot be used in a logical constraint.

    * @param a is the first interval variable
    * @param b is the second interval variable
    * @param z is the minimum delay
    * @return a precedence constraint
    */
  def endAtEnd(a: IntervalVar, b: IntervalVar, z: IntExpr): Constraint =
    Constraint(cp.endAtEnd(a.getIloIntervalVar(), b.getIloIntervalVar(), z.getIloIntExpr()))(implicitly(this))

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is a set of interval variables
    * @param value is the cardinality
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: IntervalVarArray, value: Int=1, name: String=null) = {
    val c = cp.alternative(a.getIloIntervalVar(), bs.toIloArray, value)
    c.setName(name)
    Constraint(c)(implicitly(this))
  }

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is a set of interval variables
    * @param value is the cardinality
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: Array[IntervalVar], value: Int, name: String) = {
    val c = cp.alternative(a.getIloIntervalVar(), bs.map((v) => v.getIloIntervalVar()).toArray, value)
    c.setName(name)
    Constraint(c)(implicitly(this))
  }

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is a set of interval variables
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: Array[IntervalVar], name: String) = {
    val c = cp.alternative(a.getIloIntervalVar(), bs.map((v) => v.getIloIntervalVar()))
    c.setName(name)
    Constraint(c)(implicitly(this))
  }

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is a set of interval variables
    * @param value is the cardinality
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: Array[IntervalVar], value: Int) = {
    val c = cp.alternative(a.getIloIntervalVar(), bs.map((v) => v.getIloIntervalVar()), value)
    Constraint(c)(implicitly(this))
  }

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to v between the start and the end of interval variable a and equal to 0 everywhere else. When interval
    * variable a is absent, the function is the constant nul function.
    *
    * @param a is the interval variable
    * @param v is the value of the cumul function on interval 'a'
    * @return a cumul function expression
    */
  def pulse(a: IntervalVar, v: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.pulse(a.getIloIntervalVar(), v))(implicitly(this))

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to a value 'v' such that 'vmin <= v <= vmax' everywhere between the start and the end of interval variable
    * 'a' and equal to 0 everywhere else. The choice of the value v in the range [vmin,vmax] is a decision of the
    * problem. When interval variable 'a' is absent, the function is the constant nul function.
    *
    * @param a is the interval variable
    * @param vmin is the minimum value of the cumul function on interval 'a' if present
    * @param vmax is the maximum value of the cumul function on interval 'a' is present
    * @return a cumul function expression
    */
  def pulse(a: IntervalVar, vmin: Int, vmax: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.pulse(a.getIloIntervalVar(), vmin, vmax))(implicitly(this))

  /**
    * This function returns an elementary cumul function expression that is equal to 'v' between 'start' and 'end' and
    * equal to 0 everywhere else.
    *
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @param v is the value of the cumul function on interval [start..end)
    * @return a cumul function expression
    */
  def pulse(start: Int, end: Int, v: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.pulse(start, end, v))(implicitly(this))

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to 0 before the start of a and equal to v after the start of a. When interval variable a is absent, the
    * function is the constant nul function.
    *
    * @param v is the interval variable
    * @param value is the value of the cumul funciton on the interval variable
    * @return a cumul function expression
    */
  def stepAtStart(v: IntervalVar, value: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.stepAtStart(v.getIloIntervalVar(), value))(implicitly(this))

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to a 0 before the start of a and equal to a value v such that vmin <= v <= vmax after the start of a. The
    * choice of the value v in the range [vmin,vmax] is a decision of the problem. When interval variable a is absent,
    * the function is the constant nul function.
    *
    * @param v is the interval variable
    * @param vmin is the minimum value of the cumul function on the interval variable
    * @param vmax is the maximum valyue of the cumul function on the interval variable
    * @return a cumul function expression
    */
  def stepAtStart(v: IntervalVar, vmin: Int, vmax: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.stepAtStart(v.getIloIntervalVar(), vmin, vmax))(implicitly(this))

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to 0 before the end of a and equal to v after the end of a. When interval variable a is absent, the
    * function is the constant nul function.
    *
    * @param v is the interval variable
    * @param value is the value of the cumul funciton on the interval variable
    * @return a cumul function expression
    */
  def stepAtEnd(v: IntervalVar, value: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.stepAtEnd(v.getIloIntervalVar(), value))(implicitly(this))

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to a 0 before the end of a and equal to a value v such that vmin <= v <= vmax after the end of a. The
    * choice of the value v in the range [vmin,vmax] is a decision of the problem. When interval variable a is absent,
    * the function is the constant nul function.
    *
    * @param v is the interval variable
    * @param vmin is the minimum value of the cumul function on the interval variable
    * @param vmax is the maximum value of the cuml function on the interval variable
    * @return a cumul function expression
    */
  def stepAtEnd(v: IntervalVar, vmin: Int, vmax: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.stepAtEnd(v.getIloIntervalVar(), vmin, vmax))(implicitly(this))

  /**
    * This function returns an elementary cumul function expression that is equal to 0 before point t and equal to v
    * after point t.
    *
    * @param t is the time point
    * @param v is the value of the cumul function at time t
    * @return a cumul function expression
    */
  def step(t: Int, v: Int): CumulFunctionExpr =
    CumulFunctionExpr(cp.step(t, v))(implicitly(this))

  /**
    * Returns a cumul function expressions that is the sum of a set of cumul function expressions.
    *
    * @param exprs the set of cumul function expressions
    * @return the sum of the cumul function expressions
    */
  def sum(exprs: Array[CumulFunctionExpr]): CumulFunctionExpr = {
    var s = CumulFunctionExpr(cp.cumulFunctionExpr())(implicitly(this))
    for (expr <- exprs) {
      s += expr
    }
    s
  }

  /**
    * Returns a cumul function expressions that is the sum of a set of cumul function expressions.
    *
    * @param exprs the set of cumul function expressions
    * @return the sum of the cumul function expressions
    */
  def sum(exprs: CumulFunctionExprArray): CumulFunctionExpr = {
    sum(exprs.toArray)
  }

  /**
    * This function returns a constraint that states that the value of cumul function expression f should be always
    * within the range [vmin,vmax] between start and end.
    *
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param f is the cumul function expression
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @param vmin is the minimum value of the function
    * @param vmax is the maximum value of the function
    * @return a new constraint on the value of the function
    */
  def alwaysIn(f: CumulFunctionExpr, start: Int, end: Int, vmin: Int, vmax: Int): Constraint =
    Constraint(cp.alwaysIn(f.getIloCumulFunctionExpr(), start, end, vmin, vmax))(implicitly(this))

  /**
    * Add an addable object in the model.
    *
    * @param a is the object to add to the model
    * @return the model
    */
  def add(a: Addable, name: String=null): CpModel = {
    a.setName(name)
    cp.add(a.getIloAddable())
    this
  }

  /**
    * Creates and returns an objective object to minimize the expression <em>expr</em>.
    *
    * @param expr is the expression to minimize
    * @return An objective object representing the objective to minimize
    */
  def minimize(expr: NumExpr): Objective = Objective(cp.minimize(expr.getIloNumExpr))(implicitly(this))

  /**
    * Creates a minimization multicriteria objective.
    *
    * @param expr is the multicriteria expressions
    * @return an objective
    */
  def minimize(expr: MultiCriterionExpr): Objective = Objective(cp.minimize(expr))(implicitly(this))

  /**
    * Creates and returns an objective object to maximize the expression <em>expr</em>.
    *
    * @param expr is the expression to maximize
    * @return An objective object the objective to maximize
    */
  def maximize(expr: NumExpr): Objective = Objective(cp.maximize(expr.getIloNumExpr))(implicitly(this))

  /**
    * Creates a maximization multicriteria objective.
    *
    * @param expr is the multicriteria expressions
    * @return an objective
    */
  def maximize(expr: MultiCriterionExpr): Objective = Objective(cp.maximize(expr))(implicitly(this))

  /**
    * This function defines a multicriteria expression for lexicographic ordering. A lexicographic ordering means that
    * any improvement of the i-th criterion is more important than any improvement of the subsequent criteria.
    *
    * @param exprs a set of integer expressions for the lexicographic ordering
    */
  def staticLex(exprs: NumExpr*): MultiCriterionExpr = {
    cp.staticLex(exprs.map(e => e.getIloNumExpr()).toArray)
  }

  /**
    * This method creates a search phase with a set of variables only. The variable and value choosers for these
    * variables will be chosen by CP Optimizer search automatically.
    *
    * @param vars are the variables of the search phase
    * @return a search phase
    */
  def searchPhase(vars: IntVarArray): SearchPhase = {
    SearchPhase(cp.searchPhase(vars.toIloArray))(implicitly(this))
  }

  /**
    * This method creates a search phase with a set of variables only. The variable and value choosers for these
    * variables will be chosen by CP Optimizer search automatically.
    *
    * @param vars are the variables of the search phase
    * @return a search phase
    */
  def searchPhase(vars: Array[IntVar]): SearchPhase = {
    SearchPhase(cp.searchPhase(vars.map(v => v.getIloIntVar).toArray))(implicitly(this))
  }

  /**
    * This method creates a search phase with a set of variables only. The variable and value choosers for these
    * variables will be chosen by CP Optimizer search automatically.
    *
    * @param vars are the variables of the search phase
    * @return a search phase
    */
  def searchPhase(vars: IntervalVarArray): SearchPhase = {
    SearchPhase(cp.searchPhase(vars.toIloArray))(implicitly(this))
  }

  /**
    * This method creates a search phase with a set of variables only. The variable and value choosers for these
    * variables will be chosen by CP Optimizer search automatically.
    *
    * @param vars are the variables of the search phase
    * @return a search phase
    */
  def searchPhase(vars: Array[IntervalVar]): SearchPhase = {
    SearchPhase(cp.searchPhase(vars.map(v => v.getIloIntervalVar())))(implicitly(this))
  }

  /**
    * Set the search phases.
    *
    * @param searchPhases a set of search phase
    */
  def setSearchPhases(searchPhases: SearchPhase*) = {
    cp.setSearchPhases(searchPhases.map(s=>s.getIloSearchPhase()).toArray)
  }

  /**
    * Solves the model using the CP Optimizer built-in strategy.
    *
    * @return A Boolean value indicating whether a feasible solution has been found. This solution is not
    *         necessarily optimal. If <em>false</em> is returned, a feasible solution may still be present,
    *         but CP Optimizer has not been able to prove its feasibility.
    */
  def solve(timeLimit: Double = IloCP.Infinity, logPeriod: Int = IloCP.IntMin) = {
    cp.setParameter(IloCP.DoubleParam.TimeLimit, timeLimit)
    if (logPeriod >= 0) cp.setParameter(IloCP.IntParam.LogPeriod, logPeriod)
    cp.solve()
  }

  /**
    * This member function starts a new search using the built-in strategy of IloCP. It should be used in conjunction
    * with the function next().
    */
  def startNewSearch() = cp.startNewSearch()

  /**
    * This member function searches for a new (or first) solution to the model. In the case of an optimization problem
    * (with an instance of IloObjective in the model), next finds a solution strictly better than the previous solution
    * found. In the case of a satisfaction problem, next returns a solution usually different from those previously
    * found but, it may occasionally find a solution that had been found already. Repeatedly calling next until it
    * returns false is guaranteed to terminate and to not miss any solutions of the model being solved. Solutions can
    * be accessed via getValue; for example cp.getValue(IloIntVar).
    *
    * This member function returns true when it has found a solution and false when there are no more (improving)
    * solutions or search has been stopped by a limit. You can use IloCP.getInfo with parameter
    * IloCP.IntInfo.FailStatus to find out the exact reason for the search stopping.
    *
    * For convenience, in the case where next() returns false, the last solution found will be delivered by the
    * invoking CP optimizer. When the model contains an instance of IloObjective, this will be the best solution found
    * during search.
    *
    * @return returns true when it has found a solution and false when there are no more (improving) solutions
    */
  def next() = cp.next()

  /**
    * This member function terminates a search and deletes the internal objects created by CP Optimizer to carry out
    * the search.
    * The use of this function is optional. CP Optimizer now automatically ends the search the next time
    * startNewSearch() or solve() is called. However, if you omit endSearch you will not obtain the correct summary
    * information on the search log, or through getInfo(ilog.cp.IloCP.IntInfo) until you do so.
    *
    * You should not call next() after calling endSearch() without first calling startNewSearch().
    */
  def endSearch() = cp.endSearch()

  /**
    * Returns the value of the objective in the solution.
    *
    * @return the value of the objective in the solution
    */
  def getObjectiveValue() = cp.getObjValue

  /**
    * Return the value of a numeric expression in the solution.
    *
    * @param expr is the numeric expression; the expression must be in the active model
    * @return the value of the expression in the solution
    */
  def getValue(expr: IntExpr) : Int = cp.getValue(expr.getIloIntExpr()).toInt

  /**
    * Return the value of a numeric expression in the solution.
    *
    * @param expr is the numeric expression; the expression must be in the active model
    * @return the value of the expression in the solution
    */
  def getValue(expr: NumExpr) : Int = cp.getValue(expr.getIloNumExpr()).toInt

  /**
    * Returns the start of the interval variable.
    *
    * @param v is the interval variable
    * @return the start of the interval variable
    */
  def getStart(v: IntervalVar) : Int = cp.getStart(v.getIloIntervalVar())

  /**
    * Returns the end of the interval variable.
    *
    * @param v is the interval variable
    * @return the end of the interval variable
    */
  def getEnd(v: IntervalVar) : Int = cp.getEnd(v.getIloIntervalVar())

  /**
    * Returns the size of the interval variable.
    *
    * @param v is the interval variable
    * @return the end of the interval variable
    */
  def getSize(v: IntervalVar) : Int = cp.getSize(v.getIloIntervalVar())

  /**
    * Returns the size of the interval variable.
    *
    * @param v is the interval variable
    * @return the end of the interval variable
    */
  def getLength(v: IntervalVar) : Int = cp.getLength(v.getIloIntervalVar())

  /**
    * Returns the current domain of an interval variable.
    *
    * @param v is the interval variable
    * @return a domain of the interval variable as a string
    */
  def getDomain(v: IntervalVar): String = cp.getDomain(v.getIloIntervalVar())


  /**
    * This member function assumes that the cumul function expression f is fixed. It returns the number of segments of
    * the corresponding stepwise non-negative function. A segment is an interval [start, end) on which the value of f
    * is constant. An exception is thrown if the cumul function expression f is not fixed.
    *
    * This function can be used to print the content of a cumul function expression as illustrated by the following
    * code sample.
    * <pre>
    *   <code>
    *     for (i <- 0 until cp.getNumberOfSegments(f))
    *         println("["  + cp.getSegmentStart(f,i) +
    *             ","  + cp.getSegmentEnd(f, i) +
    *             "):" + cp.getSegmentValue(f, i))
    *   </code>
    * </pre>
    *
    * @param f is the cumul function expression
    * @return the number of segments of the cumul function
    */
  def getNumberOfSegments(f: CumulFunctionExpr): Int = cp.getNumberOfSegments(f.getIloCumulFunctionExpr())

  /**
    * This member function assumes that the cumul function expression f is fixed. It returns the value of the ith
    * segment of the corresponding stepwise non-negative function. A segment is an interval [start, end) on which the
    * value of f is constant. If n is the number of segments of the function segments are indexed starting from 0 so
    * index i should belong to the range [0,n). An exception is thrown if i is not a valid segment index or f is not
    * fixed.
    *
    * @param f is the cumul function expression
    * @param i is index of the segment
    * @return the value of the cumul function on the segment
    */
  def getSegmentValue(f: CumulFunctionExpr, i: Int) = cp.getSegmentValue(f.getIloCumulFunctionExpr(), i)

  /**
    * This member function assumes that the cumul function expression f is fixed. It returns the start of the ith
    * segment of the corresponding stepwise non-negative function. A segment is an interval [start, end) on which the
    * value of f is constant. If n is the number of segments of the function segments are indexed starting from 0 so
    * index i should belong to the range [0,n). An exception is thrown if i is not a valid segment index or f is not
    * fixed.
    *
    * @param f is the cumul function expression
    * @param i is the index of the segment
    * @return the start of the ith segment
    */
  def getSegmentStart(f: CumulFunctionExpr, i: Int) = cp.getSegmentStart(f.getIloCumulFunctionExpr(), i)

  /**
    * This member function assumes that the cumul function expression f is fixed. It returns the end of the ith segment
    * of the corresponding stepwise non-negative function. A segment is an interval [start, end) on which the value of
    * f is constant. If n is the number of segments of the function segments are indexed starting from 0 so index i
    * should belong to the range [0,n). An exception is thrown if i is not a valid segment index or f is not fixed.
    *
    * @param f is the cumul function expression
    * @param i is the index of the segment
    * @return the end of the ith segment
    */
  def getSegmentEnd(f: CumulFunctionExpr, i: Int) = cp.getSegmentEnd(f.getIloCumulFunctionExpr(), i)

  //
  // Solutions
  //

  /**
    * Creates and returns a solution
    *
    * @return a solution
    */
  def solution(): Solution = cp.solution()

  /**
    * This member function stores the values of the objects added to solution by examining their current values in the
    * invoking constraint programming model.
    *
    * @param solution is the solution
    */
  def store(solution: Solution) = cp.store(solution)

  /**
    * This member function uses the invoking CP optimizer object to instantiate the variables in solution with their
    * saved values. The value of any objective added to the solution is not restored. If the solution does not violate
    * any constraints of the model extracted by the invoking CP optimizer, then true is returned and the state of the
    * constraint variables in the CP optimizer reflect those stored in solution. Otherwise the optimizer's state
    * remains unchanged and false is returned.
    *
    * @param solution is the solution
    * @return true if the solution can be restored, false otherwise
    */
  def restore(solution: IloSolution): Boolean = cp.restore(solution)

  /**
    * This member function sets solution sp as the new starting point for subsequent searches of the invoking CP
    * Optimizer engine.

    * @param solution is the solution
    */
  def setStartingPoint(solution: Solution) = cp.setStartingPoint(solution)

  /**
    * This member function removes any starting point specified on the invoking CP Optimizer engine: subsequent
    * searches will not use any starting point information, unless a new starting point is set.
    */
  def clearStartingPoint() = cp.clearStartingPoint()

  //
  // Debugging
  //

  /**
    * This method identifies a minimal conflict for the infeasibility of the current model. Since the conflict is
    * minimal, removal of any one of these constraints will remove that particular cause for infeasibility. There may
    * be other conflicts in the model; consequently, repair of a given conflict does not guarantee feasibility of the
    * remaining model.
    * If the conflict refiner also works on variable domains (parameter IloCP.IntParam.ConflictRefinerOnVariables set
    * to IloCP.ParameterValues.On), it will first identify a minimal conflicting set of constraints. Then, it will
    * refine this conflict further by identifying a minimal subset of variables whose initial domain is responsible for
    * the infeasibility from amongst the decision variables involved in the constraints of the conflict.
    * This method returns a boolean value reporting whether or not a conflict has been found.
    * When this method returns true, the conflict can be queried with the getConflict methods. The method writeConflict
    * can write the elements of the current conflict.

    * @return true if the a conflict has been found, false otherwise
    */
  def refineConflict() = cp.refineConflict()

  /**
    * This function exports the currently loaded model into the file named filename. This file name must have
    * extension .cpo.
    *
    * @param name is the name of the file
    */
  def exportModel(name: String) = cp.exportModel(name)

  /**
    * This member displays statistics about the CP optimizer. Specifically, in the CP optimizer's output stream, this
    * member function displays the following information:
    * <ul>
    *   <li>The number of branches (decisions taken) in the last solve.</li>
    *   <li>The number of fails in the last solve.</li>
    *   <li>The number of choice points created in the last solve.</li>
    *   <li>The number of variables in the CP optimizer engine, with a breakdown of the number extracted from the model
    *   and the number of additional variables added by the CP optimizer for its internal workings.</li>
    *   <li>The number of constraints in the CP optimizer engine.</li>
    *   <li>The total memory usage of the current optimizer including a breakdown into the memory used by the CP
    *   optimizer and that used by the Concert environmnet with which this CP optimizer was built.</li>
    *   <li>The time spent in the last solve (including startNewSearch/next). A breakdown is given for the CP Optimizer
    *   engine itself and the time to extract the model from Concert.</li>
    *   <li>The total time spent in the CP optimizer since its creation.</li>
    * </ul>
    *
    * Each of those numbers can be accessed individually via getInfo(ilog.cp.IloCP.IntInfo). Typical output from the
    * member function printInformation looks like this:
    * <pre>
    *   <code>
    *     Number of branches : 23355
    *     Number of fails : 7228
    *     Number of choice points : 12624
    *     Number of variables : 11856 (1408 model + 10448 additional)
    *     Number of constraints : 15873
    *     Total memory usage : 10.6 Mb (9.3 Mb CP + 1.3 Mb Concert)
    *     Time in last solve : 12.02 (11.97 engine + 0.05 extraction)
    *     Total time spent in CP : 12.02
    *   </code>
    * </pre>
    */
  def printInformation() = cp.printInformation()

  /**
    * Frees all memory resources allocated by the invoking CP Optimizer.
    */
  def end() = cp.end()

}

object CpModel {

  //
  // Types definition in case we need to encapsulate CPO types later
  //

  type IntervalSequenceVar = IloIntervalSequenceVar
  type TransitionDistance = IloTransitionDistance
  type Solution = IloSolution
  type MultiCriterionExpr = IloMultiCriterionExpr

  /**
    * Create and return a new mathematical programming model.
    *
    * @param name is the name of the model
    * @return a mathematical programming model
    */
  def apply(name: String=null) = new CpModel(name)

  /**
    * Return the sum of a set of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the sum of the numeric expressions
    */
  def sum(exprs: NumExprArray)(implicit model: CpModel): NumExpr = model.sum(exprs)

  /**
    * Return the sum of a set of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the sum of the numeric expressions
    */
  def sum(exprs: Array[NumExpr])(implicit model: CpModel): NumExpr = model.sum(exprs)

  /**
    * Return the integer sum of a sequence of integer expressions.
    *
    * @param exprs is a sequence of integer variables
    * @return a integer expression that represents the sum of the integer expressions
    */
  def sum(exprs: Array[IntExpr])(implicit model: CpModel): IntExpr = model.sum(exprs)

  /**
    * Returns the integer sum of a sequence of integer expressions.
    *
    * @param exprs is a sequence of integer variables
    * @return a integer expression that represents the sum of the integer expressions
    */
  def sumi(exprs: IntExprArray)(implicit model: CpModel): IntExpr = model.sumi(exprs)

  /**
    * Returns the maximum of a set of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the maximum of the numeric expressions
    */
  def max(exprs: NumExprArray)(implicit model: CpModel): NumExpr = model.max(exprs)

  /**
    * Returns the minimum of a set of numeric expressions.
    *
    * @param exprs is a sequence of numeric variables
    * @return a numeric expression that represents the minimum of the numeric expressions
    */
  def min(exprs: NumExprArray)(implicit model: CpModel): NumExpr = model.min(exprs)

  /**
    * Creates a new constrained integer expression equal to the number of variables that are equals to the
    * value v.
    *
    * @param vars is a set of integer variables
    * @param v is the value
    * @return a integer expression that is the number of variables equal to the given value
    */
  def count(vars: IntExprArray, v: Int)(implicit model: CpModel): IntExpr = model.count(vars, v)

  /**
    * Creates a new constrained integer expression equal to the number of variables that are equals to the
    * value v.
    *
    * @param vars is a set of integer variables
    * @param v is the value
    * @return a integer expression that is the number of variables equal to the given value
    */
  def count(vars: Array[IntExpr], v: Int)(implicit model: CpModel): IntExpr = model.count(vars, v)

  /**
    * Creates and returns an integer linear expression representing the scalar product of the given integer values
    * with the given integer variables.
    *
    * @param values is an arrayr of integer values
    * @param vars is an array of integer variables
    * @return an integer expression equals to the scalar product of the integer values with the integer variables
    */
  def scalarProduct(values: IntArray, vars: IntVarArray)(implicit model: CpModel): IntExpr =
    model.scalarProduct(values, vars)

  /**
    * Creates and returns an integer linear expression representing the scalar product of the given integer values
    * with the given integer variables.
    *
    * @param values is the sequence of values
    * @param vars is the sequence of variables
    * @return the scalar product of integer values with integer variables
    */
  def scalarProduct(values: Array[Int], vars: Array[IntVar])(implicit model: CpModel): IntExpr =
    model.scalarProduct(values, vars)

  /**
    * Returns an expression equal to the scalar product of values and exps, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param exprs is the sequence of integer expressions
    * @param values is the sequence of integer values
    * @return the scalar product of integer values with integer expressions
    */
  def prod(values: IntArray, exprs: IntExprArray)(implicit model: CpModel): IntExpr =
    model.prod(values, exprs)

  /**
    * Returns an expression equal to the scalar product of values and exps, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param exprs is an array of integer expressions
    * @param values is an array of integer values
    * @return an integer expression equals to the scalar product of the integer values with the integer expressions
    */
  def prod(values: Array[Int], exprs: Array[IntExpr])(implicit model: CpModel): IntExpr =
    model.prod(values, exprs)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps1 is an array of integer expressions
    * @param exps2 is an array of integer expressions
    * @return an integer expression equals to the scalar product of two arrays of integer expressions
    */
  def prod(exps1: IntExprArray, exps2: IntExprArray)(implicit model: CpModel): IntExpr = model.prod(exps1, exps2)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps1 is an array of integer expressions
    * @param exps2 is an array of integer expressions
    * @return an integer expression equals to the scalar product of two arrays of integer expressions
    */
  def prod(exps1: Array[IntExpr], exps2: Array[IntExpr])(implicit model: CpModel): IntExpr = model.prod(exps1, exps2)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps is an array of integer expressions
    * @param values is an array of integer values
    * @return an integer expression equals to the scalar product of the integer expressions with the integer values
    */
  def prod(exps: IntExprArray, values: IntArray)(implicit model: CpModel): IntExpr = model.prod(exps, values)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, exps1[0]*exps2[0] +
    * exps1[1]*exps2[1] + ...
    *
    * @param exps is an array of integer expressions
    * @param values is an array of integer values
    * @return an integer expression equals to the scalar product of the integer expressions with the integer values
    */
  def prod(exps: Array[IntExpr], values: Array[Int])(implicit model: CpModel): IntExpr = model.prod(exps, values)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is an  array of numeric values
    * @param exps is an array of numeric expressions
    * @return a numeric expression equals to the scalar product of the numeric expressions with the numeric values
    */
  def prod(values: NumArray, exps: NumExprArray)(implicit model: CpModel): NumExpr = model.prod(values, exps)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is an  array of numeric values
    * @param exps is an array of numeric expressions
    * @return a numeric expression equals to the scalar product of the numeric expressions with the numeric values
    */
  def prod(values: Array[Double], exps: Array[NumExpr])(implicit model: CpModel): NumExpr = model.prod(values, exps)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is an array of numeric values
    * @param exps is an array of numeric expressions
    * @return a numeric expression equals to the scalar product of the numeric expressions with the numeric values
    */
  def prod(exps: NumExprArray, values: NumArray)(implicit model: CpModel): NumExpr = model.prod(exps, values)

  /**
    * Returns an expression equal to the scalar product of exps1 and exps2, that is, values[0]*exps[0] +
    * values[1]*exps[1] + ...
    *
    * @param values is an array of numeric values
    * @param exps is an array of numeric expressions
    * @return a numeric expression equals to the scalar product of the numeric expressions with the numeric values
    */
  def prod(exps: Array[NumExpr], values: Array[Double])(implicit model: CpModel): NumExpr = model.prod(exps, values)

  /**
    * Creates and returns a new integer expression equals to exprs[index] where index is an integer expression.
    *
    * @param values is an array of integer values
    * @param index is an integer expression of the index
    * @return an integer expression equals to values[index]
    */
  def element(values: IntArray, index: IntExpr)(implicit model: CpModel): IntExpr = model.element(values, index)

  /**
    * Creates and returns a new integer expression equals to values[index] where index is an integer expression.
    *
    * @param values is an array of integer values
    * @param index is an integer expression of the index
    * @return an integer expression equals to values[index]
    */
  def element(values: Array[Int], index: IntExpr)(implicit model: CpModel): IntExpr = model.element(values, index)

  /**
    * Creates and returns a new integer expression equals to exprs[index] where index is a integer expression.
    *
    * @param exprs is an array of integer expressions
    * @param index is an integer expression of the index
    * @return an integer expression equals to exprs[index]
    */
  def element(exprs: IntExprArray, index: IntExpr)(implicit model: CpModel): IntExpr = model.element(exprs, index)

  /**
    * Creates and returns a new integer expression equals to exprs[index] where index is a integer expression.
    *
    * @param exprs is an array of integer expressions
    * @param index is an integer expression of the index
    * @return an integer expression equals to exprs[index]
    */
  def element(exprs: Array[IntExpr], index: IntExpr)(implicit model: CpModel): IntExpr = model.element(exprs, index)

  /**
    * Creates and returns a new constraint stating that the integer expressions must all take different values.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param exprs is an array of integer expressions
    * @return a new 'all different' constraint
    */
  def allDiff(exprs: IntExprArray)(implicit model: CpModel): Constraint = model.allDiff(exprs)

  /**
    * Creates and returns a new constraint stating that the integer expressions must all take different values.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param exprs is an array of integer expressions
    * @return a new 'all different' constraint
    */
  def allDiff(exprs: Array[IntExpr])(implicit model: CpModel): Constraint = model.allDiff(exprs)

  /**
    * Creates and returns a new constraint based on explicitly stating the allowed assignments for a small group of
    * variables. The allowed assignments are the values that satisfy the constraint; the argument table specifies the
    * combinations of allowed values of the variables. The order of the constrained variables is important because
    * the same order is respected in the set. To avoid exceptions, the size of vars must be the same as the arity of
    * the set.
    *
    * @param vars is an array of integer variables
    * @param values is the set of combinations of allowed values
    * @param model is the constraint programming model
    * @return a new 'allowed-assignments' constraint
    */
  def allowedAssignments(vars: IntVarArray, values: Iterable[IntArray])(implicit model: CpModel) =
    model.allowedAssignments(vars, values)

  /**
    * Creates and returns a new constraint based on explicitly stating the allowed assignments for a small group of
    * variables. The allowed assignments are the values that satisfy the constraint; the argument table specifies the
    * combinations of allowed values of the variables. The order of the constrained variables is important because
    * the same order is respected in the set. To avoid exceptions, the size of vars must be the same as the arity of
    * the set.
    *
    * @param vars is an array of integer variables
    * @param values is the set of combinations of allowed values
    * @param model is the constraint programming model
    * @return a new 'allowed-assignments' constraint
    */
  def allowedAssignments(vars: Array[IntVar], values: Iterable[Array[Int]])(implicit model: CpModel) =
    model.allowedAssignments(vars, values)

  /**
    * Creates and returns a pack constraint which maintains the load of a set of containers or bins, given a set of
    * weighted items and an assignment of items to containers. Consider that we have n items and m containers. Each
    * item i has an integer weight weight[i] and a constrained integer variable where[i] associated with it, indicating
    * in which container (numbered contiguously from 0) item i is to be placed. No item can be split up, and so an item
    * can go in only one container. Associated with each container j is an integer variable load[j] representing the
    * load in that container; that is, the sum of the weights of the items which have been assigned to that container.
    * A capacity can be set for each container placing an upper bound on this load variable. The constraint also ensures
    * that the total sum of the loads of the containers is equal to the sum of the weights of the items being placed.
    *
    * Finally, the number, or indeed the set of containers used can be specified by the integer expression used. A
    * container is used if at least one item is placed in the container in question.
    *
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param load is an array of integer expressions
    * @param where is an array of integer expressions
    * @param weight is an array of integer values
    * @param used is a interger expression
    * @param model is the constraint programming model
    * @return a new 'pack' constraint
    */
  def pack(load: IntExprArray, where: IntExprArray, weight: IntArray, used: IntExpr)(implicit model: CpModel): Constraint =
    model.pack(load, where, weight, used)

  /**
    * Creates and returns a pack constraint which maintains the load of a set of containers or bins, given a set of
    * weighted items and an assignment of items to containers. Consider that we have n items and m containers. Each
    * item i has an integer weight weight[i] and a constrained integer variable where[i] associated with it, indicating
    * in which container (numbered contiguously from 0) item i is to be placed. No item can be split up, and so an item
    * can go in only one container. Associated with each container j is an integer variable load[j] representing the
    * load in that container; that is, the sum of the weights of the items which have been assigned to that container.
    * A capacity can be set for each container placing an upper bound on this load variable. The constraint also ensures
    * that the total sum of the loads of the containers is equal to the sum of the weights of the items being placed.
    *
    * Finally, the number, or indeed the set of containers used can be specified by the integer expression used. A
    * container is used if at least one item is placed in the container in question.
    *
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param load is an array of integer expressions
    * @param where is an array of integer expressions
    * @param weight is an array of integer values
    * @param used is a interger expression
    * @param model is the constraint programming model
    * @return a new 'pack' constraint
    */
  def pack(load: Array[IntExpr], where: Array[IntExpr], weight: Array[Int], used: IntExpr)(implicit model: CpModel): Constraint =
    model.pack(load, where, weight, used)

  /**
    * This function returns a constraint that states that interval variable a is present. Typically, this constraint is
    * used in combination with other constraints.
    *
    * @param v is an interval variable
    * @return a constraint
    */
  def presenceOf(v: IntervalVar)(implicit model: CpModel): Constraint =
    model.presenceOf(v)

  /**
    * This function returns an integer expression that represents the start of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value absVal.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the start of the interval variable
    */
  def startOf(a: IntervalVar, absValue: Int=0)(implicit model: CpModel): IntExpr =
    model.startOf(a, absValue)

  /**
    * This function returns an integer expression that represents the end of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the end of the interval variable
    */
  def endOf(a: IntervalVar, absValue: Int=0)(implicit model: CpModel): IntExpr =
    model.endOf(a, absValue)

  /**
    * This function returns an integer expression that represents the length of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the length of the interval variable
    */
  def lengthOf(a: IntervalVar, absValue: Int=0)(implicit model: CpModel): IntExpr =
    model.lengthOf(a, absValue)

  /**
    * This function returns an integer expression that represents the size of interval variable 'a' whenever the
    * interval variable is present. When the interval variable is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param a is the interval variable
    * @param absValue is the value return when the interval variable is absent
    * @return an integer expression of the size of the interval variable
    */
  def sizeOf(a: IntervalVar, absValue: Int=0)(implicit model: CpModel): IntExpr =
    model.sizeOf(a, absValue)

  /**
    * This function returns an integer expression that represents the type of the interval variable that is next to
    * interval 'a' in sequence variable 'seq'. When interval 'a' is present and is the last interval of sequence 'seq',
    * it returns the constant integer value 'lastVal'. When interval a is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last interval variable present in the sequence
    * @param absVal is the value returned is the interval variable is absent
    * @return an interger expression that represents the type of the next interval variable in the sequence
    */
  def typeOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.typeOfNext(seq, a, lastVal, absVal)

  /**
    * This function returns an integer expression that represents the type of the interval variable that is previous to
    * interval 'a' in sequence variable 'seq'. When interval 'a' is present and is the first interval of sequence 'seq',
    * it returns the constant integer value 'firstVal'. When interval a is absent, it returns the constant integer value
    * 'absVal'.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the first interval variable present in the
    *                 sequence
    * @param absVal is the value returned is the interval variable is absent
    * @return an interger expression that represents the type of the previous interval variable in the sequence
    */
  def typeOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    typeOfPrevious(seq, a, firstVal, absVal)

  /**
    * This function returns an integer expression that represents the start of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the start of the next interval variable in the sequence
    */
  def startOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.startOfNext(seq, a, lastVal, absVal)

  /**
    * This function returns an integer expression that represents the start of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the start of the previous interval variable in the sequence
    */
  def startOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.startOfPrevious(seq, a, firstVal, absVal)

  /**
    * This function returns an integer expression that represents the end of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the end of the next interval variable in the sequence
    */
  def endOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.endOfNext(seq, a, lastVal, absVal)

  /**
    * This function returns an integer expression that represents the end of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the end of the previous interval variable in the sequence
    */
  def endOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.endOfPrevious(seq, a, firstVal, absVal)

  /**
    * This function returns an integer expression that represents the size of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the size of the next interval variable in the sequence
    */
  def sizeOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.sizeOfNext(seq, a, lastVal, absVal)

  /**
    * This function returns an integer expression that represents the size of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the size of the previous interval variable in the sequence
    */
  def sizeOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.sizeOfPrevious(seq, a, firstVal, absVal)

  /**
    * This function returns an integer expression that represents the length of the interval variable that is next to
    * interval a in sequence variable seq. When interval a is present and is the last interval of sequence seq, it
    * returns the constant integer value lastVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param lastVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the length of the next interval variable in the sequence
    */
  def lengthOfNext(seq: IntervalSequenceVar, a: IntervalVar, lastVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.lengthOfNext(seq, a, lastVal, absVal)

  /**
    * This function returns an integer expression that represents the length of the interval variable that is previous to
    * interval a in sequence variable seq. When interval a is present and is the first interval of sequence seq, it
    * returns the constant integer value firstVal. When interval a is absent, it returns the constant integer value
    * absVal.
    *
    * @param seq is the sequence variable
    * @param a is the interval variable
    * @param firstVal is the value returned if the interval variable is the last one present in the sequence
    * @param absVal is the value returns if the interval variable is absent
    * @return an integer expression that is the length of the previous interval variable in the sequence
    */
  def lengthOfPrevious(seq: IntervalSequenceVar, a: IntervalVar, firstVal: Int, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.lengthOfPrevious(seq, a, firstVal, absVal)

  /**
    * This function returns an integer expression that represents the length of the overlap of interval variable a1
    * and interval variable a2 whenever interval variables a1 and a2 are present. When interval variable a1 or a2 is
    * absent, the function returns the constant integer value absVal.
    *
    * @param a1 is the first interval variable
    * @param a2 is the second interval variable
    * @param absVal is the value returned if interval variable a1 or a1 is absent
    * @return an integer expression of the overlap of two interval variables
    */
  def overlapLength(a1: IntervalVar, a2: IntervalVar, absVal: Int=0)(implicit model: CpModel): IntExpr =
    model.overlapLength(a1, a2, absVal)

  /**
    * This function returns an integer expression that represents the length of the overlap of interval variable a and
    * constant interval [start, end) whenever interval variable a is present. When interval variable a is absent, the
    * function returns the constant integer value absVal.
    *
    * @param a is the interval variable
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @param absVal is the value returned if the inverval variable is absent
    * @return an integer expression of the overlap of an interval variable and a constant interval
    */
  def overlapLength(a: IntervalVar, start: Int, end: Int, absVal: Int)(implicit model: CpModel): IntExpr =
    model.overlapLength(a, start, end, absVal)

  /**
    * This function returns an integer expression that represents the length of the overlap of interval variable a and
    * constant interval [start, end) whenever interval variable a is present. When interval variable a is absent, the
    * function returns 0.
    *
    * @param a is the interval variable
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @return an integer expression of the overlap of an interval variable and a constant interval
    */
  def overlapLength(a: IntervalVar, start: Int, end: Int)(implicit model: CpModel): IntExpr =
    model.overlapLength(a, start, end)

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the start of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the start of the interval variable
    */
  def startEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0)(implicit model: CpModel): NumExpr =
    model.startEval(a, f, absVal)

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the end of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the end of the interval variable
    */
  def endEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0)(implicit model: CpModel): NumExpr =
    model.endEval(a, f, absVal)

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the length of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the length of the interval variable
    */
  def lengthEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0)(implicit model: CpModel): NumExpr =
    model.lengthEval(a, f, absVal)

  /**
    * This function returns a numerical expression that represents the value of function f evaluated on the size of
    * interval variable a whenever the interval variable is present. When the interval variable is absent, it returns
    * the constant numerical value absVal.
    *
    * @param a is the interval variable
    * @param f is the step function
    * @param absVal is the value returned if the interval variable is absent
    * @return an numeric expression of the value of function f on the size of the interval variable
    */
  def sizeEval(a: IntervalVar, f: IloNumToNumSegmentFunction, absVal: Double=.0)(implicit model: CpModel): NumExpr =
    model.sizeEval(a, f, absVal)

  /**
    * This method creates a no-overlap constraint on the set of interval variables defined by array a.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param vars is the sef of interval variables
    * @return a no-overlap constraint
    */
  def noOverlap(vars: IntervalVarArray)(implicit model: CpModel): Constraint = model.noOverlap(vars)

  /**
    * This method creates a no-overlap constraint on the set of interval variables defined by array a.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param vars is the sef of interval variables
    * @return a no-overlap constraint
    */
  def noOverlap(vars: Array[IntervalVar])(implicit model: CpModel): Constraint = model.noOverlap(vars)

  /**
    * This method creates a no-overlap constraint on the sequence variable seq. This constraint states that the
    * interval variables of the sequence do not overlap and that the order of intervals in the sequence is the order
    * implied by the relative position of the start and end points of the non-overlapping intervals. A transition
    * distance tdist is used to specify a minimal distance between two interval variables in the sequence.
    * The transition distance holds between an interval and all its successors in the sequence.
    *
    * @param seq is the sequence variable
    * @param tdist is the transition distance
    * @return a no-overlap constraint
    */
  def noOverlap(seq: IntervalSequenceVar, tdist: TransitionDistance=null, direct: Boolean=false)(implicit model: CpModel): Constraint =
    model.noOverlap(seq, tdist, direct)

  /**
    * This method creates a span constraint between interval variable a and the set of interval variables bs.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is the interval variable
    * @param bs is the set of interval variables
    * @return a span constraint
    */
  def span(a: IntervalVar, bs: Iterable[IntervalVar])(implicit model: CpModel): Constraint = model.span(a, bs)

  /**
    * Creates and returns a precedence constraint that states that whenever both interval variables a and b are present,
    * the distance start(b)-end(a)between the end of interval a and the start of interval b must be greater than or
    * equal to 0.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param b is an interval variable
    * @return a precedence constraint
    */
  def endBeforeStart(a: IntervalVar, b: IntervalVar)(implicit model: CpModel): Constraint =
    model.endBeforeStart(a, b)

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is an array of interval variables
    * @param cardinality is the cardinality
    * @param name is the name of the constraint
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: IntervalVarArray, cardinality: Int=1, name: String=null)(implicit model: CpModel): Constraint =
    model.alternative(a, bs, cardinality, name)

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is an array of interval variables
    * @param cardinality is the cardinality
    * @param name is the name of the constraint
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: Array[IntervalVar], cardinality: Int, name: String)(implicit model: CpModel): Constraint =
    model.alternative(a, bs, cardinality, name)

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is an array of interval variables
    * @param cardinality is the cardinality
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: Array[IntervalVar], cardinality: Int)(implicit model: CpModel): Constraint =
    model.alternative(a, bs, cardinality)

  /**
    * This method creates an alternative constraint between interval variable a and the set of interval variables in
    * the array bs with cardinality c. If a is present, c intervals in bs will be selected by the alternative
    * constraint.
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param a is an interval variable
    * @param bs is an array of interval variables
    * @param name is the name of the constraint
    * @return an alternative constraint
    */
  def alternative(a: IntervalVar, bs: Array[IntervalVar], name: String)(implicit model: CpModel): Constraint =
    model.alternative(a, bs, name)

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to v between the start and the end of interval variable a and equal to 0 everywhere else. When interval
    * variable a is absent, the function is the constant nul function.
    *
    * @param a is the interval variable
    * @param v is the value of the cumul function on interval 'a'
    * @return a cumul function expression
    */
  def pulse(a: IntervalVar, v: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.pulse(a, v)

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to a value 'v' such that 'vmin <= v <= vmax' everywhere between the start and the end of interval variable
    * 'a' and equal to 0 everywhere else. The choice of the value v in the range [vmin,vmax] is a decision of the
    * problem. When interval variable 'a' is absent, the function is the constant nul function.
    *
    * @param a is the interval variable
    * @param vmin is the minimum value of the cumul function on interval 'a' if present
    * @param vmax is the maximum value of the cumul function on interval 'a' is present
    * @return a cumul function expression
    */
  def pulse(a: IntervalVar, vmin: Int, vmax: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.pulse(a, vmin, vmax)

  /**
    * This function returns an elementary cumul function expression that is equal to 'v' between 'start' and 'end' and
    * equal to 0 everywhere else.
    *
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @param v is the value of the cumul function on interval [start..end)
    * @return a cumul function expression
    */
  def pulse(start: Int, end: Int, v: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.pulse(start, end, v)

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to 0 before the start of a and equal to v after the start of a. When interval variable a is absent, the
    * function is the constant nul function.
    *
    * @param v is the interval variable
    * @param value is the value of the cumul funciton on the interval variable
    * @return a cumul function expression
    */
  def stepAtStart(v: IntervalVar, value: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.stepAtStart(v, value)

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to a 0 before the start of a and equal to a value v such that vmin <= v <= vmax after the start of a. The
    * choice of the value v in the range [vmin,vmax] is a decision of the problem. When interval variable a is absent,
    * the function is the constant nul function.
    *
    * @param v is the interval variable
    * @param vmin is the minimum value of the cumul function on the interval variable
    * @param vmax is the maximum valyue of the cumul function on the interval variable
    * @return a cumul function expression
    */
  def stepAtStart(v: IntervalVar, vmin: Int, vmax: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.stepAtStart(v, vmin, vmax)

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to 0 before the end of a and equal to v after the end of a. When interval variable a is absent, the
    * function is the constant nul function.
    *
    * @param v is the interval variable
    * @param value is the value of the cumul funciton on the interval variable
    * @return a cumul function expression
    */
  def stepAtEnd(v: IntervalVar, value: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.stepAtEnd(v, value)

  /**
    * This function returns an elementary cumul function expression that, whenever interval variable a is present, is
    * equal to a 0 before the end of a and equal to a value v such that vmin <= v <= vmax after the end of a. The
    * choice of the value v in the range [vmin,vmax] is a decision of the problem. When interval variable a is absent,
    * the function is the constant nul function.
    *
    * @param v is the interval variable
    * @param vmin is the minimum value of the cumul function on the interval variable
    * @param vmax is the maximum value of the cuml function on the interval variable
    * @return a cumul function expression
    */
  def stepAtEnd(v: IntervalVar, vmin: Int, vmax: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.stepAtEnd(v, vmin, vmax)

  /**
    * This function returns an elementary cumul function expression that is equal to 0 before point t and equal to v
    * after point t.
    *
    * @param t is the time point
    * @param v is the value of the cumul function at time t
    * @return a cumul function expression
    */
  def step(t: Int, v: Int)(implicit model: CpModel): CumulFunctionExpr =
    model.step(t, v)

  /**
    * Creates and returns a new cumul function expressions that is the sum of a set of cumul function expressions.
    *
    * @param exprs is an array of cumul function expressions
    * @return a new cumul function expressions
    */
  def sum(exprs: CumulFunctionExprArray)(implicit model: CpModel): CumulFunctionExpr = model.sum(exprs)

  /**
    * Returns a cumul function expressions that is the sum of a set of cumul function expressions.
    *
    * @param exprs the set of cumul function expressions
    * @return the sum of the cumul function expressions
    */
  def sum(exprs: Array[CumulFunctionExpr])(implicit model: CpModel): CumulFunctionExpr = model.sum(exprs)

  /**
    * This function returns a constraint that states that the value of cumul function expression f should be always
    * within the range [vmin,vmax] between start and end.
    *
    * Note: This constraint cannot be used in a logical constraint.
    *
    * @param f is the cumul function expression
    * @param start is the start of the interval
    * @param end is the end of the interval
    * @param vmin is the minimum value of the function
    * @param vmax is the maximum value of the function
    * @return a new constraint on the value of the function
    */
  def alwaysIn(f: CumulFunctionExpr, start: Int, end: Int, vmin: Int, vmax: Int)(implicit model: CpModel): Constraint =
    model.alwaysIn(f, start, end, vmin, vmax)

  /**
    * Creates and returns an objective object to minimize the expression <em>expr</em>.
    *
    * @param expr is the expression to minimize
    * @return An objective object representing the objective to minimize
    */
  def minimize(expr: NumExpr)(implicit model: CpModel): Objective = model.minimize(expr)

  /**
    * Creates a minimization multicriteria objective.
    *
    * @param expr is the multicriteria expressions
    * @return an objective
    */
  def minimize(expr: MultiCriterionExpr)(implicit model: CpModel): Objective = model.minimize(expr)

  /**
    * Creates and returns an objective object to maximize the expression <em>expr</em>.
    *
    * @param expr is the expression to maximize
    * @return An objective object the objective to maximize
    */
  def maximize(expr: NumExpr)(implicit model: CpModel): Objective = model.maximize(expr)

  /**
    * Creates a maximization multicriteria objective.
    *
    * @param expr is the multicriteria expressions
    * @return an objective
    */
  def maximize(expr: MultiCriterionExpr)(implicit model: CpModel): Objective = model.maximize(expr)

  /**
    * This function defines a multicriteria expression for lexicographic ordering. A lexicographic ordering means that
    * any improvement of the i-th criterion is more important than any improvement of the subsequent criteria.
    *
    * @param exprs a set of integer expressions for the lexicographic ordering
    */
  def staticLex(exprs: NumExpr*)(implicit model: CpModel): MultiCriterionExpr = model.staticLex(exprs: _*)

  /**
    * This method creates a search phase with a set of variables only. The variable and value choosers for these
    * variables will be chosen by CP Optimizer search automatically.
    *
    * @param vars are the variables of the search phase
    * @return a search phase
    */
  def searchPhase(vars: IntVarArray)(implicit model: CpModel): SearchPhase =
    model.searchPhase(vars)

  /**
    * This method creates a search phase with a set of variables only. The variable and value choosers for these
    * variables will be chosen by CP Optimizer search automatically.
    *
    * @param vars are the variables of the search phase
    * @return a search phase
    */
  def searchPhase(vars: Array[IntVar])(implicit model: CpModel): SearchPhase =
    model.searchPhase(vars)

  /**
    * This method creates a search phase with a set of variables only. The variable and value choosers for these
    * variables will be chosen by CP Optimizer search automatically.
    *
    * @param vars are the variables of the search phase
    * @return a search phase
    */
  def searchPhase(vars: Array[IntervalVar])(implicit model: CpModel): SearchPhase =
    model.searchPhase(vars)

  /**
    *  Implicit conversion of set of numeric expressions: add behavior
    *
    * @param exprs are the integer expressions
    * @param model is the constraint programming model
    */
  implicit class NumExprArray(val exprs: Iterable[NumExpr])(implicit model: CpModel) {

    /**
      * Converts to scala array
      */
    def toArray: Array[NumExpr] = exprs.toArray

    /**
      * Convert to CPLEX array
      */
    def toIloArray: Array[IloNumExpr] = exprs.map(e => e.getIloNumExpr()).toArray
  }

  /**
    *  Implicit conversion of set of integer expressions: add behavior
    *
    * @param exprs are the integer expressions
    * @param model is the constraint programming model
    */
  implicit class IntExprArray(val exprs: Iterable[IntExpr])(implicit model: CpModel)
    extends Iterable[IntExpr] {

    /**
      * Method get creates and returns a new integer expression equal to exprs[index] where index is an integer
      * expression.
      *
      * @param expr is the integer expression for the index
      * @return an new integer expression
      */
    def element(expr: IntExpr): IntExpr = model.element(exprs.toArray, expr)

    /**
      * Converts to scala array
      */
    override def toArray[B >: IntExpr : ClassTag]: Array[B] = exprs.toArray[B]

    /**
      * Converts to scala array
      */
    def toIloArray: Array[IloIntExpr] = exprs.map(e => e.getIloIntExpr()).toArray

    /**
      * Returns an iterator.
      *
      * @return an iterator
      */
    override def iterator: Iterator[IntExpr] = exprs.iterator
  }

  /**
    *  Class IterableInt give additional behavior such as a method get to on a set of integer values.
    *
    * @param values are the integer values
    * @param model is the constraint programming model
    */
  implicit class NumArray(val values: Iterable[Double])(implicit model: CpModel)
    extends Iterable[Double] {

    /**
      * Method get creates and returns a new integer expression equal to exprs[index] where index is an integer
      * expression.
      *
      * @param expr is the integer expression for the index
      * @return an new integer expression
      */
    def element(expr: IntExpr): NumExpr = model.element(values, expr)

    /**
      * Converts to scala array
      */
    def toArray: Array[Double] = values.toArray

    /**
      * Returns an iterator.
      *
      * @return an iterator
      */
    override def iterator: Iterator[Double] = values.iterator
  }

  /**
    *  Class IterableInt give additional behavior such as a method get to on a set of integer values.
    *
    * @param values are the integer values
    * @param model is the constraint programming model
    */
  implicit class IntArray(val values: Iterable[Int])(implicit model: CpModel)
    extends Iterable[Int] {

    /**
      * Method get creates and returns a new integer expression equal to exprs[index] where index is an integer
      * expression.
      *
      * @param expr is the integer expression for the index
      * @return an new integer expression
      */
    def element(expr: IntExpr): IntExpr = model.element(values, expr)

    /**
      * Converts to scala array
      */
    def toArray: Array[Int] = values.toArray

    /**
      * Returns an iterator.
      *
      * @return an iterator
      */
    override def iterator: Iterator[Int] = values.iterator

  }

  /**
    *  Class IterableIntervalVar gives additional behavior such as implicit conversion to search phase
    *
    * @param exprs are the integer expressions
    * @param model is the constraint programming model
    */
  implicit class IntervalVarArray(val exprs: Iterable[IntervalVar])(implicit model: CpModel)
    extends Iterable[IntervalVar] {

    /**
      * Converts to search phase.
      *
      * @return a search phase
      */
    def toSearchPhase: SearchPhase = SearchPhase(model.cp.searchPhase(exprs.map(v => v.getIloIntervalVar()).toArray))

    /**
      * Converts to scala array
      */
    def toArray: Array[IntervalVar] = exprs.toArray

    /**
      * Converts to scala array of CPLEX interval variables.
      *
      * @return an array of CPLEX interval variables
      */
    def toIloArray: Array[IloIntervalVar] = exprs.map(e => e.getIloIntervalVar()).toArray

    /**
      * Returns an iterator.
      *
      * @return an iterator
      */
    override def iterator: Iterator[IntervalVar] = exprs.iterator

  }

  /**
    *  Class IntVarArray gives additional behavior such as implicit conversion to search phase
    *
    * @param vars are the integer variables
    * @param model is the constraint programming model
    */
  implicit class IntVarArray(val vars: Iterable[IntVar])(implicit model: CpModel) extends Iterable[IntVar] {

    /**
      * Converts to search phase.
      *
      * @return a search phase
      */
    def toSearchPhase: SearchPhase = SearchPhase(model.cp.searchPhase(vars.map(v => v.getIloIntVar()).toArray))

    /**
      * Converts to scala array
      */
    override def toArray[B >: IntVar: ClassTag]: Array[B] = vars.toArray[B]

    /**
      * Converts to CPLEX array
      */
    def toIloArray: Array[IloIntVar] = vars.map(v => v.getIloIntVar()).toArray

    /**
      * Returns an iterator.
      *
      * @return an iterator
      */
    override def iterator: Iterator[IntVar] = vars.iterator
  }

  /**
    * Implicit conversion of a set of cumul function expressions to an array of cumul function expressions.
    *
    * @param exprs is a set of cumul function expressions
    * @param model is the constraint programming model
    */
  implicit class CumulFunctionExprArray(val exprs: Iterable[CumulFunctionExpr])(implicit model: CpModel)
    extends Iterable[CumulFunctionExpr] {

    implicit private val num = CumulFunctionExprNumeric(model)

    /**
      * Creates and returns a new cumul function expression that represents the sum of the cumul function expressions.
      *
      * @return a new cumul function expression
      */
    def sum: CumulFunctionExpr = exprs.sum(num)

    /**
      * Converts an cumul function expression array to an scala array
      *
      * @return
      */
    def toArray: Array[CumulFunctionExpr] = exprs.toArray

    /**
      * Returns an iterator.
      *
      * @return an iterator
      */
    override def iterator: Iterator[CumulFunctionExpr] = exprs.iterator
  }
}