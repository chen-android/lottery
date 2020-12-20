package com.cs.lottery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var start = false
    private var findBall: LotteryView.Ball? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lottery_view.setNames(lottery_name_et.text.toString().split(",", "，"))
        lottery_view.findGoatListener = {
            goat_tv.text = "恭喜你获得了${it.text}的礼物"
            start_roll_bt.text = "开始下一个"
            restart_roll_bt.visibility = View.VISIBLE
            findBall = it
        }
        confirm_name_bt.setOnClickListener {
            lottery_view.setNames(lottery_name_et.text.toString().split(",", "，"))
        }
        restart_roll_bt.setOnClickListener {
            roll(false)
        }
        start_roll_bt.setOnClickListener {
            roll(true)
        }
    }

    private fun roll(deleteGoat: Boolean) {
        restart_roll_bt.visibility = View.GONE
        start = start.not()
        if (start) {
            goat_tv.text = ""
            if (lottery_view.currentStatus == LotteryView.Status.SHOW_GOAT) {
                if (findBall != null && deleteGoat) {
                    lottery_name_et.setText(lottery_view.nameList.filter { name -> name != findBall!!.text }.joinToString(","))
                }
                lottery_view.setNames(lottery_name_et.text.toString().split(",", "，"))
                findBall = null
            }
            if (lottery_view.currentStatus == LotteryView.Status.INIT) {
                lottery_view.startRoll()
            }
        } else {
            if (lottery_view.currentStatus == LotteryView.Status.START_ROLL) {
                lottery_view.stopRoll()
            }
        }
        start_roll_bt.text = if (start) "停止" else "开始"
    }
}