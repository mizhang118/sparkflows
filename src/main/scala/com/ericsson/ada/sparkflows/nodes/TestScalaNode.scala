package com.ericsson.ada.sparkflows.nodes

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType

import fire.context.JobContext
import fire.workflowengine.Node
import fire.output.OutputTable

class TestScalaNode extends Node {

  var n: Int = 5

  def this(i: Int, nm: String, tempn: Int) = {
    this()
    id = i
    name = nm
    
    n = tempn
  }

  @Override
  override def execute(jobContext: JobContext): Unit = {
// get the first n rows
    val rows: Array[Row] = dataFrame.take(n).asInstanceOf[Array[Row]]
    if (rows == null || rows.length == 0) return
    val numRows: Int = rows.length
    val numCols: Int = rows(0).length
// create result 2d array
    val values: Array[Array[String]] =
      Array.ofDim[String](numRows + 2, numCols)
// fill in the schema
    val structType: StructType = dataFrame.schema
    val fields: Array[StructField] = structType.fields
    for (i <- 0 until fields.length) {
      values(0)(i) = fields(i).name
      values(1)(i) = fields(i).dataType.toString
    }
    for (i <- 0 until numRows) {
      val row: Row = rows(i)
      for (j <- 0 until numCols) {
        values(i + 2)(j) = if (row.get(j) == null) "" else row.get(j).toString
      }
    }
// create output table
    val outputTable: OutputTable = new OutputTable()
    outputTable.id = id
    outputTable.name = name
    outputTable.title = "Row Values"
    outputTable.cellValues = values
// output to workflow context
    jobContext.workflowctx().outTable(this, outputTable)
    passDataFrameToNextNodesAndExecute(jobContext, dataFrame)
  }

}
