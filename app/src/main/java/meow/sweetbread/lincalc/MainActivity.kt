package meow.sweetbread.lincalc

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LimitExceededException
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private var priority = HashMap<String, Int>()
    private var tokensType = HashMap<String, Tokens>()
    private val leftAssociative = arrayOf("^")
    private var constants = HashMap<String, Double>()

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        priority["("] = 0
        priority["+"] = 1
        priority["-"] = 1
        priority["*"] = 2
        priority["/"] = 2
        priority["^"] = 3
        priority["√"] = 3

        tokensType["+"] = Tokens.Operations
        tokensType["-"] = Tokens.Operations
        tokensType["*"] = Tokens.Operations
        tokensType["/"] = Tokens.Operations
        tokensType["^"] = Tokens.Operations
        tokensType["√"] = Tokens.Function
        tokensType["+"] = Tokens.Operations
        tokensType["+"] = Tokens.Operations
        tokensType["("] = Tokens.BracketOpen
        tokensType[")"] = Tokens.BracketClose

        constants["π"] = Math.PI
        constants["e"] = Math.E

        findViewById<Button>(R.id.btn_0).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_1).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_2).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_3).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_4).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_5).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_6).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_7).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_8).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_9).setOnClickListener { btn_listener(it as Button) }

        findViewById<Button>(R.id.btn_dot).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_add).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_sub).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_mul).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_div).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_bro).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_brc).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_pow).setOnClickListener { btn_listener(it as Button) }
        findViewById<Button>(R.id.btn_sqrt).setOnClickListener { btn_listener(it as Button) }

        findViewById<Button>(R.id.btn_pi).setOnClickListener { btn_listener(it as Button) }
