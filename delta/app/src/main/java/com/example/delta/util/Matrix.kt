package com.example.delta.util

import kotlin.math.E
import kotlin.math.pow

class Matrix {
    private var mRowSize: Int = 0
    private var mColSize: Int = 0
    private var mData: MutableList<MutableList<Double>> = mutableListOf()
    var shape: Pair<Int,Int> = Pair(0,0)

    constructor(rowSize: Int, colSize: Int){
        mRowSize = rowSize
        mColSize = colSize
        shape = Pair(rowSize,colSize)
        mData = MutableList(mRowSize) { MutableList(mColSize) { 0.0 } }
    }
    constructor(data: MutableList<MutableList<Double>>){
        mData = data
        mRowSize = data.size
        mColSize = data[0].size
        shape = Pair(data.size,data[0].size)

    }
    constructor(matrixString: String){
        val listOfRowStrings: List<String> = matrixString.split("\n")
        for (row in listOfRowStrings) {
            val doubleListOfEntries: MutableList<Double> = mutableListOf()
            val listOfEntries = row.split(",")
            for (entry in listOfEntries){
                doubleListOfEntries.add(entry.toDouble())
            }
            mData.add(doubleListOfEntries)
        }
        mRowSize = mData.size
        mColSize = mData[0].size
        shape = Pair(mData.size,mData[0].size)

    }
    operator fun times(rightMatrix: Matrix): Matrix {
        if(this.mColSize != rightMatrix.mRowSize){
            throw java.lang.IllegalArgumentException("Matrix dimensions not compatible for multiplication!")
        }
        val product = Matrix(this.getRowSize(),rightMatrix.getColSize())
        for (i in 0 until mRowSize) {
            for (j in 0 until rightMatrix.mColSize) {
                for (k in 0 until mColSize) {
                    product.mData[i][j] += this.mData[i][k] * rightMatrix.mData[k][j]
                }
            }
        }
        return product
    }
    operator fun get(i: Int): MutableList<Double> {
        return mData[i]
    }
    override fun toString(): String {
        return this.mData.joinToString(separator = "\n")
    }
    fun copy(): Matrix {
        val newData: MutableList<MutableList<Double>> = mutableListOf()
        for (row in this.mData){
            val newRow: MutableList<Double> = mutableListOf()
            for (entry in row){
                newRow.add(entry)
            }
            newData.add(newRow)
        }
        return Matrix(newData)
    }
    fun getData(): MutableList<MutableList<Double>>{
        return this.mData
    }
    fun getRowSize(): Int{
        return this.mRowSize
    }
    fun getColSize(): Int{
        return this.mColSize
    }
    fun addOneToFront(){
        this.mData.add(0, MutableList(1){ 1.0 })
        mRowSize = mData.size
        mColSize = mData[0].size
        shape = Pair(mData.size,mData[0].size)
    }
    companion object{
        fun tanSigmoid(input: Matrix): Matrix {
            val output = input.copy()
            for (i in 0 until input.getRowSize()){
                output[i][0] = (2 * (1 / (1 + E.pow(-2*input[i][0]))))-1
            }
            return output
        }

        fun logSigmoid(input: Matrix): Matrix {
            val output = input.copy()
            for (i in 0 until input.getRowSize()){
                output[i][0] = (1)/(1+E.pow(-input[i][0]))
            }
            return output
        }
    }
}