package com.example.simplecaculator2

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.example.simplecaculator2.model.History
import org.w3c.dom.Text
import java.lang.NumberFormatException
import androidx.core.view.isVisible as isVisible

class MainActivity : AppCompatActivity() {

    private val expressionTextView: TextView by lazy {
        findViewById<TextView>(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById<TextView>(R.id.resultTextView)
    }

    private val historyLayout: View by lazy {
        findViewById<View>(R.id.historyLayout)
    }
    private val historyLinearLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
    }

    lateinit var db: AppDatabase

    private var isOperator = false
    private var hasOperator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db=Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()
    }

    fun buttonClicked(v: View) {

        when(v.id){
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")
            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonMulti -> operatorButtonClicked("X")
            R.id.buttonDivider -> operatorButtonClicked("??")
            R.id.buttonModulo -> operatorButtonClicked("%")
        }



    }

    private fun numberButtonClicked(number: String) {

        if(isOperator){
            expressionTextView.append(" ")

        }

        isOperator = false

        val expressionText = expressionTextView.text.split(" ")

        if(expressionText.isNotEmpty() && expressionText.last().length >= 15) {
            Toast.makeText(this, "15?????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show()
            return
        }else if(expressionText.last().isEmpty() && number == "0" ) {
            Toast.makeText(this, "0??? ?????? ???????????? ?????? ????????????.", Toast.LENGTH_SHORT).show()
            return
        }

        expressionTextView.append(number)
        resultTextView.text = calculatorExpression()

    }

    private fun operatorButtonClicked(operator: String){

        when{
            isOperator -> {
                val text = expressionTextView.text.toString()
                expressionTextView.text = text.dropLast(1)+operator

            }

            hasOperator -> {
                Toast.makeText(this, "???????????? ?????? ?????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                return

            }

            else -> {
                expressionTextView.append(" $operator")
            }

        }

        val ssb = SpannableStringBuilder(expressionTextView.text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ssb.setSpan(
                ForegroundColorSpan(getColor(R.color.green)),
                expressionTextView.text.length -1,
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        expressionTextView.text = ssb

        isOperator = true
        hasOperator = true
    }
    fun resultButtonClicked(v: View) {
        val expressionTexts = expressionTextView.text.split(" ")

        if(expressionTexts.isEmpty() || expressionTexts.size ==1) {
            return
        }
        if(expressionTexts.size !=3 && hasOperator) {
            Toast.makeText(this, "?????? ???????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()
            return
        }

        if(expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            Toast.makeText(this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
            return
        }

        val expressionText = expressionTextView.text.toString()
        val resultText = calculatorExpression()

        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()

        expressionTextView.text = resultText
        resultTextView.text = ""

        isOperator = false
        hasOperator = false

    }

    private fun calculatorExpression(): String {

        val expressionTexts = expressionTextView.text.split(" ")

        if(hasOperator.not() || expressionTexts.size != 3) {
            return ""
        }else if(expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            return ""
        }

        val ex1 = expressionTexts[0].toBigInteger()
        val ex2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when(op){
            "+" -> (ex1 + ex2).toString()
                "-" -> (ex1 - ex2).toString()
                "X" -> (ex1 * ex2).toString()
                "??" -> (ex1 / ex2).toString()
                "%" -> (ex1 % ex2).toString()
                else -> ""
        }
    }
    fun historyButtonClicked(v: View) {
        //ToDo ???????????? ?????? ?????? ????????????, ??? ?????????
        historyLayout.visibility = View.VISIBLE

        historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {

                runOnUiThread{
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = " = ${it.result}"

                    historyLinearLayout.addView(historyView)
                }

            }
        }).start()

    }

    fun historyClearButtonClicked(v: View) {
        //ToDo ???????????? ?????? ?????? ??????, ???????????? ?????? ?????? ??????

        historyLinearLayout.removeAllViews()

        Thread(Runnable {

            db.historyDao().deleteAll()

        }).start()

    }

    fun historyCloseButtonClicked(v: View) {
        //ToDo ???????????? ??? ???????????? ??????
        historyLayout.visibility = View.GONE
    }

    fun clearButtonClicked(v: View) {
        expressionTextView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }
}

private fun String.isNumber(): Boolean {

    return try{
        this.toBigInteger()
        true
    }catch(e : NumberFormatException){
        false
    }

}
