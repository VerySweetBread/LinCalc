package meow.sweetbread.lincalc

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        tokensType["+"] = Tokens.Operations
        tokensType["-"] = Tokens.Operations
        tokensType["*"] = Tokens.Operations
        tokensType["/"] = Tokens.Operations
        tokensType["^"] = Tokens.Operations
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
        findViewById<Button>(R.id.btn_elr).setOnClickListener { btn_listener(it as Button) }

        findViewById<Button>(R.id.btn_x).setOnClickListener { btn_listener(it as Button) }

        findViewById<Button>(R.id.btn_equ).setOnClickListener { btn_listener(it as Button) }

        val image = findViewById<TextView>(R.id.input)

        findViewById<ImageButton>(R.id.del).setOnClickListener {
            image.text = image.text.dropLast(1)
        }

        findViewById<Button>(R.id.btn_sbm).setOnClickListener {
            findViewById<TextView>(R.id.last_exp_out).text = findViewById<TextView>(R.id.input).text.toString()
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
                    (left.type == Tokens.Linear) and (right.type == Tokens.Quadratic)) {
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
                    value = if (answer.toString().split('.')[1] == "0")
                        answer.toInt().toString()
                    else answer.toString()
                    findViewById<TextView>(R.id.last_exp_out).text =
                        findViewById<TextView>(R.id.last_exp_out).text.toString() +
                                "\n${(left.value as Linear).name} = " + value
                    findViewById<TextView>(R.id.input).text = ""
                } else {

                }
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
                    if (tokens.last().type in arrayOf(Tokens.BracketClose, Tokens.Constant, Tokens.Variable))
                        tokens += Token(Tokens.Operations, "*")
                }
                tokens += Token(Tokens.Number, exp.substring(position, pos).toDouble())
                position = pos
            } else if (chr == 'X') {
                if (tokens.isNotEmpty()) {
                    if (tokens.last().type in arrayOf(Tokens.BracketClose, Tokens.Constant, Tokens.Number))
                        tokens += Token(Tokens.Operations, "*")
                }
                tokens += Token(Tokens.Variable, chr.toString())
                position++
            } else if (chr.toString() in constants.keys){
                if (tokens.isNotEmpty()) {
                    if (tokens.last().type in arrayOf(Tokens.BracketClose, Tokens.Constant, Tokens.Variable, Tokens.Number))
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
                    if (tokens.last().type in arrayOf(Tokens.BracketClose, Tokens.Constant, Tokens.Variable))
                        tokens += Token(Tokens.Operations, "*")
                }
                tokens += Token(Tokens.Function, exp.substring(position, pos))
                position = pos
            }
            else if (tokensType.containsKey(chr.toString())) {
                if (tokens.isNotEmpty()) {
                    if ((tokensType[chr.toString()]!! == Tokens.BracketOpen) and (tokens.last().type == Tokens.Number))
                        tokens += Token(Tokens.Operations, "*")
                    if ((chr == '-') and (tokens.last().type != Tokens.Number))
                        tokens += Token(Tokens.Number, 0.toDouble())
                } else if (chr == '-')
                    tokens += Token(Tokens.Number, 0.toDouble())
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
                                (token.value in leftAssociative))) {
                        out_buf += act_buf.removeLast()
                        if (act_buf.isEmpty())
                            break
                    }
                }
                act_buf += token
            } else if (token.type == Tokens.BracketOpen)
                act_buf += token
            else if (token.type == Tokens.BracketClose){
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
            val token = buffer[position]

            if (token.type == Tokens.Function) {
                if (token.value == "sqrt") {
                    tmp_buf = buffer.dropLast(ext.size - position + 1)
                    var res = Token(Tokens.Number, sqrt(buffer[position - 1].value as Double))
                    tmp_buf += res
                    tmp_buf += buffer.drop(position+1)
                }
            } else if (token.type == Tokens.Operations) {
                val first = buffer[position-2]
                val second = buffer[position-1]

                //TODO: make operations with other objects
                if (token.value == "+") {
                    tmp_buf = buffer.dropLast(ext.size - position + 2)
                    var res: Token
                    if ((first.type == Tokens.Number) and (second.type == Tokens.Number))
                        res = Token(Tokens.Number, (first.value as Double + second.value  as Double))
                    else if ((first.type == Tokens.Variable) and (second.type == Tokens.Number))
                        res = Token(Tokens.Linear, Linear(first.value as String, second.value as Double, 0.toDouble()))
                    else
                        res = Token(Tokens.Linear, Linear(second.value as String, first.value as Double, 0.toDouble()))
                    tmp_buf += res
                    tmp_buf += buffer.drop(position+1)
                } else if (token.value == "-") {
                    tmp_buf = buffer.dropLast(ext.size - position + 2)
                    val res = Token(Tokens.Number, (first.value as Double - second.value as Double))
                    tmp_buf += res
                    tmp_buf += buffer.drop(position+1)
                } else if (token.value == "*") {
                    tmp_buf = buffer.dropLast(ext.size - position + 2)
                    val res: Token
                    res = if ((first.type == Tokens.Number) and (second.type == Tokens.Number))
                        Token(Tokens.Number, first.value as Double*second.value as Double)
                    else if ((first.type == Tokens.Number) and (second.type == Tokens.Variable))
                        Token(Tokens.Linear, Linear(second.value as String, 0.toDouble(), first.value as Double))
                    else if ((first.type == Tokens.Variable) and (second.type == Tokens.Number))
                        Token(Tokens.Linear, Linear(first.value as String, 0.toDouble(), second.value as Double))
                    else //if ((first.type == Tokens.Variable) and (second.type == Tokens.Variable))
                        Token(Tokens.Quadratic, Quadratic(first.value as String, 0.toDouble(), 0.toDouble(), 2.toDouble()))
                    tmp_buf += res
                    tmp_buf += buffer.drop(position+1)
                } else if (token.value == "/") {
                    tmp_buf = buffer.dropLast(ext.size - position + 2)
                    val res = Token(Tokens.Number, first.value as Double/second.value as Double)
                    tmp_buf += res
                    tmp_buf += buffer.drop(position+1)
                } else if (token.value == "^") {
                    tmp_buf = buffer.dropLast(ext.size - position + 2)
                    val res = Token(
                        Tokens.Number, (first.value as Double)
                            .pow(second.value as Double)
                    )
                    tmp_buf += res
                    tmp_buf += buffer.drop(position + 1)
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

class Token (val type: Tokens, var value: Any) {
    override fun toString(): String {
        return "<$type, $value>"
    }
}

open class MathObject(val value: Any) {
    operator fun minus(num: MathObject): MathObject {
        return MathObject(value)
    }
}

class Linear(val name: String, private val add: Double, private val mul: Double) : MathObject("$mul*$name+$add") {
    //TODO: Implement more methods
    override fun toString(): String {
        return "$mul*$name+$add"
    }

    operator fun minus(num: Double): Linear {
        return Linear(name, add-num, mul)
    }

    operator fun minus(lin: Linear): Linear {
//        if (name != lin.name) throw IllegalArgumentException
        return Linear(name, add-lin.add, mul-lin.mul)
    }

    fun answer() = -add/mul
}

class Quadratic(name: String, add: Double, mul: Double, pow: Double) {
    //TODO: Implement more methods
}