package com.example.msdk_ardupilot.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import com.example.msdk_coords.R

class AltPickerWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var _altitude = 1.5f // Начальная высота
    val altitude get() = _altitude
    private val btnDecrease: ImageButton
    private val btnIncrease: ImageButton
    private val etHeight: EditText

    init {
        LayoutInflater.from(context).inflate(R.layout.alt_picker_widget, this, true)

        btnDecrease = findViewById(R.id.btn_decrease)
        btnIncrease = findViewById(R.id.btn_increase)
        etHeight = findViewById(R.id.tv_height)

        radius = 12f // Закругленные углы
        cardElevation = 6f // Тень

        updateHeightDisplay()
        setupEditText()

        btnDecrease.setOnClickListener { decreaseHeight() }
        btnIncrease.setOnClickListener { increaseHeight() }
    }

    private fun updateHeightDisplay() {
        etHeight.setText(_altitude.toString())
    }

    private fun increaseHeight() {
        applyEnteredValue()
        _altitude += 1
        updateHeightDisplay()
    }

    private fun decreaseHeight() {
        if (_altitude > 0) {
            applyEnteredValue()
            _altitude -= 1
            updateHeightDisplay()
        }
    }

    private fun setupEditText() {
        etHeight.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                applyEnteredValue()
                etHeight.clearFocus()
                true
            } else {
                false
            }
        }
        etHeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                applyEnteredValue()
            }
        }
    }

    private fun applyEnteredValue() {
        val input = etHeight.text.toString().replace(',', '.')
        _altitude = input.toFloatOrNull() ?: _altitude
        updateHeightDisplay()
//        etHeight.isCursorVisible = false
        etHeight.clearFocus()
    }
}