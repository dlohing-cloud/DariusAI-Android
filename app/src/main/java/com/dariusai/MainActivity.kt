package com.dariusai

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    private lateinit var chat: LinearLayout
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        root.addView(TextView(this).apply {
            text = "Darius AI  •  V3"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(21,101,192))
            setPadding(24,28,24,28)
            gravity = Gravity.CENTER_VERTICAL
        })
        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6,6,6,6)
        }
        listOf("General","Diplomacy","Law","Teaching","Business").forEach { name ->
            tabs.addView(Button(this).apply {
                text = name
                textSize = 10f
                setOnClickListener { Toast.makeText(this@MainActivity, "$name workspace", Toast.LENGTH_SHORT).show() }
            }, LinearLayout.LayoutParams(0,-2,1f))
        }
        root.addView(tabs)
        val scroll = ScrollView(this)
        chat = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20,16,20,16)
        }
        addMessage("Darius AI","Hello. I’m Darius AI V3. Your mobile assistant is ready.")
        scroll.addView(chat)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12,8,12,12)
        }
        input = EditText(this).apply { hint = "Ask Darius AI…"; maxLines = 4 }
        composer.addView(input, LinearLayout.LayoutParams(0,-2,1f))
        composer.addView(Button(this).apply {
            text = "Send"
            setOnClickListener { sendMessage() }
        })
        root.addView(composer)
        setContentView(root)
    }

    private fun sendMessage() {
        val message = input.text.toString().trim()
        if (message.isEmpty()) return
        addMessage("You", message)
        addMessage("Darius AI","Message received. Secure AI backend, memory, documents, voice, tasks and integrations are the next V3 modules.")
        input.text.clear()
    }

    private fun addMessage(sender: String, message: String) {
        chat.addView(TextView(this).apply {
            text = "$sender\n$message"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(18,14,18,14)
            setBackgroundColor(Color.rgb(245,247,250))
        }, LinearLayout.LayoutParams(-1,-2).apply { setMargins(0,0,0,12) })
    }
}
