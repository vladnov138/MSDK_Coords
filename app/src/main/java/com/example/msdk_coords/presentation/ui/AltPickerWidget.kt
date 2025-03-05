package com.example.msdk_coords.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.msdk_coords.R

class AltPickerWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var _altitude = 30.0f // Начальная высота
    val altitude get() = _altitude
    private val btnDecrease: ImageButton
    private val btnIncrease: ImageButton
    private val tvHeight: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.alt_picker_widget, this, true)

        btnDecrease = findViewById(R.id.btn_decrease)
        btnIncrease = findViewById(R.id.btn_increase)
        tvHeight = findViewById(R.id.tv_height)

        radius = 12f // Закругленные углы
        cardElevation = 6f // Тень

        updateHeightDisplay()

        btnDecrease.setOnClickListener { decreaseHeight() }
        btnIncrease.setOnClickListener { increaseHeight() }
    }

    private fun updateHeightDisplay() {
        tvHeight.text = _altitude.toString()
    }

    private fun increaseHeight() {
        _altitude += 1
        updateHeightDisplay()
    }

    private fun decreaseHeight() {
        if (_altitude > 0) { // Минимальное значение
            _altitude -= 1
            updateHeightDisplay()
        }
    }
}