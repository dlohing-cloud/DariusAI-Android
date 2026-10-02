package com.dariusai

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity() {
    private lateinit var chat: LinearLayout
    private lateinit var input: EditText
    private var workspace = "General"

    private val blue = Color.rgb(21, 101, 192)
    private val lightBlue = Color.rgb(232, 240, 254)
    private val textDark = Color.rgb(32, 40, 50)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 250, 252))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 18, 12, 18)
            setBackgroundColor(blue)
        }

        header.addView(TextView(this).apply {
            text = "Darius AI"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        header.addView(Button(this).apply {
            text = "New"
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { startNewChat() }
        })
        root.addView(header)

        val workspaceLabel = TextView(this).apply {
            text = "Workspace: General"
            tag = "workspaceLabel"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(blue)
            setPadding(18, 12, 18, 6)
        }
        root.addView(workspaceLabel)

        val tabsScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(8, 0, 8, 4)
        }
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("General", "Diplomacy", "Law", "Teaching", "Business").forEach { name ->
            tabs.addView(Button(this).apply {
                text = name
                textSize = 12f
                setOnClickListener {
                    workspace = name
                    workspaceLabel.text = "Workspace: $name"
                    addMessage("Darius AI", "$name workspace selected. How can I help?")
                }
            }, LinearLayout.LayoutParams(-2, -2).apply { setMargins(3, 0, 3, 0) })
        }
        tabsScroll.addView(tabs)
        root.addView(tabsScroll)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        chat = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 10, 16, 16)
        }
        addMessage("Darius AI", "Welcome, Darius. I’m ready to help with diplomacy, law, teaching, business and everyday tasks.")
        addQuickActions()
        scroll.addView(chat)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10, 8, 10, 10)
            setBackgroundColor(Color.WHITE)
        }

        input = EditText(this).apply {
            hint = "Ask Darius AI…"
            textSize = 16f
            maxLines = 4
            imeOptions = EditorInfo.IME_ACTION_SEND
            setSingleLine(false)
            setPadding(16, 10, 16, 10)
            background = rounded(Color.rgb(242, 244, 248), 28f)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
            }
        }
        composer.addView(input, LinearLayout.LayoutParams(0, -2, 1f))

        composer.addView(Button(this).apply {
            text = "Send"
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener { sendMessage() }
        }, LinearLayout.LayoutParams(-2, -2).apply { setMargins(8, 0, 0, 0) })

        root.addView(composer)
        setContentView(root)
    }

    private fun addQuickActions() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 4, 0, 12)
        }
        listOf("Draft memo", "Study law", "Plan lesson").forEach { action ->
            row.addView(Button(this).apply {
                text = action
                textSize = 11f
                setOnClickListener {
                    input.setText(action + " for me")
                    input.requestFocus()
                }
            }, LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(3, 0, 3, 0) })
        }
        chat.addView(row)
    }

    private fun sendMessage() {
        val message = input.text.toString().trim()
        if (message.isEmpty()) return
        addMessage("You", message)
        addMessage("Darius AI", "I received your request in the $workspace workspace. The next backend connection will let me generate the full response here.")
        input.text.clear()
    }

    private fun startNewChat() {
        chat.removeAllViews()
        addMessage("Darius AI", "New chat started. What would you like to work on?")
        addQuickActions()
        input.requestFocus()
    }

    private fun addMessage(sender: String, message: String) {
        val bubble = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(textDark)
            setPadding(16, 13, 16, 13)
            background = rounded(if (sender == "You") lightBlue else Color.WHITE, 18f)
        }
        val label = TextView(this).apply {
            text = sender
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (sender == "You") blue else Color.DKGRAY)
            setPadding(4, 4, 4, 2)
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(label)
            addView(bubble)
        }
        chat.addView(box, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, 12)
        })
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
}
