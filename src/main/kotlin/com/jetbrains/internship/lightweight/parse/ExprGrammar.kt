package com.jetbrains.internship.lightweight.parse

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser
import com.jetbrains.internship.lightweight.parse.model.*

object ExprGrammar : Grammar<List<Call>>() {
    private val ws by token("\\s+", ignore = true) // let's ignore whitespaces for the sake of readability
    private val lpar by token("\\(")
    private val rpar by token("\\)")
    private val minusSign by token("-")
    private val numOpSign by token("[+*]")
    private val compareSign by token("[><]")
    private val boolOpSign by token("[|&]")
    private val number by token("[\\d]+") use { text.toInt() }
    private val element by token("element") map { Element() }
    private val mapCallName by token("map")
    private val filterCallName by token("filter")
    private val lcpar by token("\\{")
    private val rcpar by token("}")
    private val callChainOp by token("%>%")

    val binaryOperationSign = minusSign or numOpSign or compareSign or boolOpSign use { text }

    val numConstant by (number or (-minusSign * number map { -it })) map ::Num

    val constantExpression by numConstant map { ConstantExpression(it) }

    val binaryExpression by
        -lpar * parser(this::expression) * binaryOperationSign * parser(this::expression) * -rpar use {
            val left = t1
            val opSign = t2
            val right = t3
            BinaryExpression(opSign, left, right)
        }

    val expression: Parser<Expression> by element or constantExpression or binaryExpression

    val mapCall by -mapCallName * -lcpar * expression * -rcpar map ::MapCall
    val filterCall by -filterCallName * -lcpar * expression * -rcpar map ::FilterCall
    val call = mapCall or filterCall

    val callChain by separated(call, callChainOp) use { terms }

    override val rootParser = callChain
}