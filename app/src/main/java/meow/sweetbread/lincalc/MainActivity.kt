package meow.sweetbread.lincalc

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private var tmp_buf = ""
    var buffer = listOf<String>()
    private var act_ena = false
    var priority = HashMap<String, Int>()

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        priority.put("+", 1)
        priority.put("-", 1)
        priority.put("*", 2)
        priority.put("/", 2)

        val input = findViewById<TextView>(R.id.input)

        findViewById<Button>(R.id.btn_0).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_1).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_2).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_3).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_4).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_5).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_6).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_7).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_8).setOnClickListener { btnInt(it as Button) }
        findViewById<Button>(R.id.btn_9).setOnClickListener { btnInt(it as Button) }

        findViewById<Button>(R.id.btn_add).setOnClickListener { btnAct(it as Button) }
        findViewById<Button>(R.id.btn_sub).setOnClickListener { btnAct(it as Button) }
        findViewById<Button>(R.id.btn_mul).setOnClickListener { btnAct(it as Button) }
        findViewById<Button>(R.id.btn_div).setOnClickListener { btnAct(it as Button) }

        findViewById<Button>(R.id.btn_equ).setOnClickListener { toRpn() }
    }


    fun btnInt(btn: Button) {
        val input = findViewById<TextView>(R.id.input)
        input.text = input.text.toString() + btn.text
        tmp_buf += btn.text

        act_ena = true
        findViewById<TextView>(R.id.tmp_buf_out).text = tmp_buf
        findViewById<TextView>(R.id.buf_out).text = buffer.toString()
    }

    fun btnAct(btn: Button) {
        if (act_ena) {
            val input = findViewById<TextView>(R.id.input)
            input.text = input.text.toString() + btn.text
            if (tmp_buf != "") {
                buffer += tmp_buf
                tmp_buf = ""
            }
            act_ena = false
            buffer += btn.text.toString()
        }
        findViewById<TextView>(R.id.buf_out).text = buffer.toString()
    }

    fun toRpn() {
        if (tmp_buf != "") {
            buffer += tmp_buf
            tmp_buf = ""
        }
        findViewById<TextView>(R.id.tmp_buf_out).text = tmp_buf
        findViewById<TextView>(R.id.buf_out).text = buffer.toString()

        var out_buf = listOf<String>()
        var act_stc = listOf<String>()

        Log.d("Meow!", buffer.toString())

        for (token in buffer) {
            if (token.toBigDecimalOrNull() != null)
                out_buf += token
            else if (token in listOf<String>("+", "-", "*", "/")) {
//                if (act_stc.lastOrNull() == null)
//                    act_stc += token
//                else if (priority[token]!! > priority[act_stc.last()]!!)
//                    act_stc += token
//                else {
//                    out_buf += act_stc.last()
//                    act_stc = act_stc.dropLast(1)
//                    act_stc += token
//                }

                if (act_stc.lastOrNull() != null) {
                    while (priority[token]!! <= priority[act_stc.last()]!!) {
                        out_buf += act_stc.last()
                        act_stc = act_stc.dropLast(1)

                        if (act_stc.isEmpty())
                            break
                    }
                }
                act_stc += token
            }
            Log.d("Meow!", "Act: $act_stc\tOut: $out_buf")
        }

        out_buf += act_stc.reversed()
        findViewById<TextView>(R.id.RPN_out).text = out_buf.toString()

        tmp_buf = ""
        buffer = listOf()
        act_ena = false
        findViewById<TextView>(R.id.input).text = ""
    }
}