//        findViewById<Button>(R.id.btn_elr).setOnClickListener { btn_listener(it as Button) }

        findViewById<Button>(R.id.btn_x).setOnClickListener { btn_listener(it as Button) }

        findViewById<Button>(R.id.btn_equ).setOnClickListener { btn_listener(it as Button) }

        val image = findViewById<TextView>(R.id.input)

        findViewById<Button>(R.id.btn_del).setOnClickListener {
            image.text = image.text.dropLast(1)
        }

        findViewById<Button>(R.id.btn_sbm).setOnClickListener {
            try {
                findViewById<TextView>(R.id.last_exp_out).text =
                    findViewById<TextView>(R.id.input).text.toString()
                val equations = findViewById<TextView>(R.id.input).text.toString().split('=')
                if (equations.size == 1) {
                    val answer = evaluate(toRpn(tokenizer(equations[0])))
                    val value = if (answer.value.toString().split('.')[1] == "0")
                        (answer.value as Double).toInt().toString()
                    else (answer.value as Double).toFloat().toString()
                    findViewById<TextView>(R.id.left_out).text = answer.toString()
                    findViewById<TextView>(R.id.last_exp_out).text =
                        findViewById<TextView>(R.id.last_exp_out).text.toString() + " = " + value
                    findViewById<TextView>(R.id.input).text = ""
                } else {
                    var left = evaluate(toRpn(tokenizer(equations[0])))
                    var right = evaluate(toRpn(tokenizer(equations[1])))

                    if (((left.type == Tokens.Number) and (right.type != Tokens.Number)) or
                        (left.type == Tokens.Linear) and (right.type == Tokens.Quadratic)
                    ) {
                        val tmp = right
                        left = right
                        right = tmp
                    }

                    findViewById<TextView>(R.id.left_out).text = left.toString()
                    findViewById<TextView>(R.id.right_out).text = right.toString()

                    if (left.type == Tokens.Number) {
                        val value = if (left.value.toString().split('.')[1] == "0")
                            (left.value as Double).toInt().toString()
                        else left.value.toString()
                        findViewById<TextView>(R.id.last_exp_out).text =
                            findViewById<TextView>(R.id.last_exp_out).text.toString() + " = " + value
                        findViewById<TextView>(R.id.input).text = ""
                    } else if (left.type == Tokens.Linear) {
                        val answer: Double

                        answer = if (right.type == Tokens.Number)
                            (left.value as Linear - right.value as Double).answer()
                        else
                            (left.value as Linear - right.value as Linear).answer()
                        val value: String
                        value = if (answer.toFloat().toString().split('.')[1] == "0")
                            answer.toInt().toString()
                        else answer.toString()
                        findViewById<TextView>(R.id.last_exp_out).text =
                            findViewById<TextView>(R.id.last_exp_out).text.toString() +
                                    "\n${(left.value as Linear).name} = " + value
                        findViewById<TextView>(R.id.input).text = ""
                    } else {

                    }
                }
            } catch (e: java.lang.Exception) {
                findViewById<TextView>(R.id.last_exp_out).text = "Ошибка"
                findViewById<TextView>(R.id.right_out).text = e.stackTraceToString()
            }
        }
    }


    private fun tokenizer(exp: String): List<Token> {
        var position = 0
        var tokens = listOf<Token>()

        while (position < exp.length) {
            val chr = exp[position]

            if (chr == ' ') {
                position++
                continue
            } else if ((chr in '0'..'9') or (chr == '.')) {
                var pos = position
                while (pos < exp.length) {
                    if (!((exp[pos] in '0'..'9') or (exp[pos] == '.')))
                        break
                    pos++
                }
                if (tokens.isNotEmpty()) {
                    if (tokens.last().type in arrayOf(
                            Tokens.BracketClose,
                            Tokens.Constant,
                            Tokens.Variable
                        )
                    )
                        tokens += Token(Tokens.Operations, "*")
                }
                tokens += Token(Tokens.Number, exp.substring(position, pos).toDouble())
                position = pos
            } else if (chr == 'X') {
                if (tokens.isNotEmpty()) {
                    if (tokens.last().type in arrayOf(
                            Tokens.BracketClose,
                            Tokens.Constant,
                            Tokens.Number
                        )
                    )
                        tokens += Token(Tokens.Operations, "*")
                }
                tokens += Token(Tokens.Variable, chr.toString())
                position++
            } else if (chr.toString() in constants.keys) {
                if (tokens.isNotEmpty()) {
                    if (tokens.last().type in arrayOf(
                            Tokens.BracketClose,
                            Tokens.Constant,
                            Tokens.Variable,
                            Tokens.Number
                        )
                    )
                        tokens += Token(Tokens.Operations, "*")
                }
                tokens += Token(Tokens.Constant, chr.toString())
                position++
            } else if (chr in 'a'..'z') {
                var pos = position
                while (pos < exp.length) {
                    if (exp[pos] !in 'a'..'z')
                        break
                    pos++
                }
                if (tokens.isNotEmpty()) {
                    if (tokens.last().type in arrayOf(
                            Tokens.BracketClose,
                            Tokens.Constant,
                            Tokens.Variable
                        )
                    )
                        tokens += Token(Tokens.Operations, "*")
                }
                tokens += Token(Tokens.Function, exp.substring(position, pos))
                position = pos
            } else if (tokensType.containsKey(chr.toString())) {
                if (tokens.isNotEmpty()) {
                    if ((tokensType[chr.toString()]!! == Tokens.BracketOpen) and
                        (tokens.last().type in arrayOf(Tokens.Number, Tokens.Variable))
                    )
                        tokens += Token(Tokens.Operations, "*")
//                    if ((chr == '-') and (tokens.last().type != Tokens.Number))
//                        tokens += Token(Tokens.Number, 0.0)
                } // else if (chr == '-')
//                    tokens += Token(Tokens.Number, 0.0)
                tokens += Token(tokensType[chr.toString()]!!, chr.toString())
                position++
            } else {
                findViewById<TextView>(R.id.input).error = "Неизвестный символ: ${exp[position]}"
                break
            }
        }

        var new_tokens = listOf<Token>()

        for (token in tokens) {
            if (token.type == Tokens.Constant) {
                new_tokens += Token(Tokens.Number, constants[token.value]!!)
            } else {
                new_tokens += token
            }
        }

        Log.i("Meow!", new_tokens.toString())
        return new_tokens
    }

    private fun btn_listener(btn: Button) {
        val input = findViewById<TextView>(R.id.input)
        input.text = input.text.toString() + btn.text.toString()
    }

    fun toRpn(tokens: List<Token>): List<Token> {
        var out_buf = listOf<Token>()
        val act_buf = arrayListOf<Token>()

        for (token in tokens) {
            if (token.type in arrayOf(Tokens.Number, Tokens.Variable))
                out_buf += token
            else if (token.type == Tokens.Function)
                act_buf += token
            else if (token.type == Tokens.Operations) {
                if (act_buf.lastOrNull() != null) {
                    while (
                        (priority[token.value]!! < priority[act_buf.last().value]!!) or
                        ((priority[token.value]!! == priority[act_buf.last().value]!!) and
                                (token.value in leftAssociative))
                    ) {
                        out_buf += act_buf.removeLast()
                        if (act_buf.isEmpty())
                            break
                    }
                }
                act_buf += token
            } else if (token.type == Tokens.BracketOpen)
                act_buf += token
            else if (token.type == Tokens.BracketClose) {
                while (act_buf.last().type != Tokens.BracketOpen)
                    out_buf += act_buf.removeLast()
                act_buf.removeLast()
                if (act_buf.lastOrNull() != null) {
                    if (act_buf.last().type == Tokens.Function)
                        out_buf += act_buf.removeLast()
                }
            }

            Log.d("Meow!", "Act: $act_buf\tOut: $out_buf")
        }

        out_buf += act_buf.reversed()
        findViewById<TextView>(R.id.RPN_out).text = out_buf.toString()
        findViewById<TextView>(R.id.input).text = ""

        Log.d("Meow!", "toRpn: $out_buf")
        return out_buf
    }

    fun evaluate(ext: List<Token>): Token {
        var position = 0
        var buffer = ext
        var tmp_buf = listOf<Token>()

        while (buffer.size != 1) {
            var token = buffer[position]

            if (token.type == Tokens.Function) {
                if (token.value == "√") {
                    tmp_buf = buffer.dropLast(ext.size - position + 1)
                    var res = Token(Tokens.Number, sqrt(buffer[position - 1].value as Double))
                    tmp_buf += res
                    tmp_buf += buffer.drop(position + 1)
                }
            } else if (token.type == Tokens.Operations) {
                val first = buffer[position - 2].value
                val second = buffer[position - 1].value

                //TODO: make operations with other objects
                when (token.value) {
                    "+" -> {
                        tmp_buf = buffer.dropLast(ext.size - position + 2)
                        val res: Token =
                            if ((first is Double) and (second is Double))
                                Token(
                                    Tokens.Number,
                                    (first as Double + second as Double)
                                )
                            else if ((first is String) and (second is Double))
                                Token(
                                    Tokens.Linear,
                                    Linear(first as String, second as Double, 0.0)
                                )
                            else if ((first is Linear) and (second is Double))
                                Token(
                                    Tokens.Linear, Linear(
                                        (first as Linear).name,
                                        (first as Linear).add + (second as Double),
                                        (first as Linear).mul
                                    )
                                )
                            else
                                Token(
                                    Tokens.Linear,
                                    Linear(second as String, first as Double, 0.0)
                                )
                        tmp_buf += res
                        tmp_buf += buffer.drop(position + 1)
                    }
                    "-" -> {
                        tmp_buf = buffer.dropLast(ext.size - position + 2)
                        val res: Token = when (first) {
                            is Double -> {
                                when (second) {
                                    is Double -> {
                                        Token(Tokens.Number, (first - second))
                                    }
                                    is String -> {
                                        Token(Tokens.Linear, Linear(second, first, -1.0))
                                    }
                                    is Linear -> {
                                        Token(
                                            Tokens.Linear,
                                            Linear(
                                                second.name,
                                                first - second.add,
                                                -1.0 * second.mul
                                            )
                                        )
                                    }
                                    else -> {
                                        Token(Tokens.Linear, "")
                                    }
                                }
                            }
                            is String -> {
                                when (second) {
                                    is Double -> {
                                        Token(Tokens.Linear, Linear(first, -second, 1.0))
                                    }
                                    is String -> {
                                        Token(Tokens.Quadratic, Quadratic(first, 0.0, 1.0, 2.0))
                                    }
                                    else -> {
                                        Token(Tokens.Linear, "")
                                    }
                                }
                            }
                            is Linear -> {
                                when (second) {
                                    is Double -> {
                                        Token(
                                            Tokens.Linear,
                                            Linear(first.name, first.add - second, first.mul)
                                        )
                                    }
                                    else -> {
                                        Token(Tokens.Linear, "")
                                    }
                                }
                            }

                            else -> {
                                Token(Tokens.Linear, "")
                            }
                        }
                        tmp_buf += res
                        tmp_buf += buffer.drop(position + 1)
                    }
                    "*" -> {
                        tmp_buf = buffer.dropLast(ext.size - position + 2)

                        val res: Token = when (first) {
                            is Double -> {
                                when (second) {
                                    is Double -> {
                                        Token(Tokens.Number, first * second)
                                    }
                                    is String -> {
                                        Token(Tokens.Linear, Linear(second, 0.0, first))
                                    }
                                    is Linear -> {
                                        Token(Tokens.Linear, second * first)
                                    }
                                    else -> {
                                        Token(Tokens.Linear, "")
                                    }
                                }
                            }
                            is String -> {
                                when (second) {
                                    is Double -> {
                                        Token(Tokens.Linear, Linear(first, 0.0, second))
                                    }
                                    is String -> {
                                        Token(Tokens.Quadratic, "")
                                    }
                                    is Linear -> {
                                        Token(Tokens.Quadratic, "")
                                    }
                                    else -> {
                                        Token(Tokens.Quadratic, "")
                                    }
                                }
                            }
                            else -> {
                                Token(Tokens.Quadratic, "")
                            }
                        }
                        tmp_buf += res
                        tmp_buf += buffer.drop(position + 1)
                    }
                    "/" -> {
                        tmp_buf = buffer.dropLast(ext.size - position + 2)
                        val res: Token =
                            if ((first is Double) and (second is Double))
                                Token(Tokens.Number, first as Double / second as Double)
                            else if ((first is String) and (second is Double))
                                Token(
                                    Tokens.Linear,
                                    Linear(
                                        first as String,
                                        0.0,
                                        1.0 / (second as Double)
                                    )
                                )
                            else
                                Token(Tokens.Number, 0.0)
                        tmp_buf += res
                        tmp_buf += buffer.drop(position + 1)
                    }
                    "^" -> {
                        tmp_buf = buffer.dropLast(ext.size - position + 2)
                        val res = Token(
                            Tokens.Number, (first as Double)
                                .pow(second as Double)
                        )
                        tmp_buf += res
                        tmp_buf += buffer.drop(position + 1)
                    }
                }
            } else {
                position++
                continue
            }

            buffer = tmp_buf
            position = 0
            Log.d("Meow!", buffer.toString())
        }

        return buffer[0]
    }

}

enum class Tokens {
    Number,
    Operations,
    Function,
    BracketOpen,
    BracketClose,
    Constant,
    Variable,
    Linear,
    Quadratic
}

class Token(val type: Tokens, val value: Any) {
    override fun toString(): String {
        return "<$type, $value>"
    }
}

open class MathObject(val value: Any) {
    operator fun minus(num: MathObject): MathObject {
        return MathObject(value)
    }
}

class Linear(val name: String, val add: Double, val mul: Double) : MathObject("$mul*$name+$add") {
    //TODO: Implement more methods
    override fun toString(): String {
        return "$mul*$name+$add"
    }

    operator fun minus(num: Double): Linear {
        return Linear(name, add - num, mul)
    }

    operator fun minus(lin: Linear): Linear {
//        if (name != lin.name) throw IllegalArgumentException
        return Linear(name, add - lin.add, mul - lin.mul)
    }

    operator fun times(num: Double): Linear {
        return Linear(name, add, mul * num)
    }

    fun answer() = -add / mul
}

class Quadratic(name: String, add: Double, mul: Double, pow: Double) {
    //TODO: Implement more methods
}