package com.dariusai

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import java.util.Locale
import android.graphics.drawable.GradientDrawable
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var chat: LinearLayout
    private lateinit var input: EditText
    private var workspace = "General"
    private val attachedFiles = mutableListOf<String>()
    private var sending = false

    private val blue = Color.rgb(36, 99, 235)
    private val navy = Color.rgb(18, 31, 53)
    private val lightBlue = Color.rgb(232, 240, 254)
    private val textDark = Color.rgb(32, 40, 50)

    companion object {
        private const val PICK_FILES = 1001
        private const val SPEECH_INPUT = 1002
        private const val BACKEND_URL = "https://darius-ai.vercel.app/api/chat"
    }

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
            setBackgroundColor(navy)
        }

        header.addView(TextView(this).apply {
            text = "DARIUS AI"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        header.addView(Button(this).apply {
            text = "＋  New"
            textSize = 12f
            setTextColor(Color.WHITE)
            background = rounded(Color.argb(45, 255, 255, 255), 22f)
            setOnClickListener { startNewChat() }
        })
        root.addView(header)

        val workspaceLabel = TextView(this).apply {
            text = "  GENERAL"
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
                    workspaceLabel.text = "  " + name.uppercase(Locale.getDefault())
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
            hint = "Message Darius AI…"
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

        composer.addView(Button(this).apply {
            text = "+"
            textSize = 22f
            setTextColor(blue)
            contentDescription = "Attach files"
            setOnClickListener { openFilePicker() }
        }, LinearLayout.LayoutParams(52, -2))

        composer.addView(input, LinearLayout.LayoutParams(0, -2, 1f))

        composer.addView(Button(this).apply {
            text = "🎙"
            textSize = 18f
            contentDescription = "Voice input"
            setOnClickListener { startVoiceInput() }
        }, LinearLayout.LayoutParams(52, -2))

        composer.addView(Button(this).apply {
            text = "➤"
            textSize = 21f
            setTextColor(Color.WHITE)
            background = rounded(blue, 24f)
            setOnClickListener { sendMessage() }
        }, LinearLayout.LayoutParams(-2, -2).apply { setMargins(8, 0, 0, 0) })

        root.addView(composer)
        setContentView(root)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "image/*", "application/pdf", "text/plain", "text/markdown",
                "text/csv", "application/json", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
        }
        startActivityForResult(intent, PICK_FILES)
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Darius AI")
        }
        try {
            startActivityForResult(intent, SPEECH_INPUT)
        } catch (_: Exception) {
            Toast.makeText(this, "Voice input is not available on this phone.", Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return
        if (requestCode == PICK_FILES) {
            val uris = mutableListOf<Uri>()
            data.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) uris.add(clip.getItemAt(i).uri)
            } ?: data.data?.let { uris.add(it) }
            uris.forEach { uri ->
                try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                attachedFiles.add(uri.toString())
                addMessage("Attachment", "Added: " + (uri.lastPathSegment ?: "Selected file"))
            }
            if (uris.isNotEmpty()) Toast.makeText(this, uris.size.toString() + " file(s) attached", Toast.LENGTH_SHORT).show()
        } else if (requestCode == SPEECH_INPUT) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                input.setText(results[0])
                input.setSelection(input.text.length)
            }
        }
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
        if (sending) return
        val message = input.text.toString().trim()
        if (message.isEmpty() && attachedFiles.isEmpty()) {
            Toast.makeText(this, "Type a message or attach a file.", Toast.LENGTH_SHORT).show()
            return
        }

        val userText = if (message.isEmpty()) "Please analyse the attached file(s)." else message
        addMessage("You", userText)
        input.text.clear()
        attachedFiles.clear()
        sending = true
        addMessage("Darius AI", "Thinking…")

        thread {
            try {
                val connection = URL(BACKEND_URL).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 60000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                val payload = JSONObject().apply {
                    put("message", message.ifEmpty { "Please analyse the attached file(s)." })
                    put("workspace", workspace)
                }.toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val answer = if (connection.responseCode in 200..299) {
                    json.optString("text", "No response text was returned.")
                } else {
                    "I couldn't complete that request: " + json.optString("error", "Server error.")
                }
                runOnUiThread {
                    removeLastMessage()
                    addMessage("Darius AI", answer)
                    sending = false
                }
                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    removeLastMessage()
                    addMessage("Darius AI", "Connection error. Please check your internet connection and try again.")
                    sending = false
                }
            }
        }
    }

    private fun startNewChat() {
        if (sending) {
            Toast.makeText(this, "Please wait for the current response.", Toast.LENGTH_SHORT).show()
            return
        }
        chat.removeAllViews()
        attachedFiles.clear()
        addMessage("Darius AI", "New chat started. What would you like to work on?")
        addQuickActions()
        input.requestFocus()
    }

    private fun removeLastMessage() {
        if (chat.childCount > 0) chat.removeViewAt(chat.childCount - 1)
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
